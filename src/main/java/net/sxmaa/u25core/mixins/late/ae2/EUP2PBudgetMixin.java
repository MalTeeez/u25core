package net.sxmaa.u25core.mixins.late.ae2;

import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.util.TransferBudget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/** Caps what an EU P2P input tunnel passes on per tick. AE2 isn't a compile dependency. */
@Pseudo
@Mixin(targets = "appeng.parts.p2p.PartP2PIC2Power", remap = false)
public abstract class EUP2PBudgetMixin {

    @Unique
    private final TransferBudget u25core$budget = new TransferBudget();
    @Unique
    private boolean u25core$inner;

    @Shadow(remap = false)
    public abstract double injectEnergy(ForgeDirection from, double amount, double voltage);

    // So the energy net doesn't offer more than the tunnel takes this tick
    @ModifyReturnValue(method = "getDemandedEnergy", at = @At("RETURN"))
    private double u25core$demand(double demand) {
        return Math.min(demand, u25core$budget.remaining(ModConfig.p2pEUPerTick));
    }

    // Returns what wasn't accepted
    @Inject(method = "injectEnergy", at = @At("HEAD"), cancellable = true)
    private void u25core$budget(ForgeDirection from, double amount, double voltage,
        CallbackInfoReturnable<Double> cir) {
        if (u25core$inner) return;
        double allowed = Math.min(amount, u25core$budget.remaining(ModConfig.p2pEUPerTick));
        double accepted = 0;
        if (allowed > 0) {
            u25core$inner = true;
            try {
                accepted = allowed - injectEnergy(from, allowed, voltage);
            } finally {
                u25core$inner = false;
            }
            u25core$budget.use(accepted);
        }
        cir.setReturnValue(amount - accepted);
    }
}
