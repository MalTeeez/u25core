package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.minecraft.entity.Entity;
import net.sxmaa.u25core.api.radiation.RadiationAPI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import Reika.ReactorCraft.Auxiliary.RadiationEffects;
import Reika.ReactorCraft.Entities.EntityRadiation;

/** Radiation clouds from older saves turn into U25Core contamination on their first tick. */
@Mixin(value = EntityRadiation.class, remap = false)
public class EntityRadiationMixin {

    @Shadow
    private RadiationEffects.RadiationIntensity intensity;

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true, remap = true)
    private void u25core$convert(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.worldObj.isRemote) {
            double svPerHour = intensity == null || intensity.ordinal() == 0 ? 0
                : Math.pow(10, intensity.ordinal() - 3);
            RadiationAPI.contaminate(self.worldObj, self.posX, self.posY, self.posZ, svPerHour);
            self.setDead();
        }
        ci.cancel();
    }
}
