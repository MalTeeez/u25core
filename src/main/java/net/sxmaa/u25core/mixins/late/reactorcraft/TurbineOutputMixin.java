package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.util.ReactorSteamSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import Reika.ReactorCraft.TileEntities.PowerGen.TileEntityTurbineCore;

/**
 * Scales a ReactorCraft turbine's torque and power, which feed both its generator and RotaryCraft shafts; speed is
 * left alone. The HiP turbine scales by where its steam came from, the others (fed by steam blocks, which don't record
 * a source) by reactorTurbineMultiplier.
 */
@Mixin(value = TileEntityTurbineCore.class, remap = false)
public abstract class TurbineOutputMixin {

    @ModifyReturnValue(method = "getTorque", at = @At("RETURN"))
    private int u25core$scaleTorque(int torque) {
        return (int) (torque * u25core$multiplier());
    }

    @ModifyReturnValue(method = "getPower", at = @At("RETURN"))
    private long u25core$scalePower(long power) {
        return (long) (power * u25core$multiplier());
    }

    @Unique
    private double u25core$multiplier() {
        Object self = this;
        return self instanceof ReactorSteamSource ? ((ReactorSteamSource) self).u25core$steamMultiplier()
            : ModConfig.reactorTurbineMultiplier;
    }
}
