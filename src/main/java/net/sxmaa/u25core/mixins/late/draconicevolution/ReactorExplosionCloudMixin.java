package net.sxmaa.u25core.mixins.late.draconicevolution;

import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.reactor.ReactorExplosion;

/** The reactor's explosion is Draconic's own and reaches about power * 10 blocks; it pushes the clouds here. */
@Mixin(value = ReactorExplosion.class, remap = false)
public abstract class ReactorExplosionCloudMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void u25core$pushClouds(World world, int x, int y, int z, float power, CallbackInfo ci) {
        CloudCompat.explosion(world, x + 0.5, y + 0.5, z + 0.5, power * 5);
    }
}
