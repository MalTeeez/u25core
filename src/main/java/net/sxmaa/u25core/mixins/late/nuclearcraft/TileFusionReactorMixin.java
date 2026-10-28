package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import nc.handler.BombType;
import nc.handler.IEntityBomb;
import nc.tile.generator.TileFusionReactor;
import nc.tile.generator.TileFusionReactorSteam;

@Mixin(value = { TileFusionReactor.class, TileFusionReactorSteam.class }, remap = false)
public class TileFusionReactorMixin {

    @Shadow(remap = false)
    public double heat;

    @Unique
    private int u25core$overheatCount;

    @Inject(method = "overheat", at = @At("HEAD"))
    private void u25core$resetOverheatCount(World world, double x, double y, double z, float radius, BombType type,
        CallbackInfo ci) {
        if (heat <= 20000) u25core$overheatCount = 0;
    }

    // Only melt down after overheating 12 ticks in a row
    @WrapOperation(
        method = "overheat",
        at = @At(
            value = "INVOKE",
            target = "Lnc/handler/NCExplosion;createExplosion(Lnc/handler/IEntityBomb;Lnet/minecraft/world/World;DDDFFZ)V"))
    private void u25core$delayMeltdown(IEntityBomb bomb, World world, double x, double y, double z, float size,
        float power, boolean flag, Operation<Void> original) {
        if (u25core$overheatCount++ < 12) return;
        original.call(bomb, world, x, y, z, size, power, flag);
        u25core$overheatCount = 0;
    }
}
