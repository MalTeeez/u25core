package net.sxmaa.u25core.api.radiation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

/**
 * Entry point of the U25Core radiation framework.
 * <p>
 * Ship against the api jar as compileOnly, and only touch this class when {@code Loader.isModLoaded(MODID)}.
 * Without U25Core installed there is no radiation, so callers should simply skip it.
 * <p>
 * Units: ambient levels and source emissions are in Sv/h, doses in Sv. Radioactivity in the registry is in Sv/h per
 * unit (per item, per mB of fluid or gas).
 * <p>
 * Registry keys: {@code "gas:<mekanism gas name>"}, {@code "fluid:<fluid name>"}, {@code "ore:<ore dict name>"},
 * {@code "item:<modid:name>"} or {@code "item:<modid:name>:<meta>"}.
 */
public final class RadiationAPI {

    public static final String MODID = "u25core";

    private static IRadiationManager manager;
    /** Registrations made before U25Core's preInit, applied once the manager is set. */
    private static final java.util.Map<String, Double> earlyRegistrations = new java.util.LinkedHashMap<>();

    private RadiationAPI() {}

    /** Set by U25Core during preInit. */
    public static void setManager(IRadiationManager instance) {
        manager = instance;
        for (java.util.Map.Entry<String, Double> e : earlyRegistrations.entrySet()) {
            instance.registerRadioactive(e.getKey(), e.getValue());
        }
        earlyRegistrations.clear();
    }

    /** Whether radiation is active. If false, don't consume materials you would otherwise release. */
    public static boolean isEnabled() {
        return manager != null && manager.isEnabled();
    }

    /** Ambient radiation in Sv/h at a position. */
    public static double getLevel(World world, double x, double y, double z) {
        return isEnabled() ? manager.getLevel(world, x, y, z) : 0;
    }

    /** Accumulated dose of an entity in Sv. */
    public static double getDose(EntityLivingBase entity) {
        return isEnabled() ? manager.getDose(entity) : 0;
    }

    /** Adds dose in Sv directly, ignoring protection. */
    public static void addDose(EntityLivingBase entity, double sieverts) {
        if (isEnabled()) manager.addDose(entity, sieverts);
    }

    /** Exposes an entity to a rate in Sv/h for some ticks, reduced by its protection. */
    public static void expose(EntityLivingBase entity, double svPerHour, int ticks) {
        if (isEnabled()) manager.expose(entity, svPerHour, ticks);
    }

    /** Exposes every living entity in a box, e.g. the inside of a running reactor. */
    public static void exposeArea(World world, AxisAlignedBB box, double svPerHour, int ticks) {
        if (isEnabled()) manager.exposeArea(world, box, svPerHour, ticks);
    }

    /** Adds lingering contamination in Sv/h at a position. It decays over time and spreads to nearby chunks. */
    public static void contaminate(World world, double x, double y, double z, double svPerHour) {
        if (isEnabled()) manager.contaminate(world, x, y, z, svPerHour);
    }

    /**
     * Releases a material into the environment (dumped, leaked, destroyed). Returns true if it was radioactive and got
     * turned into contamination, in which case the caller should consume it.
     */
    public static boolean release(World world, double x, double y, double z, String key, double amount) {
        return isEnabled() && manager.release(world, x, y, z, key, amount);
    }

    public static boolean release(World world, double x, double y, double z, ItemStack stack) {
        return isEnabled() && manager.release(world, x, y, z, stack);
    }

    public static boolean release(World world, double x, double y, double z, FluidStack stack) {
        return isEnabled() && manager.release(world, x, y, z, stack);
    }

    /** Radioactivity of a registry key in Sv/h per unit, 0 if not radioactive. */
    public static double getRadioactivity(String key) {
        return manager != null ? manager.getRadioactivity(key) : 0;
    }

    public static double getRadioactivity(ItemStack stack) {
        return manager != null ? manager.getRadioactivity(stack) : 0;
    }

    public static double getRadioactivity(FluidStack stack) {
        return manager != null ? manager.getRadioactivity(stack) : 0;
    }

    /** Registers or overrides a radioactive material, in Sv/h per unit. Config values take precedence. */
    public static void registerRadioactive(String key, double svPerHourPerUnit) {
        if (manager != null) manager.registerRadioactive(key, svPerHourPerUnit);
        else earlyRegistrations.put(key, svPerHourPerUnit);
    }

    /**
     * Tells the framework a tile entity started emitting, so its chunk gets tracked. Tiles implementing
     * {@link IRadiationSource} are found on chunk load; call this when one starts emitting later, it's cheap.
     */
    public static void notifySource(TileEntity tile) {
        if (isEnabled()) manager.notifySource(tile);
    }

    /**
     * Fires neutrons from a position along the axes. They hit {@link INeutronHandler}s and get absorbed by shielding.
     */
    public static void emitNeutrons(World world, int x, int y, int z, int count, NeutronType type) {
        if (isEnabled()) manager.emitNeutrons(world, x, y, z, count, type);
    }

    /**
     * Schedules a meltdown: explosions over {@code durationTicks} inside {@code bounds}, then contamination of
     * {@code contamination} Sv/h at its centre. Persisted across restarts.
     */
    public static void scheduleMeltdown(World world, AxisAlignedBB bounds, float explosionPower, double contamination,
        int durationTicks) {
        if (isEnabled()) manager.scheduleMeltdown(world, bounds, explosionPower, contamination, durationTicks);
    }
}
