package net.sxmaa.u25core.mixins.late.mekanism;

import net.minecraft.tileentity.TileEntity;
import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import mekanism.common.tile.TileEntityElectrolyticSeparator;

@Mixin(value = TileEntityElectrolyticSeparator.class, remap = false)
public class ElectrolyticSeparatorMixin {

    // With AccurateHeavyWater, the filter upgrade captures heavy water while separating water. Only capture on Mars;
    // hasFilter() also gates draining, which is left alone so stored heavy water can still be taken out anywhere.
    @ModifyExpressionValue(
        method = "onUpdate",
        at = @At(value = "INVOKE", target = "Lmekanism/common/tile/TileEntityElectrolyticSeparator;hasFilter()Z"))
    private boolean u25core$onlyCaptureOnMars(boolean hasFilter) {
        return hasFilter
            && ((TileEntity) (Object) this).getWorldObj().provider.dimensionId == ModConfig.heavyWaterMarsDimension;
    }
}
