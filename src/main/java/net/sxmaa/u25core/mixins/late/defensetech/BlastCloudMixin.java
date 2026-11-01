package net.sxmaa.u25core.mixins.late.defensetech;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * DefenseTech blasts skip vanilla's explosion packet, so they push the clouds here. Their size is a radius in blocks,
 * about twice vanilla's strength. Targeted by name, DefenseTech is only on the runtime classpath.
 */
@Pseudo
@Mixin(targets = "defense.common.explosive.blast.Blast", remap = false)
public abstract class BlastCloudMixin extends Explosion {

    @Shadow
    public World worldObj;

    private BlastCloudMixin(World world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, x, y, z, size);
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void u25core$pushClouds(CallbackInfo ci) {
        switch (getClass().getSimpleName()) {
            // Gas, EMP, regrowth and rot don't go bang
            case "BlastChemical", "BlastEMP", "BlastRegen", "BlastMutation", "BlastRot" -> {}
            default -> CloudCompat.explosion(worldObj, explosionX, explosionY, explosionZ, explosionSize / 2);
        }
    }
}
