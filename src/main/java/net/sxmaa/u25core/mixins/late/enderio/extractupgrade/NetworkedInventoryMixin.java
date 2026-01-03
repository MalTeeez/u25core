package net.sxmaa.u25core.mixins.late.enderio.extractupgrade;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import crazypants.enderio.conduit.item.IItemConduit;
import crazypants.enderio.conduit.item.NetworkedInventory;

@Mixin(value = NetworkedInventory.class, remap = false)
public abstract class NetworkedInventoryMixin {

    @Unique
    int u25core$itemExtractsLeft;
    @Unique
    boolean u25core$transferredItem;

    @Shadow
    abstract int insertIntoTargets(ItemStack toExtract);

    @Shadow
    public abstract void itemExtracted(int slot, int numInserted);

    @WrapOperation(
        method = "transferItems",
        at = @At(
            value = "INVOKE",
            target = "Lcrazypants/enderio/conduit/item/NetworkedInventory;doTransfer(Lnet/minecraft/item/ItemStack;II)Z"))
    public boolean needToDoTransfer(NetworkedInventory instance, ItemStack extractItem, int slot, int maxExtract,
        Operation<Boolean> original) {
        if (maxExtract <= 0) return false;

        int transferredItems = u25core$doTransferInt(extractItem, slot, Math.min(maxExtract, u25core$itemExtractsLeft));
        if (transferredItems > 0) u25core$transferredItem = true;
        u25core$itemExtractsLeft -= transferredItems;
        // Have we transferred all items we could with the extract capacity we have (for this inv)?
        return u25core$itemExtractsLeft <= 0;
    }

    @WrapOperation(
        method = "transferItems",
        at = @At(
            value = "INVOKE",
            target = "Lcrazypants/enderio/conduit/item/IItemConduit;getMaximumExtracted(Lnet/minecraftforge/common/util/ForgeDirection;)I"))
    public int storeInitialMaxExtractedItems(IItemConduit instance, ForgeDirection forgeDirection,
        Operation<Integer> original) {
        u25core$itemExtractsLeft = original.call(instance, forgeDirection);
        u25core$transferredItem = false;
        return u25core$itemExtractsLeft;
    }

    @Inject(method = "transferItems", at = @At("RETURN"), cancellable = true)
    private void returnTrueIfOutOfSlots(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && u25core$transferredItem) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    public int u25core$doTransferInt(ItemStack extractedItem, int slot, int maxExtract) {
        if (extractedItem == null || extractedItem.getItem() == null) {
            return 0;
        }
        ItemStack toExtract = extractedItem.copy();
        toExtract.stackSize = Math.min(maxExtract, toExtract.stackSize);
        int numInserted = this.insertIntoTargets(toExtract);
        if (numInserted <= 0) {
            return 0;
        }
        this.itemExtracted(slot, numInserted);
        return numInserted;
    }

}
