package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.common.entity.EntityDragonProjectile;

@Mixin(value = EntityDragonProjectile.class, remap = false)
public class EntityDragonProjectileMixin {

    @Shadow
    private float heath;

    @Inject(
        method = "<init>(Lnet/minecraft/world/World;ILnet/minecraft/entity/EntityLivingBase;FLnet/minecraft/entity/Entity;)V",
        at = @At("RETURN"))
    private void u25core$raiseHealth(World world, int type, EntityLivingBase target, float power, Entity shooter,
        CallbackInfo ci) {
        heath = 15.0F;
    }
}
