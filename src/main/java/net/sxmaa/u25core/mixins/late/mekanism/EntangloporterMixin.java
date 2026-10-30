package net.sxmaa.u25core.mixins.late.mekanism;

import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.util.EntangloporterBudget;
import net.sxmaa.u25core.util.TransferBudget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import mekanism.api.MekanismConfig.general;
import mekanism.common.tile.TileEntityQuantumEntangloporter;

/** Caps the energy one Entangloporter moves per tick in each direction. Spending is booked in ElectricBlockMixin. */
@Mixin(value = TileEntityQuantumEntangloporter.class, remap = false)
public class EntangloporterMixin implements EntangloporterBudget {

    @Unique
    private final TransferBudget u25core$input = new TransferBudget();
    @Unique
    private final TransferBudget u25core$output = new TransferBudget();

    @Override
    public TransferBudget u25core$input() {
        return u25core$input;
    }

    @Override
    public TransferBudget u25core$output() {
        return u25core$output;
    }

    // Every way out (cables, RF pulls, IC2) is bounded by this
    @ModifyReturnValue(method = "getMaxOutput", at = @At("RETURN"))
    private double u25core$capOutput(double max) {
        return Math.min(max, u25core$output.remaining(ModConfig.entangloporterRFPerTick * general.FROM_TE));
    }
}
