package net.sxmaa.u25core.mixins.early.endportal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.sxmaa.u25core.common.EndPortalActivator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Eyes of ender no longer go into End portal frames when endPortalActivator is set; that item does instead. */
@Mixin(ItemEnderEye.class)
public class ItemEnderEyeMixin {

    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void u25core$blockFrame(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (!EndPortalActivator.blocksEye(world, x, y, z)) return;
        if (!world.isRemote) EndPortalActivator.hint(player);
        cir.setReturnValue(false);
    }
}
