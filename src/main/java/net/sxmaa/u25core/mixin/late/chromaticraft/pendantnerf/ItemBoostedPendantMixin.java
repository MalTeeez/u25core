package net.sxmaa.u25core.mixin.late.chromaticraft.pendantnerf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import Reika.ChromatiCraft.Items.Tools.ItemBoostedPendant;

@Mixin(value = ItemBoostedPendant.class, remap = false)
public class ItemBoostedPendantMixin {

    @WrapOperation(
        method = "onTick",
        at = @At(
            value = "INVOKE",
            target = "LReika/ChromatiCraft/Items/Tools/ItemBoostedPendant;getChargeConsumptionRate(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)I"))
    public int increaseChargeConsumption(ItemBoostedPendant instance, EntityPlayer e, World world, ItemStack is,
        Operation<Integer> original) {
        return original.call(instance, e, world, is) * 400;
    }
}
