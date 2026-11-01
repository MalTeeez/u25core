package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.entity.Entity;
import net.sxmaa.u25core.clouds.CloudCompat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NuclearCraft's nuke and antimatter bomb clear the sky like other nuclear blasts; the antimatter bomb 3x as far. */
@Pseudo
@Mixin(targets = { "nc.entity.EntityNukePrimed", "nc.entity.EntityAntimatterBombPrimed" }, remap = false)
public abstract class BombCloudMixin {

    @Inject(method = "explode", at = @At("HEAD"))
    private void u25core$pushClouds(CallbackInfo ci) {
        Entity bomb = (Entity) (Object) this;
        float scale = bomb.getClass()
            .getSimpleName()
            .equals("EntityAntimatterBombPrimed") ? 3 : 1;
        CloudCompat.nuke(bomb.worldObj, bomb.posX, bomb.posY, bomb.posZ, scale);
    }
}
