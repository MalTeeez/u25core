package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.registry.GameData;

/**
 * Samples generated terrain per registered dimension on the integrated server: a grid of chunks around spawn
 * (-Du25core.worldgenChunks, default 8 wide) and four smaller ones 64 chunks out (-Du25core.worldgenFar, default 4, 0
 * for none). Counts blocks, what they drop at fortune 0, biomes, and what may spawn there. Writes worldgen.json.
 * -Du25core.worldgenSkip: dimension ids or provider class name parts to leave out, comma separated. Defaults to
 * Alfheim's Niflheim, whose bridge generator probes chunks along X without end.
 */
final class WorldgenDump {

    /** Run by the server, one per tick, so dimensions done with can unload in between. */
    static final Queue<Runnable> SERVER_THREAD = new ConcurrentLinkedQueue<>();

    private static final int FAR = 64;

    private WorldgenDump() {}

    private static final class Tally {

        final int[] count = new int[16];
        final int[][] at = new int[16][];
    }

    static int write(File dir, JsonObject info) throws IOException {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) throw new IllegalStateException("worldgen needs a singleplayer world");
        JsonArray dims = new JsonArray(), skipped = new JsonArray();
        boolean stuck = false;
        List<String> skip = java.util.Arrays.asList(
            System.getProperty("u25core.worldgenSkip", "Niflheim")
                .split(","));
        for (int id : DimensionManager.getStaticDimensionIDs()) {
            String providerName = providerName(id);
            if (skip.contains(String.valueOf(id)) || skip.stream()
                .anyMatch(part -> !part.isEmpty() && providerName.contains(part))) {
                skipped.add(DataDump.prim(id + ": skipped by u25core.worldgenSkip"));
                continue;
            }
            if (stuck) {
                skipped.add(DataDump.prim(id + ": server busy with an earlier dimension"));
                continue;
            }
            CompletableFuture<JsonObject> result = new CompletableFuture<>();
            SERVER_THREAD.add(() -> {
                try {
                    result.complete(sample(server, id));
                } catch (Throwable t) {
                    result.completeExceptionally(t);
                }
            });
            try {
                dims.add(result.get(10, TimeUnit.MINUTES));
            } catch (ExecutionException e) {
                skipped.add(DataDump.prim(id + ": " + e.getCause()));
            } catch (TimeoutException e) {
                stuck = true;
                skipped.add(DataDump.prim(id + ": timed out"));
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
        JsonObject json = new JsonObject();
        json.add("dims", dims);
        DataDump.writeJson(new File(dir, "worldgen.json"), json);
        if (skipped.size() > 0) info.add("skipped", skipped);
        return dims.size();
    }

    private static String providerName(int id) {
        try {
            return DimensionManager.createProviderFor(id)
                .getClass()
                .getName();
        } catch (Throwable t) {
            return "";
        }
    }

    private static JsonObject sample(MinecraftServer server, int id) {
        WorldServer world = server.worldServerForDimension(id);
        if (world == null || world.provider.dimensionId != id) throw new IllegalStateException("didn't load");
        ChunkProviderServer chunks = world.theChunkProviderServer;
        ChunkCoordinates spawn = world.getSpawnPoint();
        int sx = spawn.posX >> 4, sz = spawn.posZ >> 4;
        int side = Integer.getInteger("u25core.worldgenChunks", 8), far = Integer.getInteger("u25core.worldgenFar", 4);
        List<int[]> grids = new ArrayList<>();
        grids.add(new int[] { sx - side / 2, sz - side / 2, side });
        if (far > 0) for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } })
            grids.add(new int[] { sx + d[0] * FAR - far / 2, sz + d[1] * FAR - far / 2, far });

        Map<Block, Tally> blocks = new IdentityHashMap<>();
        Map<String, Integer> biomes = new TreeMap<>();
        Map<String, Map<String, Integer>> spawns = new TreeMap<>();
        int sampled = 0, unpopulated = 0;
        for (int[] grid : grids) {
            // One more row and column, so every counted chunk has the neighbours it populates with
            for (int x = grid[0]; x <= grid[0] + grid[2]; x++)
                for (int z = grid[1]; z <= grid[1] + grid[2]; z++) chunks.loadChunk(x, z);
            for (int x = grid[0]; x < grid[0] + grid[2]; x++) {
                for (int z = grid[1]; z < grid[1] + grid[2]; z++) {
                    Chunk chunk = chunks.loadChunk(x, z);
                    sampled++;
                    if (!chunk.isTerrainPopulated) unpopulated++;
                    count(chunk, blocks);
                    // Column by column through the world: EndlessIDs refuses the chunk's biome array
                    for (int i = 0; i < 256; i++) {
                        BiomeGenBase gen = world.getBiomeGenForCoords(x * 16 + (i & 15), z * 16 + (i >> 4));
                        if (gen != null) biomes.merge(gen.biomeName, 1, Integer::sum);
                    }
                }
            }
            int cx = (grid[0] + grid[2] / 2) * 16 + 8, cz = (grid[1] + grid[2] / 2) * 16 + 8;
            for (EnumCreatureType type : EnumCreatureType.values()) {
                List<?> entries = world.getChunkProvider()
                    .getPossibleCreatures(type, cx, 64, cz);
                if (entries == null) continue;
                for (Object object : entries) {
                    BiomeGenBase.SpawnListEntry entry = (BiomeGenBase.SpawnListEntry) object;
                    String name = (String) EntityList.classToStringMapping.get(entry.entityClass);
                    spawns.computeIfAbsent(type.name(), k -> new TreeMap<>())
                        .merge(name != null ? name : entry.entityClass.getName(), entry.itemWeight, Math::max);
                }
            }
        }

        JsonObject dim = new JsonObject();
        dim.addProperty("id", id);
        dim.addProperty("name", world.provider.getDimensionName());
        dim.addProperty(
            "provider",
            world.provider.getClass()
                .getName());
        dim.addProperty("chunks", sampled);
        if (unpopulated > 0) dim.addProperty("unpopulated", unpopulated);
        Map<String, Long> blockCounts = new TreeMap<>(), drops = new TreeMap<>();
        for (Map.Entry<Block, Tally> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            String name = GameData.getBlockRegistry()
                .getNameForObject(block);
            Tally tally = entry.getValue();
            for (int meta = 0; meta < 16; meta++) {
                int count = tally.count[meta];
                if (count == 0) continue;
                blockCounts.put(name + ":" + meta, (long) count);
                int[] at = tally.at[meta];
                try {
                    for (ItemStack drop : block.getDrops(world, at[0], at[1], at[2], meta, 0))
                        if (drop != null && drop.getItem() != null)
                            drops.merge(RecipeDump.id(drop), (long) drop.stackSize * count, Long::sum);
                } catch (Throwable ignored) {}
            }
        }
        dim.add("blocks", DataDump.GSON.toJsonTree(blockCounts));
        dim.add("drops", DataDump.GSON.toJsonTree(drops));
        dim.add("biomes", DataDump.GSON.toJsonTree(biomes));
        dim.add("spawns", DataDump.GSON.toJsonTree(spawns));

        if (world.playerEntities.isEmpty()) {
            if (id == 0) chunks.unloadAllChunks();
            else DimensionManager.unloadWorld(id);
        }
        return dim;
    }

    private static void count(Chunk chunk, Map<Block, Tally> blocks) {
        int baseX = chunk.xPosition << 4, baseZ = chunk.zPosition << 4;
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage == null || storage.isEmpty()) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        Block block = storage.getBlockByExtId(x, y, z);
                        if (block == Blocks.air) continue;
                        int meta = storage.getExtBlockMetadata(x, y, z) & 15;
                        Tally tally = blocks.computeIfAbsent(block, b -> new Tally());
                        if (tally.count[meta]++ == 0)
                            tally.at[meta] = new int[] { baseX + x, storage.getYLocation() + y, baseZ + z };
                    }
                }
            }
        }
    }
}
