package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import nc.tile.accelerator.TileSynchrotron;

@Mixin(value = TileSynchrotron.class, remap = false)
public class SynchrotronRingScanMixin {

    @Shadow
    public double percentageOn;

    @Shadow
    private int checkCount;

    // percentageOn() rescans the whole ring every tick, only do it right after the throttled checkRing()
    @WrapOperation(
        method = "updateEntity",
        at = @At(value = "INVOKE", target = "Lnc/tile/accelerator/TileSynchrotron;percentageOn()Z", remap = false),
        remap = true)
    private boolean u25core$throttleRingScan(TileSynchrotron self, Operation<Boolean> original) {
        return checkCount == 0 ? original.call(self) : percentageOn > 0;
    }
}
