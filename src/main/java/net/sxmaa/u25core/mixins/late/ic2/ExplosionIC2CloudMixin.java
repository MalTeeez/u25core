package net.sxmaa.u25core.mixins.late.ic2;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ic2.core.ExplosionIC2;

/**
 * IC2 explosions (nukes and reactor meltdowns are nuclear, ITNT is not) skip vanilla's explosion packet, so they push
 * the clouds here.
 */
@Mixin(value = ExplosionIC2.class, remap = false)
public abstract class ExplosionIC2CloudMixin extends Explosion {

    @Shadow
    @Final
    private World worldObj;

    @Shadow
    @Final
    private float power;

    @Shadow
    private boolean isNuclear() {
        return false;
    }

    private ExplosionIC2CloudMixin(World world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, x, y, z, size);
    }

    @Inject(method = "doExplosion", at = @At("TAIL"))
    private void u25core$pushClouds(CallbackInfo ci) {
        // A full nuke (64 ITNT and a stack of plutonium) hits IC2's power limit of 120, twice a DefenseTech nuke
        if (isNuclear()) CloudCompat.nuke(worldObj, explosionX, explosionY, explosionZ, power / 60);
        else CloudCompat.explosion(worldObj, explosionX, explosionY, explosionZ, power);
    }
}
