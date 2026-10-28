package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import nc.tile.accelerator.TileSuperElectromagnet;
import nc.tile.accelerator.TileSupercooler;
import nc.tile.generator.TileElectromagnet;

@Mixin(value = { TileSupercooler.class, TileSuperElectromagnet.class, TileElectromagnet.class }, remap = false)
public class SidedSlotsMixin {

    // These return null, which crashes hoppers (they read .length on it)
    @ModifyReturnValue(method = "getAccessibleSlotsFromSide", at = @At("RETURN"), remap = true)
    private int[] u25core$noSlots(int[] slots) {
        return slots == null ? new int[0] : slots;
    }
}
