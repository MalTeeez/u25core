package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import nc.handler.AnvilRepairHandler;

@Mixin(value = AnvilRepairHandler.class, remap = false)
public class AnvilRepairHandlerMixin {

    // Checks are `stack.getItem() == new ItemStack(material, 1, meta).getItem()`, and all NC materials share one
    // Item. Hide the expected item when the metadata differs, so only the right material matches.
    @WrapOperation(
        method = "isValidRepairItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
            remap = true))
    private Item u25core$matchMetadata(ItemStack receiver, Operation<Item> original,
        @Local(argsOnly = true) ItemStack stack) {
        if (receiver != stack && receiver.getItemDamage() != stack.getItemDamage()) return null;
        return original.call(receiver);
    }

    // The anvil name is null until the client sends one
    @WrapOperation(method = "onAnvilChange", at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z"))
    private boolean u25core$nullNameIsEmpty(String name, Operation<Boolean> original) {
        return name == null || original.call(name);
    }
}
