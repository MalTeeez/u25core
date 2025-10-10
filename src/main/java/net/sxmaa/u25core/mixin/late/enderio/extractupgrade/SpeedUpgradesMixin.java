package net.sxmaa.u25core.mixin.late.enderio.extractupgrade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import crazypants.enderio.conduit.item.SpeedUpgrade;

@Mixin(value = SpeedUpgrade.class, remap = false)
public class SpeedUpgradesMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 15, ordinal = 0))
    private static int setExtractConduitConduitMaxStackSize(int value) {
        return 63;
    }
}
