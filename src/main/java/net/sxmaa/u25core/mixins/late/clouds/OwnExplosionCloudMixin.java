package net.sxmaa.u25core.mixins.late.clouds;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;
import net.sxmaa.u25core.mixins.early.clouds.ExplosionAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Explosions that replace doExplosionB without calling vanilla's, so ExplosionCloudMixin misses them: Tinkers' mining
 * charges and Thaumic Horizons' alchemite. Targeted by name, neither mod is on the classpath.
 */
@Pseudo
@Mixin(
    targets = { "tconstruct.world.MiningExplosion", "com.kentington.thaumichorizons.common.lib.ExplosionAlchemite" },
    remap = false)
public abstract class OwnExplosionCloudMixin extends Explosion {

    private OwnExplosionCloudMixin(World world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, x, y, z, size);
    }

    // Deobfuscated name in dev, SRG name in the pack
    @Inject(method = { "doExplosionB", "func_77279_a" }, at = @At("HEAD"))
    private void u25core$pushClouds(boolean particles, CallbackInfo ci) {
        if (particles) CloudCompat.explosion(
            ((ExplosionAccessor) this).u25core$getWorld(),
            explosionX,
            explosionY,
            explosionZ,
            explosionSize);
    }
}
