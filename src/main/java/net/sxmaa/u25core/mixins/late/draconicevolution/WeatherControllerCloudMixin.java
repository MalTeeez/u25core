package net.sxmaa.u25core.mixins.late.draconicevolution;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.storage.WorldInfo;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.common.tileentities.TileWeatherController;

/** The clouds above the weather controller open up on its boom, the tick it starts changing the weather. */
@Mixin(value = TileWeatherController.class, remap = false)
public abstract class WeatherControllerCloudMixin extends TileEntity {

    @Shadow
    int tick;

    @Shadow
    boolean running;

    @Inject(method = "toggleDownfall", at = @At("HEAD"))
    private void u25core$openClouds(WorldInfo info, CallbackInfo ci) {
        if (running && tick == 81) CloudCompat.skyColumn(worldObj, xCoord + 0.5, yCoord + 1, zCoord + 0.5);
    }
}
