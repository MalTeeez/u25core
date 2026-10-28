package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.sxmaa.u25core.api.radiation.INeutronHandler;
import net.sxmaa.u25core.api.radiation.IRadiationManager;
import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.api.radiation.NeutronType;

/** Server side implementation of the radiation API: chunk levels, contamination, sources and neutrons. */
public final class RadiationManager implements IRadiationManager {

    public static final RadiationManager INSTANCE = new RadiationManager();
    /** Ticks per hour, converts Sv/h into Sv per tick. */
    public static final double TICKS_PER_HOUR = 72000;

    private static final Random RAND = new Random();

    private final Map<Integer, WorldRadiation> worlds = new HashMap<>();

    /** Per dimension chunk data and the round robin position of its update loop. */
    static final class WorldRadiation {

        final Map<Long, ChunkRadiation> chunks = new HashMap<>();
        final List<Long> order = new ArrayList<>();
        int cursor;
        /** Hot sources found during the last updates, for Cherenkov effects: x, y, z, emission. */
        final List<double[]> hotSources = new ArrayList<>();
    }

    private RadiationManager() {}

    static long key(int chunkX, int chunkZ) {
        return ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
    }

    WorldRadiation world(World world) {
        return worlds.computeIfAbsent(world.provider.dimensionId, d -> new WorldRadiation());
    }

    void unloadWorld(World world) {
        worlds.remove(world.provider.dimensionId);
    }

    ChunkRadiation getChunk(World world, int chunkX, int chunkZ) {
        return world(world).chunks.get(key(chunkX, chunkZ));
    }

    ChunkRadiation trackChunk(World world, int chunkX, int chunkZ) {
        WorldRadiation wr = world(world);
        long key = key(chunkX, chunkZ);
        ChunkRadiation chunk = wr.chunks.get(key);
        if (chunk == null) {
            chunk = new ChunkRadiation();
            wr.chunks.put(key, chunk);
            wr.order.add(key);
        }
        return chunk;
    }

    void putChunk(World world, int chunkX, int chunkZ, ChunkRadiation chunk) {
        WorldRadiation wr = world(world);
        if (wr.chunks.put(key(chunkX, chunkZ), chunk) == null) wr.order.add(key(chunkX, chunkZ));
    }

    void removeChunk(World world, int chunkX, int chunkZ) {
        WorldRadiation wr = world(world);
        if (wr.chunks.remove(key(chunkX, chunkZ)) != null) wr.order.remove(Long.valueOf(key(chunkX, chunkZ)));
    }

    // region World update

    /** Updates a budget of tracked chunks. Each one covers the ticks since its last update. */
    void tickWorld(World world) {
        WorldRadiation wr = world(world);
        int size = wr.order.size();
        if (size == 0) return;
        int budget = Math.min(RadiationConfig.chunksPerTick, size);
        // Ticks between two updates of the same chunk
        double ticks = Math.max(1, (double) size / RadiationConfig.chunksPerTick);
        if (wr.cursor == 0) wr.hotSources.clear();
        for (int i = 0; i < budget && !wr.order.isEmpty(); i++) {
            if (wr.cursor >= wr.order.size()) wr.cursor = 0;
            long key = wr.order.get(wr.cursor);
            int chunkX = (int) key, chunkZ = (int) (key >> 32);
            ChunkRadiation chunk = wr.chunks.get(key);
            if (chunk == null || !world.getChunkProvider()
                .chunkExists(chunkX, chunkZ)) {
                wr.cursor++;
                continue;
            }
            updateChunk(world, wr, world.getChunkFromChunkCoords(chunkX, chunkZ), chunk, ticks);
            if (chunk.isIdle()) {
                wr.chunks.remove(key);
                wr.order.remove(wr.cursor);
            } else {
                wr.cursor++;
            }
        }
    }

