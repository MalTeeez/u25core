package net.sxmaa.u25core.mixins.late.enderio.extractupgrade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import crazypants.enderio.conduit.gui.item.InventoryUpgrades;

@Mixin(value = InventoryUpgrades.class, remap = false)
public class InventoryUpgradesMixin {

    @ModifyConstant(method = "getInventoryStackLimit", constant = @Constant(intValue = 15, ordinal = 0))
    public int setExtractConduitConduitMaxStackSize(int value) {
        return 63;
    }
}
