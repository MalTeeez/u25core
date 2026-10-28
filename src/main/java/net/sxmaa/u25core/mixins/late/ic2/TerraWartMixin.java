package net.sxmaa.u25core.mixins.late.ic2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.sxmaa.u25core.radiation.EntityRadiationData;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ic2.core.item.ItemTerraWart;

/** Terra Wart cured IC2's radiation potion, which is now dose, so it treats dose instead. */
@Mixin(value = ItemTerraWart.class, remap = false)
public class TerraWartMixin {

    @Inject(method = "onEaten", at = @At("TAIL"), remap = true)
    private void u25core$treatDose(ItemStack stack, World world, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        EntityRadiationData data = EntityRadiationData.get(player);
        if (!world.isRemote && data != null) data.treatment += RadiationConfig.terraWartTreatment;
    }
}
