package net.sxmaa.u25core.mixins.late.ic2;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.sxmaa.u25core.api.radiation.RadiationAPI;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ic2.core.ExplosionIC2;

/** Nuclear IC2 explosions (nukes, reactor meltdowns) leave contamination behind. */
@Mixin(value = ExplosionIC2.class, remap = false)
public abstract class ExplosionIC2RadiationMixin extends Explosion {

    /** Power of a default IC2 nuke, contamination scales relative to it. */
    private static final float u25core$NUKE_POWER = 35;

    @Shadow
    @Final
    private World worldObj;

    @Shadow
    @Final
    private float power;

    @Shadow
    protected abstract boolean isNuclear();

    private ExplosionIC2RadiationMixin(World world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, x, y, z, size);
    }

    @Inject(method = "doExplosion", at = @At("TAIL"))
    private void u25core$contaminate(CallbackInfo ci) {
        if (!isNuclear()) return;
        RadiationAPI.contaminate(
            worldObj,
            explosionX,
            explosionY,
            explosionZ,
            RadiationConfig.ic2NukeContamination * power / u25core$NUKE_POWER);
    }
}
