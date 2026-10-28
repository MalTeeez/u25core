package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Server side: chunk persistence, world updates, player dose and sync. */
public final class RadiationEventHandler {

    private static final String CHUNK_KEY = "u25core:radiation";
    private static final int CHERENKOV_INTERVAL = 40;
    private static final double CHERENKOV_RANGE = 48;
    private static final int MOB_INTERVAL = 40;

    private final Map<UUID, double[]> lastSent = new HashMap<>();
    private final java.util.Set<UUID> cherenkovPlayers = new java.util.HashSet<>();

    // region Persistence

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        World world = event.world;
        if (world.isRemote) return;
        Chunk chunk = event.getChunk();
        ChunkRadiation saved = null;
        if (event.getData()
            .hasKey(CHUNK_KEY)) {
            saved = new ChunkRadiation();
            saved.readFromNBT(
                event.getData()
                    .getCompoundTag(CHUNK_KEY));
        }
        // Radiation sources track themselves when added to the chunk, scrubbers don't
        boolean scrubbers = false;
        for (Object o : chunk.chunkTileEntityMap.values()) {
            if (o instanceof TileScrubber) {
                scrubbers = true;
                break;
            }
        }
        RadiationManager.INSTANCE.loadChunk(world, chunk.xPosition, chunk.zPosition, saved, scrubbers);
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        World world = event.world;
        if (world.isRemote) return;
        Chunk chunk = event.getChunk();
        ChunkRadiation data = RadiationManager.INSTANCE.getChunk(world, chunk.xPosition, chunk.zPosition);
        if (data == null) return;
        NBTTagCompound tag = new NBTTagCompound();
        data.writeToNBT(tag);
        event.getData()
            .setTag(CHUNK_KEY, tag);
        // ChunkEvent.Unload fires before the save, so drop it here once the chunk is on its way out
        if (!chunk.isChunkLoaded) RadiationManager.INSTANCE.removeChunk(world, chunk.xPosition, chunk.zPosition);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.world.isRemote) RadiationManager.INSTANCE.unloadWorld(event.world);
    }

    // endregion

    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityLivingBase
            && event.entity.getExtendedProperties(EntityRadiationData.KEY) == null) {
            event.entity.registerExtendedProperties(EntityRadiationData.KEY, new EntityRadiationData());
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityItem item
            && RadiationRegistry.get(item.getEntityItem()) > 0) {
            RadiationManager.INSTANCE.trackChunk(
                event.world,
                (int) Math.floor(item.posX) >> 4,
                (int) Math.floor(item.posZ) >> 4).hasSources = true;
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityRadiationData old = EntityRadiationData.get(event.original);
        EntityRadiationData now = EntityRadiationData.get(event.entityPlayer);
        if (old == null || now == null) return;
        if (event.wasDeath) {
            // Capped, so dying of radiation doesn't respawn you straight back into the worst effects
            now.dose = Math.min(old.dose * RadiationConfig.respawnDoseKept, RadiationConfig.lethalDose * 0.5);
            now.immunityTicks = RadiationConfig.respawnImmunity;
        } else {
            NBTTagCompound tag = new NBTTagCompound();
            old.saveNBTData(tag);
            now.loadNBTData(tag);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || !RadiationConfig.enabled) return;
        RadiationManager.INSTANCE.tickWorld(event.world);
        MeltdownData.get(event.world)
            .tick(event.world);
        if (RadiationConfig.cherenkovLevel > 0 && event.world.getTotalWorldTime() % CHERENKOV_INTERVAL == 0) {
            sendCherenkov(event.world);
        }
    }

    private void sendCherenkov(World world) {
        int chunkRadius = (int) Math.ceil(CHERENKOV_RANGE / 16);
        for (Object o : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) o;
            List<double[]> near = new ArrayList<>();
            for (double[] s : RadiationManager.INSTANCE.getHotSources(world, player.posX, player.posZ, chunkRadius)) {
                if (player.getDistanceSq(s[0], s[1], s[2]) < CHERENKOV_RANGE * CHERENKOV_RANGE) near.add(s);
            }
            // Send an empty list once when the last hot source goes out of range, so the glow stops
            boolean send;
            if (!near.isEmpty()) {
                cherenkovPlayers.add(player.getUniqueID());
                send = true;
            } else {
                send = cherenkovPlayers.remove(player.getUniqueID());
            }
            if (send) RadiationNetwork.sendCherenkov(player, near);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.worldObj.isRemote || !RadiationConfig.enabled) return;
        int interval = RadiationConfig.playerUpdateInterval;
        // Spread players over the interval
        if ((player.worldObj.getTotalWorldTime() + player.getEntityId()) % interval != 0) return;
        EntityRadiationData data = EntityRadiationData.get(player);
        if (data == null) return;

        data.update(player, interval);
        double level = RadiationManager.INSTANCE.getLevel(player.worldObj, player.posX, player.posY, player.posZ);

        double[] last = lastSent.computeIfAbsent(player.getUniqueID(), id -> new double[] { -1, -1, -1 });
        if (player instanceof EntityPlayerMP mp && RadiationNetwork.shouldSend(last, data.dose, data.rate, level)) {
            RadiationNetwork.sendPlayer(mp, data.dose, data.rate, level);
            last[0] = data.dose;
            last[1] = data.rate;
            last[2] = level;
        }
    }

    /** Mobs update less often than players; their dose recovers and acts wherever they are. */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity instanceof EntityPlayer || entity.worldObj.isRemote || !RadiationConfig.enabled) return;
        if ((entity.worldObj.getTotalWorldTime() + entity.getEntityId()) % MOB_INTERVAL != 0) return;
        EntityRadiationData data = EntityRadiationData.get(entity);
        if (data != null) data.update(entity, MOB_INTERVAL);
    }

    @SubscribeEvent
    public void onLogout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        lastSent.remove(event.player.getUniqueID());
        cherenkovPlayers.remove(event.player.getUniqueID());
    }
}
