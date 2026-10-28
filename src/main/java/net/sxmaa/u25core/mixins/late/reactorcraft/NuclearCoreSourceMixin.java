package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.radiation.RadiationConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import Reika.ReactorCraft.Base.TileEntityNuclearCore;

/** Fuel cores emit while active (two minutes after a neutron hit), scaled by what's inside them. */
@Mixin(value = TileEntityNuclearCore.class, remap = false)
public abstract class NuclearCoreSourceMixin implements IRadiationSource {

    @Shadow
    public abstract boolean isActive();

    @Override
    public double getRadiationEmission() {
        if (!isActive()) return 0;
        IInventory inventory = (IInventory) this;
        int items = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) items += stack.stackSize;
        }
        return items * RadiationConfig.reactorCraftCoreEmission;
    }
}
