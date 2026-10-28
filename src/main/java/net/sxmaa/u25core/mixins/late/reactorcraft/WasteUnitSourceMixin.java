package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import Reika.ReactorCraft.Base.TileEntityWasteUnit;

/** Stored waste keeps emitting, so waste storage needs shielding. */
@Mixin(value = TileEntityWasteUnit.class, remap = false)
public abstract class WasteUnitSourceMixin implements IRadiationSource {

    @Shadow
    public abstract int countWaste();

    @Override
    public double getRadiationEmission() {
        return countWaste() * RadiationConfig.reactorCraftWasteEmission;
    }
}
