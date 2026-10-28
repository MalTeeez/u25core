package net.sxmaa.u25core.mixins.late.nuclearcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import nc.tile.generator.TileElectromagnet;

@Mixin(value = TileElectromagnet.class, remap = false)
public class TileElectromagnetMixin {

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lcofh/api/energy/EnergyStorage;<init>(II)V"),
        index = 0)
    private int u25core$raiseCapacity(int capacity) {
        return capacity * 20;
    }
}
