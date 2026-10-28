package net.sxmaa.u25core.api.radiation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Implement on an armor item to protect its wearer. Resistances of all worn pieces add up, and the absorbed rate is
 * {@code r * r / (r + R)}: high resistance stops low rates almost entirely but leaks at high rates.
 */
public interface IRadiationProtection {

    /** Resistance R in Sv/h. */
    double getRadiationResistance(ItemStack stack, EntityLivingBase wearer);
}
