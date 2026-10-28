package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import nc.handler.IEntityBomb;
import nc.tile.generator.TileFusionReactor;
import nc.tile.generator.TileFusionReactorSteam;

@Mixin(value = { TileFusionReactor.class, TileFusionReactorSteam.class }, remap = false)
public class FusionClientExplosionMixin {

    // The random explosion from plasma touching the core also ran on the client, blowing up blocks client side only
    @WrapWithCondition(
        method = "updateEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnc/handler/NCExplosion;createExplosion(Lnc/handler/IEntityBomb;Lnet/minecraft/world/World;DDDFFZ)V",
            remap = false),
        remap = true)
    private boolean u25core$onlyExplodeOnServer(IEntityBomb bomb, World world, double x, double y, double z, float size,
        float power, boolean flag) {
        return !world.isRemote;
    }
}
