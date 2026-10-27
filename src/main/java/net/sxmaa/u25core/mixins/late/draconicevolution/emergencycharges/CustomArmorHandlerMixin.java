package net.sxmaa.u25core.mixins.late.draconicevolution.emergencycharges;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.common.items.armor.ArmorSummary;
import com.brandon3055.draconicevolution.common.items.armor.CustomArmorHandler;
import com.brandon3055.draconicevolution.common.items.armor.ICustomArmor;
import com.brandon3055.draconicevolution.common.utils.ItemNBTHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Draconic armor only saves the player from death while it has emergency charges, stored on the chestplate.
 * A charge is gained while the shield is near full (500+ points, under 15% entropy), at the cost of 80% of the
 * chestplate's max energy and maxing out its entropy.
 */
@Mixin(value = CustomArmorHandler.class, remap = false)
public class CustomArmorHandlerMixin {

    @Unique
    private static final String u25core$CHARGES = "EmergencyCharges";

    @Unique
    private static int u25core$getCharges(ArmorSummary summary) {
        ItemStack chest = summary.armorStacks[2];
        return chest == null ? 0 : ItemNBTHelper.getInteger(chest, u25core$CHARGES, 0);
    }

    @Inject(
        method = "onLivingDeath",
        at = @At(
            value = "FIELD",
            target = "Lcom/brandon3055/draconicevolution/common/handler/BalanceConfigHandler;draconicArmorBaseStorage:I",
            ordinal = 0),
        cancellable = true)
    private void u25core$requireCharge(LivingDeathEvent event, CallbackInfo ci, @Local ArmorSummary summary) {
        if (u25core$getCharges(summary) < 1) ci.cancel();
    }

    @ModifyArg(
        method = "onLivingDeath",
        at = @At(
            value = "INVOKE",
            target = "Lcofh/api/energy/IEnergyContainerItem;extractEnergy(Lnet/minecraft/item/ItemStack;IZ)I"),
        index = 1)
    private int u25core$quadrupleSaveCost(int amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount * 4L);
    }

    @WrapOperation(
        method = "onLivingDeath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;addChatComponentMessage(Lnet/minecraft/util/IChatComponent;)V",
            remap = true))
    private void u25core$consumeCharge(EntityPlayer player, IChatComponent message, Operation<Void> original,
        @Local ArmorSummary summary) {
        int left = u25core$getCharges(summary) - 1;
        ItemNBTHelper.setInteger(summary.armorStacks[2], u25core$CHARGES, left);
        original.call(player, message.appendText(" | Emergency shield charges left: " + left));
    }

    @Inject(
        method = "onPlayerTick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/brandon3055/draconicevolution/common/items/armor/CustomArmorHandler;tickArmorEffects(Lcom/brandon3055/draconicevolution/common/items/armor/ArmorSummary;Lnet/minecraft/entity/player/EntityPlayer;)V",
            shift = At.Shift.AFTER))
    private void u25core$gainCharge(TickEvent.PlayerTickEvent event, CallbackInfo ci, @Local ArmorSummary summary) {
        if (summary == null || event.player.worldObj.isRemote) return;
        ItemStack chest = summary.armorStacks[2];
        int charges = u25core$getCharges(summary);
        if (chest == null || summary.protectionPoints < 500 || charges >= 20 || summary.entropy >= 15.0F) return;

        ICustomArmor armor = (ICustomArmor) chest.getItem();
        int cost = (int) (armor.getMaxEnergyStored(chest) / 1.25F);
        if (armor.getEnergyStored(chest) <= cost) return;
        armor.extractEnergy(chest, cost, false);
        ItemNBTHelper.setFloat(chest, "ShieldEntropy", 100.0F);
        ItemNBTHelper.setInteger(chest, u25core$CHARGES, charges + 1);
    }
}
