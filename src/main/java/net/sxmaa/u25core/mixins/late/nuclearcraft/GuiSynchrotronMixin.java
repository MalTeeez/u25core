package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import nc.gui.accelerator.GuiSynchrotron;
import nc.tile.accelerator.TileSynchrotron;

@Mixin(value = GuiSynchrotron.class, remap = false)
public class GuiSynchrotronMixin {

    @Shadow
    public TileSynchrotron entity;

    // The bar is drawn as energy * 109 / 1000000. Rescale energy so the bar fills at 5% of the raised capacity,
    // and clamp it, since energy * 109 overflows an int at that capacity.
    @ModifyExpressionValue(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(value = "FIELD", target = "Lnc/tile/accelerator/TileSynchrotron;energy:I", remap = false),
        remap = true)
    private int u25core$rescaleEnergyBar(int energy) {
        long full = (long) (entity.storage.getMaxEnergyStored() * 0.05);
        return (int) Math.min(1_000_000L, energy * 1_000_000L / full);
    }
}
