package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cofh.api.energy.EnergyStorage;
import nc.tile.accelerator.TileSynchrotron;

@Mixin(value = TileSynchrotron.class, remap = false)
public class TileSynchrotronMixin {

    @Shadow
    public EnergyStorage storage;

    @Shadow
    public int length;

    @Shadow
    public double antimatter;

    @Unique
    private boolean u25core$drainNextTick;

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcofh/api/energy/EnergyStorage;<init>(II)V"),
        index = 0)
    private int u25core$raiseCapacity(int capacity) {
        return 250_000_000;
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcofh/api/energy/EnergyStorage;<init>(II)V"),
        index = 1)
    private int u25core$raiseMaxTransfer(int maxTransfer) {
        return 10_000_000;
    }

    @Inject(method = "power", at = @At("HEAD"))
    private void u25core$drainAfterFirstAntimatter(CallbackInfo ci) {
        if (!u25core$drainNextTick) return;
        storage.extractEnergy(storage.getMaxEnergyStored() - storage.getMaxExtract() * 2, false);
        u25core$drainNextTick = false;
    }

    @ModifyConstant(method = "power", constant = @Constant(doubleValue = 2.56E8))
    private double u25core$raiseAntimatterCap(double constant) {
        return 3.56E8;
    }

    // Scale antimatter gain with the square of the ring length, then quarter it
    @WrapOperation(
        method = "power",
        at = @At(
            value = "FIELD",
            target = "Lnc/tile/accelerator/TileSynchrotron;antimatter:D",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0))
    private void u25core$scaleAntimatterGain(TileSynchrotron self, double value, Operation<Void> original) {
        double gain = (value - antimatter) * 0.0002 * length * length * 0.25;
        original.call(self, Math.max(0, antimatter + gain));
    }

    // Antimatter put into an empty output slot
    @Inject(
        method = "power",
        at = @At(
            value = "FIELD",
            target = "Lnc/tile/accelerator/TileSynchrotron;antimatter:D",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1))
    private void u25core$scheduleDrain(CallbackInfo ci) {
        u25core$drainNextTick = true;
    }
}
