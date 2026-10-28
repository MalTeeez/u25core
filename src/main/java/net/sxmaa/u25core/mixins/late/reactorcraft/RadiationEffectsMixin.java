package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;
import net.sxmaa.u25core.api.radiation.RadiationAPI;
import net.sxmaa.u25core.radiation.EntityRadiationData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.ReactorCraft.Auxiliary.RadiationEffects;

/** Routes ReactorCraft's radiation (potion exposure, contamination clouds) into the U25Core framework. */
@Mixin(value = RadiationEffects.class, remap = false)
public class RadiationEffectsMixin {

    /** ReactorCraft's intensity levels in Sv/h: background, low, moderate, high, lethal. */
    private static double u25core$svPerHour(RadiationEffects.RadiationIntensity intensity) {
        return intensity.ordinal() == 0 ? 0 : Math.pow(10, intensity.ordinal() - 3);
    }

    /**
     * Called every tick while exposed (held waste, radioactive fluid, ore). Returns true when it irradiated the entity,
     * which ReactorCraft uses for achievements. Keeps its side effects: powered armor drains, creepers turn into dirty
     * bombs.
     */
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private void u25core$expose(EntityLivingBase entity, RadiationEffects.RadiationIntensity intensity,
        CallbackInfoReturnable<Boolean> cir) {
        if (!intensity.causesHarm()) {
            cir.setReturnValue(false);
            return;
        }
        if (ReikaEntityHelper.isEntityWearingPoweredArmor(entity)) {
            for (int i = 1; i <= 4; i++) ReikaItemHelper.dechargeItem(entity.getEquipmentInSlot(i));
        }
        if (entity instanceof EntityCreeper) entity.getEntityData()
            .setBoolean("radioactive", true);
        boolean irradiated = !EntityRadiationData.isImmune(entity);
        if (irradiated) RadiationAPI.expose(entity, u25core$svPerHour(intensity), 1);
        cir.setReturnValue(irradiated);
    }

    // Neutron hits, ReactorCraft gave a 20 tick potion for them
    @Inject(method = "applyPulseEffects", at = @At("HEAD"), cancellable = true)
    private void u25core$pulse(EntityLivingBase entity, RadiationEffects.RadiationIntensity intensity,
        CallbackInfo ci) {
        RadiationAPI.expose(entity, u25core$svPerHour(intensity), 20);
        ci.cancel();
    }

    /**
     * Spawned sqrt(range) * density lingering clouds, now becomes contamination of the same total strength. Single
     * neutron leaks (low level, range 1) were capped by the number of nearby clouds; without clouds they would pile up
     * under every running reactor, so they're dropped.
     */
    @Inject(method = "contaminateArea", at = @At("HEAD"), cancellable = true)
    private void u25core$contaminate(World world, int x, int y, int z, int range, float density, double force,
        boolean los, RadiationEffects.RadiationIntensity intensity, CallbackInfoReturnable<Double> cir) {
        boolean neutronLeak = intensity == RadiationEffects.RadiationIntensity.LOWLEVEL && range <= 1;
        if (!neutronLeak) {
            int clouds = Math.max(1, (int) (Math.sqrt(range) * density));
            RadiationAPI.contaminate(world, x + 0.5, y + 0.5, z + 0.5, u25core$svPerHour(intensity) * clouds);
        }
        cir.setReturnValue(1D);
    }
}
