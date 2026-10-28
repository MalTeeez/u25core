package net.sxmaa.u25core.mixins.late.forestry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.sxmaa.u25core.radiation.EntityRadiationData;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/** Radioactive bees irradiate nearby entities instead of hurting them directly. Forestry isn't a compile dependency. */
@Pseudo
@Mixin(targets = "forestry.apiculture.genetics.alleles.AlleleEffectRadioactive", remap = false)
public class RadioactiveBeeMixin {

    @WrapOperation(
        method = "doEffectThrottled",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
            remap = true))
    private boolean u25core$irradiate(EntityLivingBase entity, DamageSource source, float damage,
        Operation<Boolean> original) {
        // Held like a potion, the effect re-triggers on its throttle
        EntityRadiationData.hold(entity, RadiationConfig.forestryBeeRadiation, 100);
        return true;
    }
}
