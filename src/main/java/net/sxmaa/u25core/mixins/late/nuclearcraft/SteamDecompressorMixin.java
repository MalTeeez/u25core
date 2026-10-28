package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import nc.tile.generator.TileDenseSteamDecompressor;
import nc.tile.generator.TileSteamDecompressor;

@Mixin(value = { TileSteamDecompressor.class, TileDenseSteamDecompressor.class }, remap = false)
public class SteamDecompressorMixin {

    @Shadow(remap = false)
    public FluidTank tank;

    @Shadow(remap = false)
    public FluidTank tank2;

    // Both tanks were written into one shared compound, so tank2 overwrote tank (or an empty one wiped both)
    @Inject(method = "writeToNBT", at = @At("TAIL"), remap = true)
    private void u25core$writeTanksSeparately(NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tank2.writeToNBT(new NBTTagCompound()));
    }

    // Same bug in the description packet data
    @Inject(method = "writeFluid", at = @At("TAIL"))
    private void u25core$writeFluidTanksSeparately(NBTTagCompound nbt, CallbackInfo ci) {
        u25core$writeTanksSeparately(nbt, ci);
    }
}
