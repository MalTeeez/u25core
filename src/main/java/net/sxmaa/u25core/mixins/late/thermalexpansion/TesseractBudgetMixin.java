package net.sxmaa.u25core.mixins.late.thermalexpansion;

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

/** Caps the RF a Tesseract sends to its frequency per tick. Thermal Expansion isn't a compile dependency. */
@Pseudo
@Mixin(targets = "cofh.thermalexpansion.block.ender.TileTesseract", remap = false)
public abstract class TesseractBudgetMixin {

    @Unique
    private final TransferBudget u25core$budget = new TransferBudget();
    @Unique
    private boolean u25core$inner;

    @Shadow(remap = false)
    public abstract int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate);

    @Inject(
        method = "receiveEnergy(Lnet/minecraftforge/common/util/ForgeDirection;IZ)I",
        at = @At("HEAD"),
        cancellable = true)
    private void u25core$budget(ForgeDirection from, int maxReceive, boolean simulate,
        CallbackInfoReturnable<Integer> cir) {
        if (u25core$inner) return;
        int allowed = (int) Math.min(maxReceive, u25core$budget.remaining(ModConfig.tesseractRFPerTick));
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
