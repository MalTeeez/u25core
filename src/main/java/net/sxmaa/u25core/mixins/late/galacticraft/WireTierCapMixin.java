package net.sxmaa.u25core.mixins.late.galacticraft;

import java.lang.reflect.Field;

import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Caps what an aluminium wire network hands each acceptor per tick, by the network's wire tier (1 if any plain wire
 * is in it, 2 if all heavy). The cap is applied to the request {@code e} right where doTickStartCalc first reads it,
 * so totalRequested, the storage excess and doProduce's per-acceptor sends all see the capped value. The cap is set
 * in RF delivered, converted with Galacticraft's live gJ to RF ratio (loss factor included). Galacticraft isn't a
 * compile dependency.
 */
@Pseudo
@Mixin(targets = "micdoodle8.mods.galacticraft.core.energy.grid.EnergyNetwork", remap = false)
public abstract class WireTierCapMixin {

    @Unique
    private static Field u25core$toRF;

    @Shadow(remap = false)
    public int networkTierGC;

    @ModifyVariable(method = "doTickStartCalc", at = @At(value = "LOAD", ordinal = 0))
    private float u25core$capRequest(float e) {
        int rf = networkTierGC == 2 ? ModConfig.gcWireTier2RFPerTick : ModConfig.gcWireTier1RFPerTick;
        return rf > 0 ? Math.min(e, rf / u25core$toRFRatio()) : e;
    }

    @Unique
    private static float u25core$toRFRatio() {
        try {
            if (u25core$toRF == null)
                u25core$toRF = Class.forName("micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler")
                    .getField("TO_RF_RATIO");
            return u25core$toRF.getFloat(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Galacticraft's EnergyConfigHandler.TO_RF_RATIO is gone", ex);
        }
    }
}
