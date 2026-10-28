package net.sxmaa.u25core.radiation.client;

import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.world.World;

/** Faint self-lit blue glow in water near strong sources. */
public class EntityCherenkovFX extends EntityReddustFX {

    public EntityCherenkovFX(World world, double x, double y, double z) {
        super(world, x, y, z, 1.2F, 0.15F, 0.45F, 1.0F);
        motionX = motionZ = 0;
        motionY = 0.004;
        particleAlpha = 0.8F;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    @Override
    public float getBrightness(float partialTicks) {
        return 1;
    }
}
