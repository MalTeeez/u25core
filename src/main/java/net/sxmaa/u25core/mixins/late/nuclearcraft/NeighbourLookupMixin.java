package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import nc.tile.generator.TileContinuousBase;
import nc.tile.generator.TileDenseSteamDecompressor;
import nc.tile.generator.TileFusionReactor;
import nc.tile.generator.TileFusionReactorSteam;
import nc.tile.generator.TileGeneratorInventory;
import nc.tile.generator.TileSteamDecompressor;
import nc.tile.storage.TileStorage;
import nc.tile.storage.TileStorageInventory;

@Mixin(
    value = { TileGeneratorInventory.class, TileContinuousBase.class, TileStorage.class, TileStorageInventory.class,
        TileSteamDecompressor.class, TileDenseSteamDecompressor.class, TileFusionReactor.class,
        TileFusionReactorSteam.class },
    remap = false)
public class NeighbourLookupMixin {

    // Pushing energy/steam to neighbours every tick loaded the neighbouring chunk when it wasn't loaded
    @WrapOperation(
        method = { "addEnergy", "addSteam" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getTileEntity(III)Lnet/minecraft/tileentity/TileEntity;",
            remap = true))
    private TileEntity u25core$skipUnloaded(World world, int x, int y, int z, Operation<TileEntity> original) {
        return world.blockExists(x, y, z) ? original.call(world, x, y, z) : null;
    }
}
