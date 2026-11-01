package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cofh.api.energy.EnergyStorage;

/**
 * NuclearCraft's storage blocks transfer up to their whole capacity per tick; caps the lithium-ion battery block's
 * input and output. NuclearCraft isn't a compile dependency, and storage is declared on TileStorage.
 */
@Pseudo
@Mixin(targets = "nc.tile.storage.TileStorage", remap = false)
public abstract class LiIonBatteryTransferMixin {

    @Shadow
    public EnergyStorage storage;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void u25core$capTransfer(int energyMax, CallbackInfo ci) {
        if (getClass().getName()
            .equals("nc.tile.storage.TileLithiumIonBattery")) storage.setMaxTransfer(ModConfig.ncLiIonBlockRFPerTick);
    }
}
