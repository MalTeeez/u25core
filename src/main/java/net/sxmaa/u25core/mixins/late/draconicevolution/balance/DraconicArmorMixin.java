package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.brandon3055.draconicevolution.common.items.armor.DraconicArmor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(value = DraconicArmor.class, remap = false)
public class DraconicArmorMixin {

    // Max absorption against unblockable, absolute and magic damage
    @ModifyConstant(method = "getProperties", constant = @Constant(intValue = 15))
    private int u25core$raiseMaxAbsorb(int constant) {
        return 25;
    }

    @ModifyReturnValue(method = "getProtectionPoints", at = @At("RETURN"))
    private float u25core$lowerShieldCapacity(float points) {
        return points * 0.75F;
    }
}
