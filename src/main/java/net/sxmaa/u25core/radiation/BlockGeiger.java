package net.sxmaa.u25core.radiation;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Emits redstone (and comparator output) by the radiation level, on the same log scale as the readout colours: 0 at
 * background (10 uSv/h), 15 at 100 Sv/h. The current strength is kept in the metadata.
 */
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

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        double level = RadiationManager.INSTANCE.getLevel(world, x + 0.5, y + 0.5, z + 0.5);
        int signal = (int) Math.ceil(RadiationFormat.severity(level) * 15);
        // Notifies redstone and comparators only when the strength changes
        if (signal != world.getBlockMetadata(x, y, z)) world.setBlockMetadataWithNotify(x, y, z, signal, 3);
        world.scheduleBlockUpdate(x, y, z, this, 20);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }

    /** Placed blocks drop as the plain detector, whatever their current strength. */
    @Override
    public int damageDropped(int meta) {
        return 0;
    }
}
