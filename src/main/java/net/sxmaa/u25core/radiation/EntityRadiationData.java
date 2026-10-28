package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.api.radiation.IRadiationImmune;

/**
 * Accumulated dose and medicine state of a living entity. Exposure from all sources is collected between updates and
 * protection is applied once per update, so splitting an exposure into many calls doesn't change the dose.
 */
public final class EntityRadiationData implements IExtendedEntityProperties {

    public static final String KEY = "u25core:radiation";
    public static final DamageSource DAMAGE = new DamageSource("u25core.radiation").setDamageBypassesArmor()
        .setDamageIsAbsolute();

    /** Accumulated dose in Sv. */
    public double dose;
    /** Absorbed rate during the last update in Sv/h, for display. */
    public double rate;
    /** Remaining dose an anti-rad treatment removes over time, in Sv. */
    public double treatment;
    /** Extra resistance from rad-protection medicine in Sv/h, and its remaining ticks. */
    public double medicineResistance;
    public int medicineTicks;
    public int immunityTicks;

    /** Exposure collected since the last update, in Sv/h times ticks, before protection. */
    private double pendingExposure;
    /** A rate held until a world time, for radiation that works like a potion effect. */
    private double heldRate;
    private long heldUntil;

    private static TreeMap<Double, List<PotionEffect>> effectTiers;

