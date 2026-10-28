package net.sxmaa.u25core.api.radiation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

/** Implemented by U25Core. Use {@link RadiationAPI} instead of calling this directly. */
public interface IRadiationManager {

    boolean isEnabled();

    double getLevel(World world, double x, double y, double z);

    double getDose(EntityLivingBase entity);

    void addDose(EntityLivingBase entity, double sieverts);

    void expose(EntityLivingBase entity, double svPerHour, int ticks);

    void exposeArea(World world, AxisAlignedBB box, double svPerHour, int ticks);

    void contaminate(World world, double x, double y, double z, double svPerHour);

    boolean release(World world, double x, double y, double z, String key, double amount);

    boolean release(World world, double x, double y, double z, ItemStack stack);

    boolean release(World world, double x, double y, double z, FluidStack stack);

    double getRadioactivity(String key);

    double getRadioactivity(ItemStack stack);

    double getRadioactivity(FluidStack stack);

    void registerRadioactive(String key, double svPerHourPerUnit);

    void notifySource(TileEntity tile);

    void emitNeutrons(World world, int x, int y, int z, int count, NeutronType type);

    void scheduleMeltdown(World world, AxisAlignedBB bounds, float explosionPower, double contamination,
        int durationTicks);
}
