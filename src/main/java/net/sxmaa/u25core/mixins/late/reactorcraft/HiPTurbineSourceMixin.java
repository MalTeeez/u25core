package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.sxmaa.u25core.util.ReactorSteamSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import Reika.DragonAPI.Instantiable.Data.Proportionality;
import Reika.ReactorCraft.Registry.ReactorType;
import Reika.ReactorCraft.TileEntities.PowerGen.TileEntityHiPTurbine;

/** Remembers which reactors the HiP turbine's steam came from, as its steam line reports when it takes some in. */
@Mixin(value = TileEntityHiPTurbine.class, remap = false)
public abstract class HiPTurbineSourceMixin implements ReactorSteamSource {

    @Unique
    private double u25core$steamMultiplier = 1;

    @ModifyExpressionValue(
        method = "intakeSteam",
        at = @At(
            value = "INVOKE",
            target = "LReika/ReactorCraft/TileEntities/PowerGen/TileEntitySteamLine;getSourceReactorType()LReika/DragonAPI/Instantiable/Data/Proportionality;"))
    private Proportionality<ReactorType> u25core$readSource(Proportionality<ReactorType> source) {
        if (source != null && !source.isEmpty()) {
            double m = 0;
            for (ReactorType type : source.getElements()) {
                m += source.getFraction(type) * ReactorSteamSource.multiplierFor(type);
            }
            u25core$steamMultiplier = m;
        }
        return source;
    }

    @Override
    public double u25core$steamMultiplier() {
        return u25core$steamMultiplier;
    }
}
