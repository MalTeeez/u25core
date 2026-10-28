package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import nc.tile.machine.TileAssembler;
import nc.tile.machine.TileMachineBase;
import nc.tile.machine.TileMachineFluidIn;
import nc.tile.machine.TileMachineFluidOut;

@Mixin(
    value = { TileMachineBase.class, TileMachineFluidIn.class, TileMachineFluidOut.class, TileAssembler.class },
    remap = false)
public class MachineCanCookMixin {

    // canCook returns true as soon as cookTime reaches the process time, skipping the recipe and output checks.
    // A speed upgrade added mid-cook gets there early, so outputs overstack or cookItem NPEs on a swapped input.
    @Inject(method = "canCook", at = @At("HEAD"), cancellable = true)
    private void u25core$checkOutputFirst(CallbackInfoReturnable<Boolean> cir) {
        TileMachineBase self = (TileMachineBase) (Object) this;
        if (self.cookTime < self.getProcessTime) return;
        for (int i = 0; i < self.inputSize; i++) {
            if (self.slots[i] == null) return;
        }
        ItemStack[] output = self.getOutput(self.inputs());
        boolean fits = output != null && output.length == self.outputSize;
        for (int j = 0; fits && j < self.outputSize; j++) {
            ItemStack slot = self.slots[j + self.inputSize];
            fits = output[j] != null && (slot == null
                || slot.isItemEqual(output[j]) && slot.stackSize + output[j].stackSize <= slot.getMaxStackSize());
        }
        if (!fits) {
            self.flag = false;
            cir.setReturnValue(false);
        }
    }
}
