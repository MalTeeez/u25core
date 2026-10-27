package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.brandon3055.draconicevolution.common.entity.EntityChaosGuardian;

@Mixin(value = EntityChaosGuardian.class, remap = false)
public class EntityChaosGuardianMixin {

    @ModifyConstant(method = "applyEntityAttributes", constant = @Constant(doubleValue = 2000.0D), remap = true)
    private double u25core$raiseMaxHealth(double constant) {
        return 5000.0D;
    }

    // Base power of the firebomb, fireball charge and the three chaser attacks
    @ModifyConstant(
        method = "updateAttack",
        constant = { @Constant(floatValue = 5.0F, ordinal = 0), @Constant(floatValue = 5.0F, ordinal = 1),
            @Constant(floatValue = 5.0F, ordinal = 2), @Constant(floatValue = 5.0F, ordinal = 3),
            @Constant(floatValue = 5.0F, ordinal = 4) })
    private float u25core$raiseProjectilePower(float constant) {
        return 12.0F;
    }

    @ModifyConstant(method = "updateAttack", constant = @Constant(floatValue = 5.0F, ordinal = 5))
    private float u25core$raiseTeleportPower(float constant) {
        return 20.0F;
    }

    @ModifyConstant(method = "updateAttack", constant = @Constant(floatValue = 8.0F, ordinal = 2))
    private float u25core$raiseTeleportPowerSpread(float constant) {
        return 12.0F;
    }

    @ModifyConstant(method = "updateDragonEnderCrystal", constant = @Constant(floatValue = 10.0F))
    private float u25core$raiseCrystalDestroyedDamage(float constant) {
        return 25.0F;
    }
}
