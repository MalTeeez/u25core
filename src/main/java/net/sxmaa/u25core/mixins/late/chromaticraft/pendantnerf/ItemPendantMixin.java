package net.sxmaa.u25core.mixins.late.chromaticraft.pendantnerf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import Reika.ChromatiCraft.Items.Tools.ItemPendant;

@Mixin(value = ItemPendant.class, remap = false)
public class ItemPendantMixin {

    @ModifyConstant(method = "onUpdate", constant = @Constant(intValue = 2, ordinal = 0))
    public int lowerPotionLevel(int constant) {
        return 1;
    }

    @ModifyConstant(method = "onUpdate", constant = @Constant(intValue = 6000, ordinal = 0))
    public int lowerPotionLength(int constant) {
        return 600;
    }
}
