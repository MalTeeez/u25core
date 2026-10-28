package net.sxmaa.u25core.mixins.early.radiation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.sxmaa.u25core.radiation.EntityRadiationData;
import net.sxmaa.u25core.radiation.RadiationRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns other mods' radiation potions (IC2 and whatever is configured) into dose. The rate is held for the potion's
 * duration, so mods re-applying their potion every tick don't stack it up.
 */
@Mixin(EntityLivingBase.class)
public class RadiationPotionMixin {

    @Inject(method = "addPotionEffect", at = @At("HEAD"), cancellable = true)
    private void u25core$convertRadiationPotion(PotionEffect effect, CallbackInfo ci) {
        double rate = RadiationRegistry.getPotionRate(effect.getPotionID());
        if (rate <= 0) return;
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        EntityRadiationData.hold(self, rate * (effect.getAmplifier() + 1), effect.getDuration());
        ci.cancel();
    }
}
