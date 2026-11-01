package net.sxmaa.u25core.mixins.early.clouds;

import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mods that build their own Explosion on the server call doExplosionB(true), vanilla's server path passes false and
 * sends its own explosion packet instead. Those explosions (e.g. ReactorCraft's hydrogen explosions) push the clouds
 * here.
 */
@Mixin(Explosion.class)
public abstract class ExplosionCloudMixin {

    @Shadow
    @Final
    private World worldObj;

    @Shadow
    public double explosionX, explosionY, explosionZ;

    @Shadow
    public float explosionSize;

    @Inject(method = "doExplosionB", at = @At("HEAD"))
    private void u25core$pushClouds(boolean particles, CallbackInfo ci) {
        if (particles) CloudCompat.explosion(worldObj, explosionX, explosionY, explosionZ, explosionSize);
    }
}
