package net.sxmaa.u25core.radiation;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** How much of a source's emission gets past the shielding blocks around it. */
final class Shielding {

    private static final int DEPTH = 8;
    /** Below this fraction a direction counts as fully shielded, deeper blocks aren't looked at. */
    private static final double MIN_PASS = 0.01;
    private static final int CACHE_TICKS = 1200;
    /** Positions cached per dimension before the cache is dropped, bounds memory for sources that are long gone. */
    private static final int MAX_ENTRIES = 20000;

    private static final Map<Integer, Map<Long, double[]>> cache = new HashMap<>();

    private Shielding() {}

    /** Average over the 6 directions of the fraction passing up to {@value DEPTH} blocks of shielding, cached. */
    static double getPassFraction(World world, int x, int y, int z) {
        Map<Long, double[]> dim = cache.computeIfAbsent(world.provider.dimensionId, d -> new HashMap<>());
        long key = ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
        double[] entry = dim.get(key);
        long now = world.getTotalWorldTime();
        if (entry != null && now - (long) entry[1] < CACHE_TICKS) return entry[0];

        double total = 0;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            double pass = 1;
            for (int d = 1; d <= DEPTH; d++) {
                int bx = x + dir.offsetX * d, by = y + dir.offsetY * d, bz = z + dir.offsetZ * d;
                if (by < 0 || by > 255 || !world.blockExists(bx, by, bz)) break;
                Block block = world.getBlock(bx, by, bz);
                RadiationRegistry.Shield shield = RadiationRegistry
                    .getShield(block, world.getBlockMetadata(bx, by, bz));
                pass *= 1 - (shield == RadiationRegistry.Shield.NONE ? byMaterial(block) : shield.radiationBlock);
                if (pass < MIN_PASS) break;
            }
            total += pass;
        }
        double fraction = total / 6;
        if (dim.size() >= MAX_ENTRIES) dim.clear();
        dim.put(key, new double[] { fraction, now });
        return fraction;
    }

    /**
     * Blocks without a shielding entry, as a metre of their material against ~1 MeV gamma, scatter included: anything
     * dense and solid stops nearly all of it, wood ~92%, wool and snow ~80%, leaves little, open blocks nothing.
     */
    private static double byMaterial(Block block) {
        Material m = block.getMaterial();
        if (!m.blocksMovement()) return 0;
        if (m == Material.leaves) return 0.1;
        if (m == Material.cloth || m == Material.craftedSnow) return 0.8;
        if (m == Material.wood) return 0.92;
        // Glass, panes, slabs, fences: solid but thin or partial
        return block.isOpaqueCube() ? 0.999 : 0.9;
    }

    static void clear(int dimension) {
        cache.remove(dimension);
    }
}
