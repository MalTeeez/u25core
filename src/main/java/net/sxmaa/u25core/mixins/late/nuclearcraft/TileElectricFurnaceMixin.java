package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import nc.tile.machine.TileElectricFurnace;

@Mixin(value = TileElectricFurnace.class, remap = false)
public class TileElectricFurnaceMixin {

    // Same early cookTime shortcut as MachineCanCookMixin, single input and output slot
    @Inject(method = "canCook", at = @At("HEAD"), cancellable = true)
    private void u25core$checkOutputFirst(CallbackInfoReturnable<Boolean> cir) {
        TileElectricFurnace self = (TileElectricFurnace) (Object) this;
        if (self.cookTime < self.getFurnaceSpeed || self.slots[0] == null) return;
        ItemStack output = self.getOutput(self.slots[0]);
        ItemStack slot = self.slots[1];
        if (output == null || slot != null
            && (!slot.isItemEqual(output) || slot.stackSize + output.stackSize > slot.getMaxStackSize())) {
            self.flag = false;
            cir.setReturnValue(false);
        }
    }
}