    private void updateChunk(World world, WorldRadiation wr, Chunk c, ChunkRadiation chunk, double ticks) {
        int baseX = c.xPosition << 4, baseZ = c.zPosition << 4;
        double sources = 0;
        double scrubbing = 1;
        boolean hasSources = false;
        for (Object o : c.chunkTileEntityMap.values()) {
            TileEntity te = (TileEntity) o;
            if (te instanceof IRadiationSource source) {
                double emission = source.getRadiationEmission();
                if (emission > 0) {
                    hasSources = true;
                    sources += emission * Shielding.getPassFraction(world, te.xCoord, te.yCoord, te.zCoord);
                    if (RadiationConfig.cherenkovLevel > 0 && emission >= RadiationConfig.cherenkovLevel) {
                        wr.hotSources.add(new double[] { te.xCoord, te.yCoord, te.zCoord, emission });
                    }
                }
            } else if (te instanceof TileScrubber scrubber && scrubber.isRunning()) {
                hasSources = true;
                scrubbing *= 1 - RadiationConfig.scrubberEfficiency;
                scrubber.consume((int) ticks);
            }
        }
        List<EntityLivingBase> living = new ArrayList<>();
        for (List<?> list : c.entityLists) {
            for (Object o : list) {
                if (o instanceof EntityItem item) {
                    double activity = RadiationRegistry.getTotal(item.getEntityItem());
                    if (activity > 0) {
                        hasSources = true;
                        sources += activity;
                    }
                } else if (o instanceof EntityLivingBase entity && !(o instanceof EntityPlayer)) {
                    living.add(entity);
                }
            }
        }
        chunk.hasSources = hasSources;

        double seconds = ticks / 20;
        double target = (sources + chunk.contamination) * scrubbing;
        double rate = target > chunk.level ? RadiationConfig.riseRate : RadiationConfig.fallRate;
        chunk.level += (target - chunk.level) * (1 - Math.pow(1 - rate, seconds));
        if (chunk.level < 1.0E-12) chunk.level = 0;

        double decay = world.isRaining() && world.canBlockSeeTheSky(baseX + 8, 255, baseZ + 8)
            ? RadiationConfig.rainCleanupFactor
            : 1;
        chunk.contamination *= Math.pow(0.5, ticks * decay / RadiationConfig.contaminationHalfLife);
        if (chunk.contamination < 1.0E-12) chunk.contamination = 0;

        spread(world, c.xPosition, c.zPosition, chunk, seconds);
        // Chunks only get saved when flagged as modified
        if (!chunk.isIdle()) c.setChunkModified();

        double level = chunk.level + RadiationRegistry.getBackground(world, baseX + 8, baseZ + 8);
        for (EntityLivingBase entity : living) {
            EntityRadiationData.expose(entity, level, ticks);
            EntityRadiationData data = EntityRadiationData.get(entity);
            if (data != null && data.dose > 0) data.update(entity, (int) ticks);
        }
        if (RadiationConfig.terrainDamageLevel > 0 && chunk.level >= RadiationConfig.terrainDamageLevel) {
            damageTerrain(world, baseX + RAND.nextInt(16), baseZ + RAND.nextInt(16));
        }
    }

