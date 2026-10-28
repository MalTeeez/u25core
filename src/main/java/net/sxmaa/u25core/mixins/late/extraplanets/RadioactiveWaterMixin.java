package net.sxmaa.u25core.mixins.late.extraplanets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.sxmaa.u25core.radiation.EntityRadiationData;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Neptune's radioactive water also irradiates. ExtraPlanets isn't a compile dependency, so the obfuscated name of
 * onEntityCollidedWithBlock is used directly; it only exists in the reobfuscated production jar.
 */
@Pseudo
@Mixin(targets = "com.mjr.extraplanets.blocks.fluid.FluidRadioActiveWater", remap = false)
public class RadioactiveWaterMixin {

    @Inject(method = "func_149670_a", at = @At("TAIL"))
    private void u25core$irradiate(World world, int x, int y, int z, Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityLivingBase living && !world.isRemote) {
            EntityRadiationData.hold(living, RadiationConfig.radioactiveWaterRadiation, 40);
        }
    }
}
