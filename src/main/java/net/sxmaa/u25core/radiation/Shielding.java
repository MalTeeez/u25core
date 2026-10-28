package net.sxmaa.u25core.radiation;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** How much of a source's emission gets past the shielding blocks around it. */
final class Shielding {

    private static final int DEPTH = 3;
    private static final int CACHE_TICKS = 1200;

    private static final Map<String, double[]> cache = new HashMap<>();

    private Shielding() {}

    /** Average over the 6 directions of the fraction passing up to {@value DEPTH} blocks of shielding, cached. */
    static double getPassFraction(World world, int x, int y, int z) {
        String key = world.provider.dimensionId + ":" + x + ":" + y + ":" + z;
        double[] entry = cache.get(key);
        long now = world.getTotalWorldTime();
        if (entry != null && now - (long) entry[1] < CACHE_TICKS) return entry[0];

        double total = 0;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            double pass = 1;
            for (int d = 1; d <= DEPTH; d++) {
                int bx = x + dir.offsetX * d, by = y + dir.offsetY * d, bz = z + dir.offsetZ * d;
                if (by < 0 || by > 255 || !world.blockExists(bx, by, bz)) break;
                pass *= 1 - RadiationRegistry
                    .getShield(world.getBlock(bx, by, bz), world.getBlockMetadata(bx, by, bz)).radiationBlock;
            }
            total += pass;
        }
        double fraction = total / 6;
        cache.put(key, new double[] { fraction, now });
        return fraction;
    }

    static void clear() {
        cache.clear();
    }
}
