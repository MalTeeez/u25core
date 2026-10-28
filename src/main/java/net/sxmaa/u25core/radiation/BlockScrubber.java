package net.sxmaa.u25core.radiation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class BlockScrubber extends BlockContainer {

    public BlockScrubber() {
        super(Material.iron);
        setBlockName("u25core.scrubber");
        setBlockTextureName("u25core:scrubber");
        setHardness(3);
        setResistance(10);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileScrubber();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote || !(world.getTileEntity(x, y, z) instanceof TileScrubber scrubber)) return true;
        ItemStack held = player.getCurrentEquippedItem();
        if (TileScrubber.isFilter(held)) {
            ItemStack current = scrubber.getStackInSlot(0);
            int space = current == null ? 64 : current.isItemEqual(held) ? 64 - current.stackSize : 0;
            int moved = Math.min(space, held.stackSize);
            if (moved > 0) {
                ItemStack added = held.splitStack(moved);
                if (current != null) added.stackSize += current.stackSize;
                scrubber.setInventorySlotContents(0, added);
            }
        }
        player.addChatComponentMessage(
            new ChatComponentTranslation(
                "u25core.scrubber.status",
                scrubber.getFilterCount(),
                scrubber.getFilterTicks() / 20));
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (world.getTileEntity(x, y, z) instanceof TileScrubber scrubber && scrubber.getStackInSlot(0) != null) {
            world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, scrubber.getStackInSlot(0)));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
