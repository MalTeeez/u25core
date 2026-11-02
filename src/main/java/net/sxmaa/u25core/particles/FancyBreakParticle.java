package net.sxmaa.u25core.particles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.sxmaa.u25core.config.ParticleConfig;

/**
 * A block break particle drawn as a small cube of the block texture instead of a flat quad, so the debris keeps its
 * volume while it tumbles. Replaces the vanilla digging particle through {@code EffectRendererParticleMixin}.
 * <p>
 * The cube is drawn in the vanilla particle vertex format (no normals) and takes its shading from a per face colour
 * multiplier instead, so it stays batched with every other particle of the layer instead of flushing the tessellator
 * and changing GL state once per particle.
 */
public class FancyBreakParticle extends EntityDiggingFX {

    /** Signs of the four corners of a face quad, in its own plane, counter clockwise seen from outside. */
    private static final int[][] QUAD = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

    static {
        // A face wound the wrong way is culled, which reads as a hole in the cube
        assert windingIsOutward() : "FancyBreakParticle face winding is inside out";
    }

    private float spinX, spinY, spinZ;
    private float prevSpinX, prevSpinY, prevSpinZ;
    private float stepX, stepY, stepZ;

    private float prevScale, prevAlpha;
    private final float shrink;
    private final float fade;
    private boolean settled;

    private final float[] cornerX = new float[8];
    private final float[] cornerY = new float[8];
    private final float[] cornerZ = new float[8];

