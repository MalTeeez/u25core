package net.sxmaa.u25core.mixins.late.mekanism;

import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.util.EntangloporterBudget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import mekanism.api.MekanismConfig.general;
import mekanism.common.tile.TileEntityElectricBlock;

/**
 * The Entangloporter inherits its energy handling from here: clamps what enters it to its input budget, and books what
 * enters and leaves. Other electric blocks are untouched.
 */
@Mixin(value = TileEntityElectricBlock.class, remap = false)
public class ElectricBlockMixin {

    @Unique
    private EntangloporterBudget u25core$budget() {
        return (Object) this instanceof EntangloporterBudget ? (EntangloporterBudget) (Object) this : null;
    }

    @Unique
    private static double u25core$inputLeft(EntangloporterBudget budget) {
        return budget.u25core$input()
            .remaining(ModConfig.entangloporterRFPerTick * general.FROM_TE);
    }

    @ModifyVariable(method = "receiveEnergy", at = @At("HEAD"), argsOnly = true)
    private int u25core$clampRF(int maxReceive) {
        EntangloporterBudget budget = u25core$budget();
        return budget == null ? maxReceive : (int) Math.min(maxReceive, u25core$inputLeft(budget) * general.TO_TE);
    }

    @Inject(method = "receiveEnergy", at = @At("RETURN"))
    private void u25core$bookRF(ForgeDirection from, int maxReceive, boolean simulate,
        CallbackInfoReturnable<Integer> cir) {
        EntangloporterBudget budget = u25core$budget();
        if (budget != null && !simulate) budget.u25core$input()
            .use(cir.getReturnValue() * general.FROM_TE);
    }

    // Mekanism cables, and IC2 through injectEnergy
    @ModifyVariable(method = "transferEnergyToAcceptor", at = @At("HEAD"), argsOnly = true)
    private double u25core$clampJ(double amount) {
        EntangloporterBudget budget = u25core$budget();
        return budget == null ? amount : Math.min(amount, u25core$inputLeft(budget));
    }

    @Inject(method = "transferEnergyToAcceptor", at = @At("RETURN"))
    private void u25core$bookJ(ForgeDirection side, double amount, CallbackInfoReturnable<Double> cir) {
        EntangloporterBudget budget = u25core$budget();
        if (budget != null) budget.u25core$input()
            .use(cir.getReturnValue());
    }

    @ModifyReturnValue(method = "getDemandedEnergy", at = @At("RETURN"))
    private double u25core$clampDemand(double demand) {
        EntangloporterBudget budget = u25core$budget();
        return budget == null ? demand : Math.min(demand, u25core$inputLeft(budget) * general.TO_IC2);
    }

    @Inject(method = "extractEnergy", at = @At("RETURN"))
    private void u25core$bookExtract(ForgeDirection from, int maxExtract, boolean simulate,
        CallbackInfoReturnable<Integer> cir) {
        EntangloporterBudget budget = u25core$budget();
        if (budget != null && !simulate) budget.u25core$output()
            .use(cir.getReturnValue() * general.FROM_TE);
    }

    @Inject(method = "drawEnergy", at = @At("HEAD"))
    private void u25core$bookDraw(double amount, CallbackInfo ci) {
        EntangloporterBudget budget = u25core$budget();
        if (budget != null) budget.u25core$output()
            .use(amount * general.FROM_IC2);
    }
}
