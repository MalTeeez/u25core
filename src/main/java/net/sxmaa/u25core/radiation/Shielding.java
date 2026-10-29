package net.sxmaa.u25core.radiation;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** How much of a source's emission gets past the shielding blocks around it. */
final class Shielding {

    private static final int DEPTH = 8;
    /** Below this fraction a direction counts as fully shielded, deeper blocks aren't looked at. */
    private static final double MIN_PASS = 0.01;
    private static final double MAX_HARDNESS_BLOCK = 0.5;
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
                pass *= 1 - (shield == RadiationRegistry.Shield.NONE ? byHardness(world, block, bx, by, bz)
                    : shield.radiationBlock);
                if (pass < MIN_PASS) break;
            }
            total += pass;
        }
        double fraction = total / 6;
        if (dim.size() >= MAX_ENTRIES) dim.clear();
        dim.put(key, new double[] { fraction, now });
        return fraction;
    }

    /** Blocks without a shielding entry block by hardness, unbreakable ones as much as the cap allows. */
    private static double byHardness(World world, Block block, int x, int y, int z) {
        float hardness = block.getBlockHardness(world, x, y, z);
        if (hardness < 0) hardness = Float.MAX_VALUE;
        return Math.min(MAX_HARDNESS_BLOCK, hardness * RadiationConfig.hardnessShielding);
    }

    static void clear(int dimension) {
        cache.remove(dimension);
    }
}
