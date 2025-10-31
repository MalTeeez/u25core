package net.sxmaa.u25core.common.multiblocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.sxmaa.u25core.U25Core;

public class DebugItem extends Item {

    public DebugItem() {
        setUnlocalizedName("u25core.debug");
        setTextureName(U25Core.MODID + ":debugItem");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null) {
            U25Core.chat(String.format("TE at %d %d %d is of type " + te.getClass(), x, y, z));
        } else {
            U25Core.chat(String.format("TE at %d %d %d is null", x, y, z));
        }
        // String[] slices = DebugUtil.mapBlocksAtLayer(world, x, y, z, 32);
        // for (String slice : slices) {
        // U25Core.chat(slice);
        // }
        return true;
    }
}
