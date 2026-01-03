package net.sxmaa.u25core.mixins.late.rtg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import rtg.util.CellNoise;
import rtg.util.OpenSimplexNoise;
import rtg.world.gen.terrain.MountainsWithPassesEffect;
import rtg.world.gen.terrain.TerrainBase;

@Mixin(value = MountainsWithPassesEffect.class, remap = false)
public class MountainWithPassesEffectMixin {

    @Shadow
    public int hillOctave;

    @Shadow
    public float mountainWavelength;

    @Shadow
    private float adjustedBottom;

    @Shadow
    public float spikeWavelength;

    @Shadow
    public int spikeOctave;

    @Shadow
    public float spikeHeight;

    @Shadow
    public float mountainHeight;

    /**
     * @author malteeez
     * @reason Modify mountain with passes effect to be more jagged and spiky
     */
    @Overwrite
    public final float added(OpenSimplexNoise simplex, CellNoise cell, float x, float y) {
        float noise = simplex.octave(hillOctave)
            .noise2(x / mountainWavelength, y / mountainWavelength);
        noise = Math.abs(noise);

        // more aggressive threshold for less smoothing
        noise = TerrainBase.blendedHillHeight(noise, -0.1f);

        // reduce the passes effect for less smoothing
        noise = 1f - (1f - noise) * 0.6f / (1f - adjustedBottom);

        float spikeNoise = simplex.octave(spikeOctave)
            .noise2(x / spikeWavelength, y / spikeWavelength);
        spikeNoise = Math.abs(spikeNoise);

        // don't square the spikes for less smoothing
        // spikeNoise *= spikeNoise;
        spikeNoise = spikeNoise * noise; // direct multiply instead of blended

        // fine detail for added roughness
        float roughness = simplex.octave(3)
            .noise2(x / 8f, y / 8f) * 3f;

        if (noise > 1.16) throw new RuntimeException();
        if (spikeNoise > 1.16) throw new RuntimeException();

        return noise * mountainHeight + spikeNoise * spikeHeight + roughness;
    }
}
