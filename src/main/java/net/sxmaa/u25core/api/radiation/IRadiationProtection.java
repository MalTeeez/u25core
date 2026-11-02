package net.sxmaa.u25core.api.radiation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Implement on an armor item to protect its wearer. Each piece only covers its part of the body (head 15%, chest 45%,
 * legs 30%, feet 10%), so a missing piece lets its share through unprotected.
 */
public interface IRadiationProtection {

    /** Fraction of radiation this piece blocks for the body part it covers, 0 to 1. */
    double getRadiationProtection(ItemStack stack, EntityLivingBase wearer);
}
