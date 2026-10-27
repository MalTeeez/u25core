package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnderResurrection;

@Mixin(value = TileEnderResurrection.class, remap = false)
public class TileEnderResurrectionMixin {

    @ModifyConstant(method = "spawn", constant = @Constant(doubleValue = 200.0D))
    private double u25core$raiseBaseHealth(double constant) {
        return 1500.0D;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(doubleValue = 50.0D))
    private double u25core$raiseHealthPerLevel(double constant) {
        return 57.0D;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(floatValue = 5.0F))
    private float u25core$raiseAttackPerLevel(float constant) {
        return 15.0F;
    }
}
