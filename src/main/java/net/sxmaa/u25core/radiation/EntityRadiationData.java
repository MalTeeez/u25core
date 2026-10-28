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

/** Accumulated dose and medicine state of a living entity. */
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

    /** Applies a rate in Sv/h over some ticks, reduced by the entity's protection. */
    public static void expose(EntityLivingBase entity, double svPerHour, double ticks) {
        EntityRadiationData data = get(entity);
        if (data == null || entity.worldObj.isRemote || isImmune(entity) || data.immunityTicks > 0) return;
        double absorbed = absorbed(svPerHour, getResistance(entity));
        data.dose += absorbed * ticks / RadiationManager.TICKS_PER_HOUR;
    }

    /** Recovery, medicine and effects. Called every {@code ticks} ticks. */
    public void update(EntityLivingBase entity, int ticks) {
        if (immunityTicks > 0) immunityTicks = Math.max(0, immunityTicks - ticks);
        if (medicineTicks > 0) medicineTicks = Math.max(0, medicineTicks - ticks);
        if (treatment > 0) {
            // Treatment clears its dose over about a minute
            double removed = Math.min(treatment, Math.max(treatment * ticks / 1200, 0.001 * ticks));
            treatment -= removed;
            dose = Math.max(0, dose - removed);
        }
        dose *= Math.pow(0.5, (double) ticks / RadiationConfig.doseHalfLife);
        if (dose < 1.0E-9) dose = 0;

        if (isImmune(entity)) return;
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
                    Potion potion = findPotion(pa[0].trim());
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

    private static Potion findPotion(String name) {
        for (Potion potion : Potion.potionTypes) {
            if (potion != null && (potion.getName()
                .equals("potion." + name)
                || potion.getName()
                    .equals(name)))
                return potion;
        }
        return null;
    }
}