    public FancyBreakParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ,
        Block block, int meta, int side, float scale) {
        super(world, x, y, z, motionX, motionY, motionZ, block, meta, side);

        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleGravity = block.blockParticleGravity * ParticleConfig.pieceGravity;

        float size = scale * ParticleConfig.pieceScale;
        if (ParticleConfig.randomScale) size *= 0.75F + world.rand.nextFloat() * 0.25F;

        this.particleScale = size;
        this.prevScale = size;

        int shortLife = Math.min(ParticleConfig.minLifetime, ParticleConfig.maxLifetime);
        int longLife = Math.max(ParticleConfig.minLifetime, ParticleConfig.maxLifetime);
        this.particleMaxAge = shortLife + world.rand.nextInt(longLife - shortLife + 1);

        this.shrink = ParticleConfig.randomFade ? 0.87F + world.rand.nextFloat() * 0.03F : 0.89F;
        this.fade = ParticleConfig.randomFade ? 0.64F + world.rand.nextFloat() * 0.12F : 0.7F;

        float speed = 6.0F + world.rand.nextFloat() * 10.0F;
        this.spinX = this.prevSpinX = world.rand.nextInt(360);
        this.spinY = this.prevSpinY = world.rand.nextInt(360);
        this.spinZ = this.prevSpinZ = world.rand.nextInt(360);
        this.stepX = world.rand.nextBoolean() ? speed : -speed;
        this.stepY = world.rand.nextBoolean() ? speed : -speed;
        this.stepZ = world.rand.nextBoolean() ? speed : -speed;

        this.setSize(size / 5.0F, size / 5.0F);
        // setSize keeps the y offset the base class chose for its own 0.2 size, which would leave the collision box
        // hanging below the position and the cube drawn above it
        this.yOffset = this.height / 2.0F;
        this.setPosition(x, y, z);
    }

    /**
     * Spawns the cubes of a broken block. Returns false to let the vanilla flat particles happen instead.
     */
    public static boolean spawnForBreak(World world, int x, int y, int z, Block block, int meta) {
        if (!ParticleConfig.fancyBlockParticles || world == null) return false;
        if (block == null || block.isAir(world, x, y, z)) return false;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.effectRenderer == null) return false;

        // The block spawns its own particles here, which vanilla would do right after this, so nothing else may run
        if (block.addDestroyEffects(world, x, y, z, meta, mc.effectRenderer)) return true;

        if (block instanceof BlockLiquid || isBlacklisted(block, meta)) return false;

        int perAxis = MathHelper.clamp_int(ParticleConfig.piecesPerAxis, 1, 8);

        for (int i = 0; i < perAxis; ++i) {
            for (int j = 0; j < perAxis; ++j) {
                for (int k = 0; k < perAxis; ++k) {
                    double px = x + (i + 0.5D) / perAxis;
                    double py = y + (j + 0.5D) / perAxis;
                    double pz = z + (k + 0.5D) / perAxis;

                    FancyBreakParticle particle = new FancyBreakParticle(
                        world,
                        px,
                        py,
                        pz,
                        px - x - 0.5D,
                        py - y - 0.5D,
                        pz - z - 0.5D,
                        block,
                        meta,
                        world.rand.nextInt(6),
                        1.0F);

                    particle.applyColourMultiplier(x, y, z);
                    mc.effectRenderer.addEffect(particle);
                }
            }
        }

        return true;
    }

    /**
     * Spawns the single cube a block face shows while it is being mined. Returns false to let vanilla do it.
     */
    public static boolean spawnForHit(World world, int x, int y, int z, int side) {
        if (!ParticleConfig.fancyBlockParticles || !ParticleConfig.hitPieces || world == null) return false;

        Block block = world.getBlock(x, y, z);
        if (block == null || block.getMaterial() == Material.air) return false;

        int meta = world.getBlockMetadata(x, y, z);
        if (block instanceof BlockLiquid || isBlacklisted(block, meta)) return false;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.effectRenderer == null) return false;

        double px = x + 0.5D;
        double py = y + 0.5D;
        double pz = z + 0.5D;

        // Mine where the crosshair is, so the cube sits under the crack overlay; fall back to the middle of the face
        MovingObjectPosition over = mc.objectMouseOver;
        if (over != null && over.hitVec != null && over.blockX == x && over.blockY == y && over.blockZ == z) {
            px = over.hitVec.xCoord;
            py = over.hitVec.yCoord;
            pz = over.hitVec.zCoord;
        } else {
            if (side == 0) py = y;
            else if (side == 1) py = y + 1.0D;
            else if (side == 2) pz = z;
            else if (side == 3) pz = z + 1.0D;
            else if (side == 4) px = x;
            else if (side == 5) px = x + 1.0D;
        }

        // Off the face, or half the cube starts inside the block, whose collision a box already overlapping it ignores,
        // so gravity drops it straight through. Vanilla moves its own hit particles out by the same 0.1.
        if (side == 0) py -= 0.1D;
        else if (side == 1) py += 0.1D;
        else if (side == 2) pz -= 0.1D;
        else if (side == 3) pz += 0.1D;
        else if (side == 4) px -= 0.1D;
        else if (side == 5) px += 0.1D;

        FancyBreakParticle particle = new FancyBreakParticle(
            world,
            px,
            py,
            pz,
            0.0D,
            0.0D,
            0.0D,
            block,
            meta,
            side,
            ParticleConfig.hitPieceScale);

        particle.applyColourMultiplier(x, y, z);
        mc.effectRenderer.addEffect(particle);

        return true;
    }

    private static boolean isBlacklisted(Block block, int meta) {
        String name = Block.blockRegistry.getNameForObject(block);
        if (name == null) return false;

        for (String entry : ParticleConfig.blacklist) {
            if (entry == null || entry.isEmpty()) continue;
            if (entry.equals(name)) return true;
            if (!entry.startsWith(name) || entry.length() <= name.length() || entry.charAt(name.length()) != ':')
                continue;

            try {
                if (Integer.parseInt(entry.substring(name.length() + 1)) == meta) return true;
            } catch (NumberFormatException ignored) {
                // An entry that is not a metadata is just a block name that does not match
            }
        }

        return false;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevSpinX = this.spinX;
        this.prevSpinY = this.spinY;
        this.prevSpinZ = this.spinZ;
        this.prevScale = this.particleScale;
        this.prevAlpha = this.particleAlpha;

        if (!ParticleConfig.fancyBlockParticles) {
            this.setDead();
            return;
        }

        if (this.particleAge++ >= this.particleMaxAge) {
            this.particleScale *= this.shrink;
            this.particleAlpha *= this.fade;
            if (this.particleScale < 0.02F || this.particleAlpha < 0.02F) {
                this.setDead();
                return;
            }
        }

        float spin = this.settled ? 0.0F : ParticleConfig.pieceSpin;
        this.spinX += this.stepX * spin;
        this.spinY += this.stepY * spin;
        this.spinZ += this.stepZ * spin;

        if (!this.onGround) this.motionY -= 0.04D * this.particleGravity;

        double beforeX = this.motionX;
        double beforeZ = this.motionZ;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (ParticleConfig.bounceOffWalls) {
            // A cube that reached a wall partway through the tick still moved, so compare what it asked for against
            // what it got, the way vanilla decides isCollidedHorizontally
            if (beforeX != 0.0D && Math.abs(this.posX - this.prevPosX - beforeX) > 1.0E-7D)
                this.motionX = -beforeX * 0.6D;
            if (beforeZ != 0.0D && Math.abs(this.posZ - this.prevPosZ - beforeZ) > 1.0E-7D)
                this.motionZ = -beforeZ * 0.6D;
        }

        if (this.onGround) {
            float traction = ParticleConfig.lowTraction ? 0.93F : 0.66F;
            this.motionX *= traction;
            this.motionZ *= traction;

            if (ParticleConfig.restOnFloor) {
                // Stop tumbling once it has landed: onGround alternates as motionY is zeroed, so the spin would
                // creep forward between snaps and the cube would twitch
                this.settled = true;
                this.spinX = Math.round(this.spinX / 90.0F) * 90.0F;
                this.spinY = Math.round(this.spinY / 90.0F) * 90.0F;
                this.spinZ = Math.round(this.spinZ / 90.0F) * 90.0F;
                this.motionY = 0.0D;
            }
        }

        if (ParticleConfig.waterPhysics && this.inWater()) {
            this.motionX *= 0.93D;
            this.motionZ *= 0.93D;
            this.motionY = this.motionY * 0.5D + 0.01D;
        }

        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks, float rotationX, float rotationZ,
        float rotationYZ, float rotationXY, float rotationXZ) {
        if (this.particleIcon == null) return;

        float size = (this.prevScale + (this.particleScale - this.prevScale) * partialTicks) / 10.0F;
        float alpha = this.prevAlpha + (this.particleAlpha - this.prevAlpha) * partialTicks;

        float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        float radX = (this.prevSpinX + (this.spinX - this.prevSpinX) * partialTicks) * 0.017453292F;
        float radY = (this.prevSpinY + (this.spinY - this.prevSpinY) * partialTicks) * 0.017453292F;
        float radZ = (this.prevSpinZ + (this.spinZ - this.prevSpinZ) * partialTicks) * 0.017453292F;

        float sinX = MathHelper.sin(radX), cosX = MathHelper.cos(radX);
        float sinY = MathHelper.sin(radY), cosY = MathHelper.cos(radY);
        float sinZ = MathHelper.sin(radZ), cosZ = MathHelper.cos(radZ);

        for (int corner = 0; corner < 8; ++corner) {
            float cx = cornerCoordinate(corner, 0) * size;
            float cy = cornerCoordinate(corner, 1) * size;
            float cz = cornerCoordinate(corner, 2) * size;

            float aroundX = cy * cosX - cz * sinX;
            float aroundXz = cy * sinX + cz * cosX;
            float aroundXZx = cx * cosZ - aroundX * sinZ;
            float aroundXZy = cx * sinZ + aroundX * cosZ;

            this.cornerX[corner] = x + aroundXZx * cosY + aroundXz * sinY;
            this.cornerY[corner] = y + aroundXZy;
            this.cornerZ[corner] = z + aroundXz * cosY - aroundXZx * sinY;
        }

        // A quarter of the block texture, like a vanilla digging particle picks
        float u0 = this.particleIcon.getInterpolatedU(this.particleTextureJitterX * 4.0F);
        float u1 = this.particleIcon.getInterpolatedU((this.particleTextureJitterX + 1.0F) * 4.0F);
        float v0 = this.particleIcon.getInterpolatedV(this.particleTextureJitterY * 4.0F);
        float v1 = this.particleIcon.getInterpolatedV((this.particleTextureJitterY + 1.0F) * 4.0F);

        int brightness = this.getBrightnessForRender(partialTicks);
        tessellator.setBrightness(brightness);

        for (int axis = 0; axis < 3; ++axis) {
            for (int side = 0; side < 2; ++side) {
                // The lightmap is a 16x16 ramp, so scaling its coordinate lands nowhere near the factor asked for;
                // the vertex colour multiplies exactly
                float shade = axis == 1 ? (side == 1 ? 1.0F : 0.6F) : 0.82F;
                tessellator.setColorRGBA_F(
                    this.particleRed * shade,
                    this.particleGreen * shade,
                    this.particleBlue * shade,
                    alpha);

                for (int step = 0; step < 4; ++step) {
                    int quadrant = side == 1 ? step : 3 - step;
                    int corner = cornerIndex(axis, side, quadrant);

                    tessellator.addVertexWithUV(
                        this.cornerX[corner],
                        this.cornerY[corner],
                        this.cornerZ[corner],
                        quadrant == 1 || quadrant == 2 ? u1 : u0,
                        quadrant >= 2 ? v1 : v0);
                }
            }
        }
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        int x = MathHelper.floor_double(this.posX);
        int z = MathHelper.floor_double(this.posZ);
        if (!this.worldObj.blockExists(x, 0, z)) return 0;

        // A little above the position, or a cube resting on a floor samples the block it lies on
        return this.worldObj.getLightBrightnessForSkyBlocks(x, MathHelper.floor_double(this.posY + 0.1D), z, 0);
    }

    /**
     * Corner of the cube touched by QUAD entry {@code quadrant} of a face. Bits of the result are the x, y, z signs.
     */
    private static int cornerIndex(int axis, int side, int quadrant) {
        int index = side == 1 ? 1 << axis : 0;
        if (QUAD[quadrant][0] > 0) index |= 1 << ((axis + 1) % 3);
        if (QUAD[quadrant][1] > 0) index |= 1 << ((axis + 2) % 3);
        return index;
    }

    /** Corner offset along an axis, -1 or 1, indexed by the sign bits of a corner. */
    private static float cornerCoordinate(int corner, int axis) {
        return ((corner >> axis) & 1) == 1 ? 1.0F : -1.0F;
    }

    /**
     * Whether the cube is in water. Entity.isInWater() only reports what handleWaterMovement() set, and nothing calls
     * that for a particle; calling it here would also play splash sounds and spawn splashes.
     */
    private boolean inWater() {
        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.posY);
        int z = MathHelper.floor_double(this.posZ);
        return this.worldObj.blockExists(x, y, z) && this.worldObj.getBlock(x, y, z)
            .getMaterial() == Material.water;
    }

    /** Every face must lie on its axis and be wound counter clockwise seen from outside, or culling eats it. */
    private static boolean windingIsOutward() {
        for (int axis = 0; axis < 3; ++axis) {
            int first = (axis + 1) % 3;
            int second = (axis + 2) % 3;

            for (int side = 0; side < 2; ++side) {
                double[] alongFirst = new double[4];
                double[] alongSecond = new double[4];

                for (int step = 0; step < 4; ++step) {
                    int quadrant = side == 1 ? step : 3 - step;
                    int corner = cornerIndex(axis, side, quadrant);

                    if (cornerCoordinate(corner, axis) != (side == 1 ? 1.0F : -1.0F)) return false;

                    alongFirst[step] = cornerCoordinate(corner, first);
                    alongSecond[step] = cornerCoordinate(corner, second);
                }

                // For cyclic axes, first crossed with second is the face normal
                double cross = (alongFirst[1] - alongFirst[0]) * (alongSecond[2] - alongSecond[1])
                    - (alongSecond[1] - alongSecond[0]) * (alongFirst[2] - alongFirst[1]);
                if (side == 1 ? cross <= 0 : cross >= 0) return false;
            }
        }

        return true;
    }
}
