package net.sxmaa.u25core.mixin.late.blockrenderer6343;

import static blockrenderer6343.client.utils.BRUtil.FAKE_PLAYER;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import Reika.ReactorCraft.Items.ItemReactorPlacer;
import blockrenderer6343.client.world.TrackedDummyWorld;
import blockrenderer6343.integration.structurelib.StructureCompatGuiHandler;

@Mixin(value = StructureCompatGuiHandler.class, remap = false)
public class StructureCompatGuiHandlerMixin {

    @WrapOperation(
        method = "placeMultiblock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public Item useFakeItemIfReikaPlacer(ItemStack instance, Operation<Item> original) {
        if (instance.getItem() instanceof ItemReactorPlacer) {
            // Use dummy item since we use TODO to place it if this condition matches
            return Item.getItemFromBlock(Blocks.dirt);
        }
        return original.call(instance);
    }

    @WrapOperation(
        method = "placeMultiblock",
        at = @At(
            value = "INVOKE",
            target = "Lblockrenderer6343/client/world/TrackedDummyWorld;setBlock(IIILnet/minecraft/block/Block;II)Z"))
    public boolean useSpecialPlaceIfReikaPlacer(TrackedDummyWorld world, int x, int y, int z, Block block, int meta,
        int updateFlag, Operation<Boolean> original) {
        ItemStack item = ((GuiMultiblockHandlerAccessor) this).u25core$getStackForm();
        if (item.getItem() instanceof ItemReactorPlacer) {
            FAKE_PLAYER.setWorld(world);
            return item
                .tryPlaceItemIntoWorld(FAKE_PLAYER, world, x, y, z, ForgeDirection.UP.ordinal(), 0.5f, 0.5f, 0.5f);
        } else {
            return original.call(world, x, y, z, block, meta, updateFlag);
        }
    }
}
