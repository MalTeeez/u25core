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

/** Caps what an RF P2P input tunnel sends across a QNB, from its grid's shared budget. */
@Pseudo
@Mixin(targets = "appeng.parts.p2p.PartP2PRFPower", remap = false)
public abstract class RFP2PQnbBudgetMixin {

    @Unique
    private boolean u25core$qnbInner;

    @Shadow(remap = false)
    public abstract int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate);

    @Inject(method = "receiveEnergy", at = @At("HEAD"), cancellable = true)
    private void u25core$qnbBudget(ForgeDirection from, int maxReceive, boolean simulate,
        CallbackInfoReturnable<Integer> cir) {
        if (u25core$qnbInner) return;
        double left = QnbBudget.remaining(this);
        if (left == Double.POSITIVE_INFINITY) return;
        int allowed = (int) Math.min(maxReceive, left);
        int received = 0;
        if (allowed > 0) {
            u25core$qnbInner = true;
            try {
                received = receiveEnergy(from, allowed, simulate);
            } finally {
                u25core$qnbInner = false;
            }
            if (!simulate) QnbBudget.use(this, received);
        }
        cir.setReturnValue(received);
    }
}