    public static EntityRadiationData get(EntityLivingBase entity) {
        return (EntityRadiationData) entity.getExtendedProperties(KEY);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("dose", dose);
        tag.setDouble("treatment", treatment);
        tag.setDouble("medicineResistance", medicineResistance);
        tag.setInteger("medicineTicks", medicineTicks);
        tag.setInteger("immunityTicks", immunityTicks);
        tag.setDouble("heldRate", heldRate);
        tag.setLong("heldUntil", heldUntil);
        compound.setTag(KEY, tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound tag = compound.getCompoundTag(KEY);
        dose = tag.getDouble("dose");
        treatment = tag.getDouble("treatment");
        medicineResistance = tag.getDouble("medicineResistance");
        medicineTicks = tag.getInteger("medicineTicks");
        immunityTicks = tag.getInteger("immunityTicks");
        heldRate = tag.getDouble("heldRate");
        heldUntil = tag.getLong("heldUntil");
    }

    @Override
    public void init(Entity entity, World world) {}

    public static boolean isImmune(EntityLivingBase entity) {
        if (entity instanceof IRadiationImmune) return true;
        if (entity instanceof EntityPlayer player && player.capabilities.isCreativeMode) return true;
        if (RadiationConfig.undeadImmune && entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) return true;
        String name = entity.getClass()
            .getName();
        for (String immune : RadiationConfig.immuneEntities) {
            if (name.equals(immune) || name.endsWith("." + immune)) return true;
        }
        return false;
    }

    /** Immune, or still protected after respawning. */
    static boolean isProtectedFrom(EntityLivingBase entity, EntityRadiationData data) {
        return data.immunityTicks > 0 || isImmune(entity);
    }

    /** Resistance in Sv/h from worn armor and medicine. */
    public static double getResistance(EntityLivingBase entity) {
        double resistance = 0;
        for (int slot = 1; slot <= 4; slot++) {
            resistance += RadiationRegistry.getResistance(entity.getEquipmentInSlot(slot), entity);
        }
        EntityRadiationData data = get(entity);
        if (data != null && data.medicineTicks > 0) resistance += data.medicineResistance;
        return resistance;
    }

    /** Absorbed rate after protection: r^2 / (r + R), so protection works well against low rates only. */
    public static double absorbed(double rate, double resistance) {
        return rate <= 0 ? 0 : rate * rate / (rate + Math.max(0, resistance));
    }

    /** Adds exposure at a rate in Sv/h for some ticks. Protection is applied at the next update. */
    public static void expose(EntityLivingBase entity, double svPerHour, double ticks) {
        EntityRadiationData data = get(entity);
        if (data == null || entity.worldObj.isRemote || svPerHour <= 0) return;
        data.pendingExposure += svPerHour * ticks;
    }

    /**
     * Holds a rate for a duration like a potion effect: overlapping calls don't add up, the strongest rate and the
     * latest
     * end win. For mods that re-apply their radiation every tick.
     */
    public static void hold(EntityLivingBase entity, double svPerHour, int durationTicks) {
        EntityRadiationData data = get(entity);
        if (data == null || entity.worldObj.isRemote || svPerHour <= 0) return;
        long now = entity.worldObj.getTotalWorldTime();
        if (data.heldUntil <= now || svPerHour > data.heldRate) data.heldRate = svPerHour;
        data.heldUntil = Math.max(data.heldUntil, now + durationTicks);
    }

    /** Applies the exposure of the last {@code ticks} ticks, then recovery, medicine and effects. */
    public void update(EntityLivingBase entity, int ticks) {
        if (immunityTicks > 0) immunityTicks = Math.max(0, immunityTicks - ticks);
        if (medicineTicks > 0) medicineTicks = Math.max(0, medicineTicks - ticks);

        double exposure = pendingExposure;
        pendingExposure = 0;
        long now = entity.worldObj.getTotalWorldTime();
        if (heldUntil > now - ticks) exposure += heldRate * Math.min(ticks, heldUntil - (now - ticks));
        if (isProtectedFrom(entity, this)) {
            rate = 0;
        } else {
            double ambient = RadiationManager.INSTANCE.getLevel(entity.worldObj, entity.posX, entity.posY, entity.posZ);
            // Shielding blocks around the entity block the surroundings, not what it carries or direct exposure
            if (RadiationConfig.entityShielding) ambient *= Shielding.getPassFraction(
                entity.worldObj,
                (int) Math.floor(entity.posX),
                (int) Math.floor(entity.posY + entity.getEyeHeight()),
                (int) Math.floor(entity.posZ));
            ambient += RadiationManager.getCarried(entity);
            rate = absorbed(ambient + exposure / ticks, getResistance(entity));
            dose += rate * ticks / RadiationManager.TICKS_PER_HOUR;
        }

        if (treatment > 0) {
            // Treatment clears its dose over about a minute
            double removed = Math.min(treatment, Math.max(treatment * ticks / 1200, 0.001 * ticks));
            treatment -= removed;
            dose = Math.max(0, dose - removed);
        }
        dose *= Math.pow(0.5, (double) ticks / RadiationConfig.doseHalfLife);
        if (dose < 1.0E-9) dose = 0;

        if (isProtectedFrom(entity, this)) return;
        if (dose >= RadiationConfig.lethalDose) {
            entity.attackEntityFrom(DAMAGE, Float.MAX_VALUE);
            return;
        }
        Map.Entry<Double, List<PotionEffect>> tier = getEffectTiers()
            .floorEntry(dose / RadiationConfig.lethalDose * 100);
        if (tier == null) return;
        for (PotionEffect effect : tier.getValue()) {
            // Refresh slightly past the next update, so effects never lapse or stack with themselves
            entity.addPotionEffect(new PotionEffect(effect.getPotionID(), ticks + 40, effect.getAmplifier(), true));
        }
    }

    private static TreeMap<Double, List<PotionEffect>> getEffectTiers() {
        if (effectTiers != null) return effectTiers;
        effectTiers = new TreeMap<>();
        for (String entry : RadiationConfig.doseEffects) {
            String[] kv = entry.split("=", 2);
            try {
                List<PotionEffect> effects = new ArrayList<>();
                for (String part : kv[1].split(";")) {
                    String[] pa = part.trim()
                        .split("@");
                    Potion potion = RadiationRegistry.findPotion(pa[0].trim());
                    if (potion == null) {
                        U25Core.LOG.warn("Unknown potion in radiation effects: {}", pa[0]);
                        continue;
                    }
                    effects.add(new PotionEffect(potion.id, 0, pa.length > 1 ? Integer.parseInt(pa[1].trim()) : 0));
                }
                effectTiers.put(Double.parseDouble(kv[0].trim()), effects);
            } catch (RuntimeException e) {
                U25Core.LOG.warn("Invalid radiation effect entry: {}", entry);
            }
        }
        return effectTiers;
    }
}
