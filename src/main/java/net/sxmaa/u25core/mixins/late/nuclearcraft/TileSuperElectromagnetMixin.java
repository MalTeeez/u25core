package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import nc.tile.accelerator.TileSuperElectromagnet;

@Mixin(value = TileSuperElectromagnet.class, remap = false)
public class TileSuperElectromagnetMixin {

    @Shadow
    public static int power;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void u25core$raisePowerUse(CallbackInfo ci) {
        power *= 36;
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcofh/api/energy/EnergyStorage;<init>(II)V"),
        index = 0)
    private int u25core$raiseCapacity(int capacity) {
        return (int) (power * 10 * 1.01);
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcofh/api/energy/EnergyStorage;<init>(II)V"),
        index = 1)
    private int u25core$raiseMaxTransfer(int maxTransfer) {
        return power * 25;
    }
}
