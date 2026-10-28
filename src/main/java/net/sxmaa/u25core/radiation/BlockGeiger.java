package net.sxmaa.u25core.radiation;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

/** Comparator output on a log scale of the radiation level, 1 per decade from 1 uSv/h up to 15 at 100 MSv/h. */
public class BlockGeiger extends Block {

    public BlockGeiger() {
        super(Material.iron);
        setBlockName("u25core.geigerBlock");
        setBlockTextureName("u25core:geiger_block");
        setHardness(2);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, this, 20);
    }

    // Comparators only re-read on neighbour updates, so poke them every second
    @Override
    public void updateTick(World world, int x, int y, int z, java.util.Random random) {
        world.func_147453_f(x, y, z, this);
        world.scheduleBlockUpdate(x, y, z, this, 20);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        double level = RadiationManager.INSTANCE.getLevel(world, x + 0.5, y + 0.5, z + 0.5);
        return (int) Math.max(0, Math.min(15, Math.floor(Math.log10(level) + 7)));
    }
}
