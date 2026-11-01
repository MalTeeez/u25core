package net.sxmaa.u25core.mixins.late.clouds;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Techguns' rocket and nuke explosions are its own class. Targeted by name, Techguns is not on the classpath. */
@Pseudo
@Mixin(targets = "techguns.util.ProjectileExplosion", remap = false)
public abstract class TechgunsExplosionCloudMixin {

    @Shadow
    private World worldObj;

    @Shadow
    public double explosionX, explosionY, explosionZ;

    @Shadow
    public float explosionSize;

    @Inject(method = "doExplosion", at = @At("HEAD"))
    private void u25core$pushClouds(boolean particles, EntityLivingBase exploder, CallbackInfo ci) {
        CloudCompat.explosion(worldObj, explosionX, explosionY, explosionZ, explosionSize);
    }
}
