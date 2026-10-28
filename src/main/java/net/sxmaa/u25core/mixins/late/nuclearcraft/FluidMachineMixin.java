package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import nc.tile.machine.TileMachineFluidIn;
import nc.tile.machine.TileMachineFluidOut;

@Mixin(value = { TileMachineFluidIn.class, TileMachineFluidOut.class }, remap = false)
public class FluidMachineMixin {

    @Shadow(remap = false)
    public FluidTank tank;

    // fill ignored canFill, so any fluid could be piped in and processed as the right one
    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void u25core$respectCanFill(ForgeDirection from, FluidStack resource, boolean doFill,
        CallbackInfoReturnable<Integer> cir) {
        if (resource == null || !((IFluidHandler) this).canFill(from, resource.getFluid())) cir.setReturnValue(0);
    }

    // drain(FluidStack) drained whatever was in the tank, regardless of the requested fluid
    @Inject(
        method = "drain(Lnet/minecraftforge/common/util/ForgeDirection;Lnet/minecraftforge/fluids/FluidStack;Z)Lnet/minecraftforge/fluids/FluidStack;",
        at = @At("HEAD"),
        cancellable = true)
    private void u25core$matchDrainedFluid(ForgeDirection from, FluidStack resource, boolean doDrain,
        CallbackInfoReturnable<FluidStack> cir) {
        if (resource == null || !resource.isFluidEqual(tank.getFluid())) cir.setReturnValue(null);
    }
}
