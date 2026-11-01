package net.sxmaa.u25core.mixins.late.draconicevolution;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.brandon3055.draconicevolution.common.handler.MinecraftForgeEventHandler;
import com.llamalad7.mixinextras.sugar.Local;

/** Keeps Draconic Evolution's speed cap from reversing and amplifying fast jetpack motion. */
@Mixin(value = MinecraftForgeEventHandler.class, remap = false)
public class MinecraftForgeEventHandlerMixin {

    // DE subtracts motionX/Z * (speed - cap). Above cap + 2 that reverses direction and increases speed,
    // repeating every tick until the player is launched out of the world. Normalize the excess by speed
    // so the existing subtraction preserves direction and leaves exactly the configured cap.
    // Double locals: motionX, motionZ, motion, reduction.
    @ModifyVariable(method = "onLivingUpdate", at = @At("STORE"), ordinal = 3, require = 1)
    private double u25core$normalizeSpeedReduction(double excess, @Local(ordinal = 2) double motion) {
        return motion > 0.0D ? Math.max(0.0D, Math.min(1.0D, excess / motion)) : 0.0D;
    }
}
