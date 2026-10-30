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

/** Caps what an RF P2P input tunnel passes on per tick. AE2 isn't a compile dependency. */
@Pseudo
@Mixin(targets = "appeng.parts.p2p.PartP2PRFPower", remap = false)
public abstract class RFP2PBudgetMixin {

    @Unique
    private final TransferBudget u25core$budget = new TransferBudget();
    @Unique
    private boolean u25core$inner;

    @Shadow(remap = false)
    public abstract int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate);

    @Inject(method = "receiveEnergy", at = @At("HEAD"), cancellable = true)
    private void u25core$budget(ForgeDirection from, int maxReceive, boolean simulate,
        CallbackInfoReturnable<Integer> cir) {
        if (u25core$inner) return;
        int allowed = (int) Math.min(maxReceive, u25core$budget.remaining(ModConfig.p2pRFPerTick));
        int received = 0;
        if (allowed > 0) {
            u25core$inner = true;
            try {
                received = receiveEnergy(from, allowed, simulate);
            } finally {
                u25core$inner = false;
            }
            if (!simulate) u25core$budget.use(received);
        }
        cir.setReturnValue(received);
    }
}
