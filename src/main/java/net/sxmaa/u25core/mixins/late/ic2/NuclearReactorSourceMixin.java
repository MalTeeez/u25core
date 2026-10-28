package net.sxmaa.u25core.mixins.late.ic2;

import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

/** Running IC2 reactors emit in proportion to their output. */
@Mixin(value = TileEntityNuclearReactorElectric.class, remap = false)
public abstract class NuclearReactorSourceMixin implements IRadiationSource {

    @Shadow
    public float output;

    @Override
    public double getRadiationEmission() {
        return Math.max(0, output) * RadiationConfig.ic2ReactorEmission;
    }
}
