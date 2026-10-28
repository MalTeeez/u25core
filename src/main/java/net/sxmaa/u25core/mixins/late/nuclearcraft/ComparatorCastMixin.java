package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import nc.block.generator.BlockDenseSteamDecompressor;
import nc.block.generator.BlockSolarPanel;
import nc.block.generator.BlockSteamDecompressor;
import nc.block.generator.BlockSteamGenerator;

@Mixin(
    value = { BlockSteamDecompressor.class, BlockDenseSteamDecompressor.class, BlockSolarPanel.class,
        BlockSteamGenerator.class },
    remap = false)
public class ComparatorCastMixin {

    // These cast their tile to IInventory for the comparator output, but the tiles aren't inventories, so a
    // comparator next to them crashed the server with a ClassCastException
    @Inject(method = "getComparatorInputOverride", at = @At("HEAD"), cancellable = true, remap = true)
    private void u25core$noInventoryNoSignal(World world, int x, int y, int z, int side,
        CallbackInfoReturnable<Integer> cir) {
        if (!(world.getTileEntity(x, y, z) instanceof IInventory)) cir.setReturnValue(0);
    }
}
