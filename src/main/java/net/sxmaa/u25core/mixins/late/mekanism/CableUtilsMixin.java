package net.sxmaa.u25core.mixins.late.mekanism;

import net.sxmaa.u25core.util.EntangloporterBudget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.util.CableUtils;

/** Books what an Entangloporter pushes out to its neighbours. */
@Mixin(value = CableUtils.class, remap = false)
public class CableUtilsMixin {

    @WrapOperation(
        method = "emit",
        at = @At(value = "INVOKE", target = "Lmekanism/common/base/IEnergyWrapper;setEnergy(D)V"))
    private static void u25core$bookEmit(IEnergyWrapper emitter, double energy, Operation<Void> original) {
        if (emitter instanceof EntangloporterBudget) ((EntangloporterBudget) emitter).u25core$output()
            .use(emitter.getEnergy() - energy);
        original.call(emitter, energy);
    }
}
