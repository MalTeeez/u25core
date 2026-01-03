package net.sxmaa.u25core.mixins.late.blockrenderer6343;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

import Reika.DragonAPI.Base.BlockTileEnum;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;

@Mixin(value = IStructureElement.BlocksToPlace.class, remap = false)
public abstract class BlocksToPlaceMixin {

    @Shadow
    public static IStructureElement.BlocksToPlace create(ItemStack... stacks) {
        return null;
    }

    @Inject(
        method = "create(Lnet/minecraft/block/Block;I)Lcom/gtnewhorizon/structurelib/structure/IStructureElement$BlocksToPlace;",
        at = @At("HEAD"),
        cancellable = true)
    private static void handleReikaItemStacks(Block block, int meta,
        CallbackInfoReturnable<IStructureElement.BlocksToPlace> cir) {
        if (block instanceof BlockTileEnum) {
            ItemStack itemStack = ReikaRenderHelper.getBlockItem(block, meta);
            if (itemStack != null) {
                cir.setReturnValue(create(itemStack));
            }
        }
    }
}
