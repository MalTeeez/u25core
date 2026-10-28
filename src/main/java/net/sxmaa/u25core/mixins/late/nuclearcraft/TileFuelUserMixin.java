package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import nc.tile.machine.TileFuelUser;

// Burn times were saved as shorts, which overflows for fuels worth more than 32767 ticks (modded fuels at
// default config). getInteger also reads the old short tags.
@Mixin(value = TileFuelUser.class, remap = false)
public class TileFuelUserMixin {

    @Shadow
    public int burnTime;

    @Shadow
    public int currentItemBurnTime;

    @Inject(method = "readFromNBT", at = @At("TAIL"), remap = true)
    private void u25core$readInts(NBTTagCompound nbt, CallbackInfo ci) {
        burnTime = nbt.getInteger("BurnTime");
        currentItemBurnTime = nbt.getInteger("CurrentBurnTime");
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"), remap = true)
    private void u25core$writeInts(NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setInteger("BurnTime", burnTime);
        nbt.setInteger("CurrentBurnTime", currentItemBurnTime);
    }
}
