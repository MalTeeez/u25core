package net.sxmaa.u25core.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class DebugUtil {

    /**
     * Creates a 2D map representation of blocks in a cubic radius around a coordinate
     * Each block is represented by its first character
     *
     * @param world  The world to scan
     * @param x      Center X coordinate
     * @param y      Center Y coordinate
     * @param z      Center Z coordinate
     * @param radius Radius around the coordinate to scan
     * @return String array representing horizontal slices of blocks, from bottom to top
     */
    public static String[] mapBlocksInRadius(World world, int x, int y, int z, int radius) {
        int size = radius * 2 + 1;
        String[] result = new String[size];

        for (int dy = -radius; dy <= radius; dy++) {
            StringBuilder layer = new StringBuilder();
            layer.append("Y=")
                .append(y + dy)
                .append(": ");

            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    Block block = world.getBlock(x + dx, y + dy, z + dz);
                    String blockName = Block.blockRegistry.getNameForObject(block);

                    if (blockName == null || blockName.isEmpty() || block.getMaterial() == Material.air) {
                        layer.append(' ');
                    } else {
                        // Get the last part after the colon (e.g., "minecraft:stone" -> "stone")
                        String[] parts = blockName.split(":");
                        String simpleName = parts.length > 1 ? parts[1] : blockName;
                        layer.append(Character.toUpperCase(simpleName.charAt(0)));
                    }
                }
                if (dz < radius) {
                    layer.append(" ");
                }
            }

            result[dy + radius] = layer.toString();
        }

        return result;
    }

    /**
     * Creates a single-layer 2D map representation of blocks around a coordinate
     *
     * @param world  The world to scan
     * @param x      Center X coordinate
     * @param y      Center Y coordinate
     * @param z      Center Z coordinate
     * @param radius Radius around the coordinate to scan
     * @return String array representing a horizontal slice at the given Y level
     */
    public static String[] mapBlocksAtLayer(World world, int x, int y, int z, int radius) {
        int size = radius * 2 + 1;
        String[] result = new String[size];

        for (int dz = -radius; dz <= radius; dz++) {
            StringBuilder row = new StringBuilder();
            for (int dx = -radius; dx <= radius; dx++) {
                Block block = world.getBlock(x + dx, y, z + dz);
                String blockName = Block.blockRegistry.getNameForObject(block);

                if (blockName == null || blockName.isEmpty() || block.getMaterial() == Material.air) {
                    row.append(' ');
                } else {
                    String[] parts = blockName.split(":");
                    String simpleName = parts.length > 1 ? parts[1] : blockName;
                    row.append(Character.toUpperCase(simpleName.charAt(0)));
                }
            }
            result[dz + radius] = row.toString();
        }

        return result;
    }
}
