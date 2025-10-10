package net.sxmaa.u25core.mixin.late.enderio.extractupgrade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import crazypants.enderio.conduit.item.ItemConduit;

@Mixin(value = ItemConduit.class, remap = false)
public class ItemConduitMixin {

    @ModifyConstant(method = "updateFromNonUpgradeableVersion", constant = @Constant(intValue = 15, ordinal = 0))
    private int setExtractConduitConduitMaxStackSize(int value) {
        return 63;
    }
}
