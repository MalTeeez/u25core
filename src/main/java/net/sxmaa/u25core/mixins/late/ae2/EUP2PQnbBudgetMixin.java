package net.sxmaa.u25core.mixins.late.ae2;

import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.util.QnbBudget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/** Caps what an EU P2P input tunnel sends across a QNB, from its grid's shared budget at 4 RF per EU. */
@Pseudo
@Mixin(targets = "appeng.parts.p2p.PartP2PIC2Power", remap = false)
public abstract class EUP2PQnbBudgetMixin {

    @Unique
    private static final double u25core$RF_PER_EU = 4;

    @Unique
    private boolean u25core$qnbInner;

    @Shadow(remap = false)
    public abstract double injectEnergy(ForgeDirection from, double amount, double voltage);

    // So the energy net doesn't offer more than the tunnel takes this tick
    @ModifyReturnValue(method = "getDemandedEnergy", at = @At("RETURN"))
    private double u25core$qnbDemand(double demand) {
        return Math.min(demand, QnbBudget.remaining(this) / u25core$RF_PER_EU);
    }

    // Returns what wasn't accepted
    @Inject(method = "injectEnergy", at = @At("HEAD"), cancellable = true)
    private void u25core$qnbBudget(ForgeDirection from, double amount, double voltage,
        CallbackInfoReturnable<Double> cir) {
        if (u25core$qnbInner) return;
        double left = QnbBudget.remaining(this);
        if (left == Double.POSITIVE_INFINITY) return;
        double allowed = Math.min(amount, left / u25core$RF_PER_EU);
        double accepted = 0;
        if (allowed > 0) {
            u25core$qnbInner = true;
            try {
                accepted = allowed - injectEnergy(from, allowed, voltage);
            } finally {
                u25core$qnbInner = false;
            }
            QnbBudget.use(this, accepted * u25core$RF_PER_EU);
        }
        cir.setReturnValue(amount - accepted);
    }
}
