package net.sxmaa.u25core.mixins.late.enderio;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import crazypants.enderio.machine.transceiver.ServerChannelRegister;

/**
 * A sending transceiver offers its whole budget to every receiver on a channel; spend it down as receivers take power.
 */
@Mixin(value = ServerChannelRegister.class, remap = false)
public class TransceiverPowerDupeMixin {

    // Runs right before sender.usePower(recievedPlusLoss); the next receiver is offered what's left
    @ModifyVariable(
        method = "sendPower",
        at = @At(value = "INVOKE", target = "Lcrazypants/enderio/machine/transceiver/TileTransceiver;usePower(I)I"),
        argsOnly = true,
        require = 1)
    private int u25core$spendBudget(int canSend, @Local(name = "recievedPlusLoss") int paid) {
        // round(round(b*0.9)/0.9) can be b+1
        return Math.max(0, canSend - paid);
    }
}
