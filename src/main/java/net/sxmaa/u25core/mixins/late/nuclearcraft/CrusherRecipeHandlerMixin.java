package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import nc.crafting.nei.CrusherRecipeHandler;

@Mixin(value = CrusherRecipeHandler.class, remap = false)
public class CrusherRecipeHandlerMixin {

    // newInstance checks the never assigned ecrushfuels, so the fuel scan reran for every handler instance
    @WrapWithCondition(
        method = "newInstance",
        at = @At(value = "INVOKE", target = "Lnc/crafting/nei/CrusherRecipeHandler;findCrushFuels()V"))
    private boolean u25core$findFuelsOnce() {
        return CrusherRecipeHandler.acrushfuels == null;
    }
}
