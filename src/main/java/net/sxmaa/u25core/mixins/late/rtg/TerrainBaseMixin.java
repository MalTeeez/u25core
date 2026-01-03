package net.sxmaa.u25core.mixins.late.rtg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import rtg.util.CellNoise;
import rtg.util.OpenSimplexNoise;
import rtg.world.gen.terrain.TerrainBase;

@Mixin(value = TerrainBase.class, remap = false)
public abstract class TerrainBaseMixin {

    @Shadow
    public static float blendedHillHeight(float simplex, float turnAt) {
        return 0;
    }

    @Shadow
    public static float getTerrainBase(float river) {
        return 0;
    }

    @ModifyConstant(method = "<init>(F)V", constant = @Constant(floatValue = 2f))
    private float increaseBaseTerrainNoise(float original) {
        return 2f;
    }

    @ModifyConstant(method = "<init>(F)V", constant = @Constant(floatValue = 6f))
    private float increaseTerrainNoiseAmplitude(float original) {
        return 18f;
    }

    @ModifyConstant(method = "groundNoise(IIFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 49f))
    private static float increaseGroundNoiseAmplitude1(float original) {
        return 25f;
    }

    @ModifyConstant(method = "groundNoise(IIFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 23f))
    private static float increaseGroundNoiseAmplitude2(float original) {
        return 12f;
    }

    @ModifyConstant(method = "groundNoise(IIFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 11f))
    private static float increaseGroundNoiseAmplitude3(float original) {
        return 6f;
    }

    @ModifyConstant(method = "groundNoise(FFFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 49f))
    private static float increaseGroundNoiseAmplitude1(float x, float y, float original) {
        return 25f;
    }

    @ModifyConstant(method = "groundNoise(FFFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 23f))
    private static float increaseGroundNoiseAmplitude2(float x, float y, float original) {
        return 12f;
    }

    @ModifyConstant(method = "groundNoise(FFFLrtg/util/OpenSimplexNoise;)F", constant = @Constant(floatValue = 11f))
    private static float increaseGroundNoiseAmplitude3(float x, float y, float original) {
        return 6f;
    }

    /**
     * @author malteeez
     * @reason Make highland terrain more rugged
     */
    @Overwrite
    public static float terrainHighland(float x, float y, OpenSimplexNoise simplex, CellNoise cell, float river,
        float start, float width, float height, float baseAdjust) {
        float h = simplex.noise2(x / width, y / width) * height * river;
        h = h < start ? start + ((h - start) / 3.5f) : h; // Less smoothing (from 4.5)

        if (h < 0f) h = 0;
        if (h > 0f) {
            float st = h * 1.5f > 15f ? 15f : h * 1.5f;
            h += simplex.octave(4)
                .noise(x / 70D, y / 70D, 1D) * st;
            h = h * river;
        }

        // More frequent, stronger variations
        h += blendedHillHeight(simplex.noise2(x / 18f, y / 18f), 0f) * 3.74f;
        h += blendedHillHeight(simplex.noise2(x / 10f, y / 10f), 0f) * 1.9f;
        h += blendedHillHeight(simplex.noise2(x / 4f, y / 4f), 0f) * 1f;

        if (h < 0) h = h / 1.6f; // Less smoothing (from 2f)
        if (h < -3) h = (h + 3f) / 1.5f - 2.9f; // Less smoothing (from 2f)

        // & Add roughness detail
        h += simplex.noise2(x / 7f, y / 7f) * 1.5f;
        h += simplex.noise2(x / 3f, y / 3f) * 0.8f;

        return getTerrainBase(river) + (h + baseAdjust) * river;
    }

    @ModifyReturnValue(method = "blendedHillHeight(F)F", at = @At("RETURN"))
    private static float modifyBlendedHillHeight(float original) {
        return original * 1.15f;
    }
}
