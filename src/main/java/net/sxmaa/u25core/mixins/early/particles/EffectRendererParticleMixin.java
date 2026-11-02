package net.sxmaa.u25core.mixins.early.particles;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.sxmaa.u25core.particles.FancyBreakParticle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Takes over the two places vanilla spawns flat block particles, so every block break and mining tick gets cubes
 * instead. Injected rather than swapping out {@code Minecraft#effectRenderer}, so any other EffectRenderer subclass in
 * the pack keeps working and the layer lists are never carried over by hand.
 * <p>
 * Cancelling the vanilla method is only the fallback: returning false from the spawners leaves the original untouched.
 * Applying is not gated on the config, so the toggle works in game without a restart.
 */
@Mixin(EffectRenderer.class)
public class EffectRendererParticleMixin {

    @Inject(method = "addBlockDestroyEffects", at = @At("HEAD"), cancellable = true)
    private void u25core$breakPieces(int x, int y, int z, Block block, int meta, CallbackInfo ci) {
        if (FancyBreakParticle.spawnForBreak(Minecraft.getMinecraft().theWorld, x, y, z, block, meta)) ci.cancel();
    }

    @Inject(method = "addBlockHitEffects", at = @At("HEAD"), cancellable = true)
    private void u25core$hitPieces(int x, int y, int z, int side, CallbackInfo ci) {
        if (FancyBreakParticle.spawnForHit(Minecraft.getMinecraft().theWorld, x, y, z, side)) ci.cancel();
    }
}