    /** Moves part of the level difference into lower neighbouring chunks, without creating any. */
    private void spread(World world, int chunkX, int chunkZ, ChunkRadiation chunk, double seconds) {
        if (chunk.level <= 0 || RadiationConfig.spreadRate <= 0) return;
        double factor = 1 - Math.pow(1 - RadiationConfig.spreadRate, seconds);
        int[][] offsets = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] off : offsets) {
            int nx = chunkX + off[0], nz = chunkZ + off[1];
            if (!world.getChunkProvider()
                .chunkExists(nx, nz)) continue;
            ChunkRadiation neighbour = getChunk(world, nx, nz);
            double nLevel = neighbour == null ? 0 : neighbour.level;
            if (chunk.level <= nLevel * 1.1) continue;
            double moved = (chunk.level - nLevel) * factor / 2;
            if (moved < 1.0E-12) continue;
            if (neighbour == null) neighbour = trackChunk(world, nx, nz);
            neighbour.level += moved;
            chunk.level -= moved;
            world.getChunkFromChunkCoords(nx, nz)
                .setChunkModified();
        }
    }

    private void damageTerrain(World world, int x, int z) {
        int y = world.getHeightValue(x, z);
        for (int dy = 0; dy >= -2; dy--) {
            Block block = world.getBlock(x, y + dy, z);
            if (block instanceof BlockBush && block != Blocks.deadbush) {
                world.setBlock(
                    x,
                    y + dy,
                    z,
                    world.getBlock(x, y + dy - 1, z) == Blocks.sand ? Blocks.deadbush : Blocks.air);
                return;
            } else if (block instanceof BlockLeavesBase) {
                world.setBlockToAir(x, y + dy, z);
                return;
            } else if (block == Blocks.grass || block == Blocks.mycelium) {
                world.setBlock(x, y + dy, z, Blocks.dirt);
                return;
            }
        }
    }

    // endregion

    // region API

    @Override
    public boolean isEnabled() {
        return RadiationConfig.enabled;
    }

    @Override
    public double getLevel(World world, double x, double y, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        ChunkRadiation chunk = getChunk(world, bx >> 4, bz >> 4);
        return RadiationRegistry.getBackground(world, bx, bz) + (chunk == null ? 0 : chunk.level);
    }

    @Override
    public double getDose(EntityLivingBase entity) {
        EntityRadiationData data = EntityRadiationData.get(entity);
        return data == null ? 0 : data.dose;
    }

    @Override
    public void addDose(EntityLivingBase entity, double sieverts) {
        EntityRadiationData data = EntityRadiationData.get(entity);
        if (data != null && !EntityRadiationData.isImmune(entity)) data.dose = Math.max(0, data.dose + sieverts);
    }

    @Override
    public void expose(EntityLivingBase entity, double svPerHour, int ticks) {
        EntityRadiationData.expose(entity, svPerHour, ticks);
    }

    @Override
    public void exposeArea(World world, AxisAlignedBB box, double svPerHour, int ticks) {
        for (Object o : world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            EntityRadiationData.expose((EntityLivingBase) o, svPerHour, ticks);
        }
    }

    @Override
    public void contaminate(World world, double x, double y, double z, double svPerHour) {
        if (world.isRemote || svPerHour <= 0) return;
        int chunkX = (int) Math.floor(x) >> 4, chunkZ = (int) Math.floor(z) >> 4;
        trackChunk(world, chunkX, chunkZ).contamination += svPerHour;
        if (world.getChunkProvider()
            .chunkExists(chunkX, chunkZ))
            world.getChunkFromChunkCoords(chunkX, chunkZ)
                .setChunkModified();
    }

    @Override
    public boolean release(World world, double x, double y, double z, String key, double amount) {
        double activity = RadiationRegistry.get(key) * amount;
        if (activity <= 0) return false;
        contaminate(world, x, y, z, activity);
        return true;
    }

    @Override
    public boolean release(World world, double x, double y, double z, ItemStack stack) {
        double activity = RadiationRegistry.getTotal(stack);
        if (activity <= 0) return false;
        contaminate(world, x, y, z, activity);
        return true;
    }

    @Override
    public boolean release(World world, double x, double y, double z, FluidStack stack) {
        double activity = RadiationRegistry.get(stack);
        if (activity <= 0) return false;
        contaminate(world, x, y, z, activity);
        return true;
    }

    @Override
    public double getRadioactivity(String key) {
        return RadiationRegistry.get(key);
    }

    @Override
    public double getRadioactivity(ItemStack stack) {
        return RadiationRegistry.get(stack);
    }

    @Override
    public double getRadioactivity(FluidStack stack) {
        return RadiationRegistry.get(stack);
    }

    @Override
    public void registerRadioactive(String key, double svPerHourPerUnit) {
        RadiationRegistry.register(key, svPerHourPerUnit);
    }

    @Override
    public void notifySource(TileEntity tile) {
        if (tile.getWorldObj() == null || tile.getWorldObj().isRemote) return;
        trackChunk(tile.getWorldObj(), tile.xCoord >> 4, tile.zCoord >> 4).hasSources = true;
    }

    @Override
    public void emitNeutrons(World world, int x, int y, int z, int count, NeutronType type) {
        if (world.isRemote) return;
        ForgeDirection[] dirs = ForgeDirection.VALID_DIRECTIONS;
        for (int n = 0; n < count; n++) {
            ForgeDirection dir = dirs[RAND.nextInt(dirs.length)];
            boolean thermal = false;
            for (int step = 1; step <= RadiationConfig.neutronRange; step++) {
                int bx = x + dir.offsetX * step, by = y + dir.offsetY * step, bz = z + dir.offsetZ * step;
                if (by < 0 || by > 255 || !world.blockExists(bx, by, bz)) break;
                Block block = world.getBlock(bx, by, bz);
                TileEntity te = world.getTileEntity(bx, by, bz);
                if (te instanceof INeutronHandler handler) {
                    if (handler.onNeutron(world, bx, by, bz, type, thermal)) break;
                } else if (block instanceof INeutronHandler handler) {
                    if (handler.onNeutron(world, bx, by, bz, type, thermal)) break;
                }
                RadiationRegistry.Shield shield = RadiationRegistry
                    .getShield(block, world.getBlockMetadata(bx, by, bz));
                if (shield.moderates) thermal = true;
                if (shield.neutronAbsorb > 0 && RAND.nextDouble() < shield.neutronAbsorb) break;
            }
        }
    }

    @Override
    public void scheduleMeltdown(World world, AxisAlignedBB bounds, float explosionPower, double contamination,
        int durationTicks) {
        if (!world.isRemote) MeltdownData.get(world)
            .add(bounds, explosionPower, contamination, durationTicks);
    }

    // endregion

    /** Hot sources of a world for Cherenkov effects, refreshed as chunks update. */
    List<double[]> getHotSources(World world) {
        return world(world).hotSources;
    }

    /** Radioactivity of everything an entity carries, in Sv/h. */
    static double getCarried(Entity entity) {
        if (!(entity instanceof EntityPlayer player)) return 0;
        double total = 0;
        for (ItemStack stack : player.inventory.mainInventory) total += RadiationRegistry.getTotal(stack);
        return total;
    }
}
