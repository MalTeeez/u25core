package net.sxmaa.u25core.mixins.late.draconicevolution.balance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.brandon3055.draconicevolution.common.items.armor.DraconicArmor;
import com.brandon3055.draconicevolution.common.items.armor.WyvernArmor;
import com.brandon3055.draconicevolution.common.items.weapons.DraconicBow;
import com.brandon3055.draconicevolution.common.items.weapons.DraconicSword;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(value = { DraconicArmor.class, WyvernArmor.class, DraconicBow.class, DraconicSword.class }, remap = false)
public class UpgradeTierMixin {

    @ModifyReturnValue(method = "getMaxTier", at = @At("RETURN"))
    private int u25core$allowNextCoreTier(int tier) {
        return tier + 1;
    }
}
