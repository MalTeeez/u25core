package net.sxmaa.u25core.mixins.late.chromaticraft.adjacencycap;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import Reika.ChromatiCraft.TileEntity.AOE.Effect.TileEntityEnergyIncrease;
import Reika.DragonAPI.ModList;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

/**
 * Caps the energy adjacency upgrade's boost per target, summed over every upgrade around it. Each upgrade passes
 * its own boost to its EnergyInterface per side; this hands out what is left of the target's budget instead.
 */
@Mixin(value = TileEntityEnergyIncrease.class, remap = false)
public class TileEntityEnergyIncreaseMixin {

    /** Target -> {stamp, value}: injection generators sum boosts per world tick, reactors keep their base output. */
    @Unique
    private static final Map<TileEntity, double[]> u25core$spent = new WeakHashMap<>();

    @ModifyArg(
        method = "tickDirection",
        at = @At(
            value = "INVOKE",
            target = "LReika/ChromatiCraft/TileEntity/AOE/Effect/TileEntityEnergyIncrease$EnergyInterface;tick(LReika/ChromatiCraft/TileEntity/AOE/Effect/TileEntityEnergyIncrease;Lnet/minecraft/tileentity/TileEntity;D)V"),
        index = 2)
    private double u25core$capBoost(TileEntityEnergyIncrease booster, TileEntity te, double boost) {
        double cap = ModConfig.chromaAdjacencyMaxBoost;
        if (cap < 0) return boost;
        if (ModList.IC2.isLoaded()) {
            TileEntity t = te instanceof IReactorChamber ? (TileEntity) ((IReactorChamber) te).getReactor() : te;
            if (t instanceof TileEntityNuclearReactorElectric) {
                return u25core$capReactor((TileEntityNuclearReactorElectric) t, boost, cap);
            }
        }
        // The rest inject base * boost every tick, so the boosts of all upgrades add up within one tick
        double now = te.getWorldObj()
            .getTotalWorldTime();
        synchronized (u25core$spent) {
            double[] s = u25core$spent.get(te);
            if (s == null || s[0] != now) u25core$spent.put(te, s = new double[] { now, 0 });
            double b = Math.min(boost, Math.max(0, cap - s[1]));
            s[1] += b;
            return b;
        }
    }

    /**
     * ChromatiCraft adds boost * current output once per reactor cycle (updateTicker % 20 == 1), so upgrades
     * compound; keep the output after all of them at most base * (1 + cap).
     */
    @Unique
    private static double u25core$capReactor(TileEntityNuclearReactorElectric reactor, double boost, double cap) {
        int ticker = reactor.updateTicker;
        if (ticker % 20 != 1 || reactor.output <= 0) return boost; // ChromatiCraft adds nothing then
        synchronized (u25core$spent) {
            double[] s = u25core$spent.get(reactor);
            if (s == null || s[0] != ticker) u25core$spent.put(reactor, s = new double[] { ticker, reactor.output });
            return Math.min(boost, Math.max(0, s[1] * (1 + cap) / reactor.output - 1));
        }
    }
}
