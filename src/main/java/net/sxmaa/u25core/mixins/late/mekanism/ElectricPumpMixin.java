package net.sxmaa.u25core.mixins.late.mekanism;

import net.minecraft.tileentity.TileEntity;
import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import mekanism.common.tile.TileEntityElectricPump;

@Mixin(value = TileEntityElectricPump.class, remap = false)
public class ElectricPumpMixin {

    // Without AccurateHeavyWater, the filter upgrade makes the pump suck heavy water. Only on Mars, plain water
    // elsewhere.
    @ModifyReturnValue(method = "hasFilter", at = @At("RETURN"))
    private boolean u25core$onlyOnMars(boolean hasFilter) {
        return hasFilter
            && ((TileEntity) (Object) this).getWorldObj().provider.dimensionId == ModConfig.heavyWaterMarsDimension;
    }
}
