package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.common.entity.EntityCustomDragon;

@Mixin(value = EntityCustomDragon.class, remap = false)
public class EntityCustomDragonMixin {

    @Shadow
    public float attackDamage;

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void u25core$raiseAttackDamage(World world, CallbackInfo ci) {
        attackDamage = 22.0F;
    }

    @ModifyConstant(method = "applyEntityAttributes", constant = @Constant(doubleValue = 200.0D), remap = true)
    private double u25core$raiseMaxHealth(double constant) {
        return 1750.0D;
    }
}
