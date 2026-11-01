package net.sxmaa.u25core.mixins.late.enderio;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import crazypants.enderio.machine.obelisk.weather.EntityWeatherRocket;

/** The weather obelisk's rocket changes the weather when it dies at the top of its flight; the clouds open there. */
@Mixin(EntityWeatherRocket.class)
public abstract class WeatherRocketCloudMixin extends Entity {

    private WeatherRocketCloudMixin(World world) {
        super(world);
    }

    @Inject(method = "setDead", at = @At("HEAD"))
    private void u25core$openClouds(CallbackInfo ci) {
        if (!isDead) CloudCompat.skyColumn(worldObj, posX, posY, posZ);
    }
}
