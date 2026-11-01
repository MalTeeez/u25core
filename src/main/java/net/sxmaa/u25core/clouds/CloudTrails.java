package net.sxmaa.u25core.clouds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.sxmaa.u25core.config.ModConfig;

import com.darkshadow44.projectbabelfish.api.clouds.CloudEffects;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Client side, only registered when NotSoSimpleClouds is loaded. Leaves trails through the clouds behind:
 * <ul>
 * <li>rockets, missiles and meteors, any entity whose class name says so (Galacticraft, ExtraPlanets, DefenseTech,
 * Techguns, EnderIO's weather rocket, Blood Magic, Alfheim, vanilla fireworks);</li>
 * <li>other modded non-living entities moving very fast, e.g. RotaryCraft railgun shots and shells, Power Suits plasma
 * bolts;</li>
 * <li>players flying or falling fast (Draconic armor, angel rings, Botania's tiara, hang gliders, ...) or climbing with
 * a
 * jetpack (any chest item whose class name says so: Simply Jetpacks, Mekanism, IC2, Techguns, RotaryCraft; or Modular
 * Powersuits' jetpack and jet boots modules);</li>
 * <li>lightning bolts, vanilla or summoned by mods, punch a narrow shaft through the clouds.</li>
 * </ul>
 * Also applies the explosions and weather machine columns the server sends (CloudCompat).
 */
public final class CloudTrails {

    /** Rockets/missiles move at least this far per tick, squared, to leave a trail. */
    private static final double MIN_STEP_SQ = 0.1 * 0.1;
    /** Other modded projectiles have to move at least this far per tick, squared. */
    private static final double FAST_STEP_SQ = 1.5 * 1.5;
    /** Players flying or falling at least this far per tick, squared. */
    private static final double FAST_PLAYER_SQ = 0.4 * 0.4;
    /** Players this close above solid ground are walking or jumping, not flying. A jump peaks at about 1.25. */
    private static final int GROUND_CLEARANCE = 3;
    /** Jetpacks climb at least this fast, in blocks per tick. */
    private static final double MIN_CLIMB = 0.05;
    /** Clouds sit at y 128-200; trails far below them would only evict effects that show. */
    private static final double MIN_Y = 100;
    /** Clouds are 8-block cubes, smaller trails barely show. */
    private static final float MIN_RADIUS = 8;
    /** Extra ticks of lead on top of the expansion time, so a cushion of pushed cloud runs ahead of a player. */
    private static final float LEAD_MARGIN_TICKS = 4;
    /**
     * Largest explosion core, in blocks. Its outer pressure wave reaches 1.5x as far, ~1500, the cloud render distance.
     */
    private static final float MAX_EXPLOSION_RADIUS = 1000;
    /** Lightning and weather machine columns reach this far up, through any cloud layer. */
    private static final double COLUMN_HEIGHT = 400;

    /** Squared step per tick an entity class needs to leave a trail, infinite for none. */
    private final Map<Class<?>, Double> minStepSq = new HashMap<>();
    private final Map<Class<?>, Boolean> isJetpack = new HashMap<>();
    private final Set<Object> seenBolts = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        float[] e;
        while ((e = CloudCompat.PENDING.poll()) != null) {
            if (e[0] == CloudCompat.EXPLOSION) {
                // Capped so the biggest blasts stay within the cloud render distance
                CloudEffects.explosion(
                    e[1],
                    e[2],
                    e[3],
                    Math.min(e[4], MAX_EXPLOSION_RADIUS / CloudEffects.explosionRadiusPerStrength));
            } else {
                // The sky stays open for a while after the weather changes
                CloudEffects.trail(e[1], e[2], e[3], e[1], e[2] + COLUMN_HEIGHT, e[3], e[4], 2F, 30F);
            }
        }
        World w = Minecraft.getMinecraft().theWorld;
        if (w == null) return;

        for (Object o : w.weatherEffects) {
            Entity bolt = (Entity) o;
            if (seenBolts.add(bolt)) CloudEffects.trail(
                bolt.posX,
                bolt.posY,
                bolt.posZ,
                bolt.posX,
                bolt.posY + COLUMN_HEIGHT,
                bolt.posZ,
                MIN_RADIUS,
                0.15F,
                4F);
        }

        for (Object o : w.loadedEntityList) {
            Entity entity = (Entity) o;
            if (entity instanceof EntityPlayer) {
                player((EntityPlayer) entity);
                continue;
            }
            double stepSq = minStepSq.computeIfAbsent(entity.getClass(), CloudTrails::minStepSq);
            if (stepSq == Double.POSITIVE_INFINITY) continue;
            trail(
                entity,
                stepSq(entity) > stepSq,
                Math.max(MIN_RADIUS, entity.width * ModConfig.cloudRocketTrailRadiusPerWidth),
                0);
        }
    }

    private static double minStepSq(Class<?> c) {
        if (nameHas(c, "Rocket", "Missile", "Meteor")) return MIN_STEP_SQ;
        // Vanilla arrows, snowballs etc. are too common, mobs are not projectiles
        if (EntityLivingBase.class.isAssignableFrom(c) || c.getName()
            .startsWith("net.minecraft.")) return Double.POSITIVE_INFINITY;
        return FAST_STEP_SQ;
    }

    private void player(EntityPlayer player) {
        boolean active = player.ridingEntity == null && clearBelow(player)
            && (stepSq(player) > FAST_PLAYER_SQ || jetpack(player) && player.posY - player.prevPosY > MIN_CLIMB);
        float radius = ModConfig.cloudPlayerTrailRadius;
        // Laid ahead of the player by the distance they cover while it opens, so they fly into open air, not a wall
        trail(player, active, radius, CloudEffects.defaultExpandSeconds(radius) * 20 + LEAD_MARGIN_TICKS);
    }

    /**
     * True if there is nothing solid within GROUND_CLEARANCE blocks under the player's feet: flying, falling or
     * gliding,
     * not jumping. Also works for other players, whose onGround the client doesn't know.
     */
    private static boolean clearBelow(EntityPlayer player) {
        int x = MathHelper.floor_double(player.posX), z = MathHelper.floor_double(player.posZ);
        int feet = MathHelper.floor_double(player.boundingBox.minY);
        for (int y = feet - 1; y >= feet - GROUND_CLEARANCE; y--) {
            if (player.worldObj.getBlock(x, y, z)
                .getMaterial()
                .blocksMovement()) return false;
        }
        return true;
    }

    private boolean jetpack(EntityPlayer player) {
        ItemStack chest = player.getCurrentArmor(2);
        return chest != null && isJetpack.computeIfAbsent(
            chest.getItem()
                .getClass(),
            c -> nameHas(c, "Jetpack")) || mpsModule(chest, "Jetpack")
            || mpsModule(player.getCurrentArmor(0), "Jet Boots");
    }

    private static double stepSq(Entity entity) {
        double dx = entity.posX - entity.prevPosX, dy = entity.posY - entity.prevPosY,
            dz = entity.posZ - entity.prevPosZ;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Extends the entity's trail to where it will be in {@code leadTicks} at its current speed. */
    private static void trail(Entity entity, boolean active, float radius, float leadTicks) {
        if (active && entity.posY > MIN_Y) {
            CloudEffects.trailPoint(
                entity,
                entity.posX + (entity.posX - entity.prevPosX) * leadTicks,
                entity.posY + (entity.posY - entity.prevPosY) * leadTicks,
                entity.posZ + (entity.posZ - entity.prevPosZ) * leadTicks,
                radius);
        } else {
            // A later burn starts a new trail instead of bridging the gap
            CloudEffects.endTrail(entity);
        }
    }

    /** Modular Powersuits keeps installed modules under "mmmpsmod", switched off ones with Active = false. */
    private static boolean mpsModule(ItemStack stack, String module) {
        if (stack == null || !stack.hasTagCompound()) return false;
        NBTTagCompound modules = stack.getTagCompound()
            .getCompoundTag("mmmpsmod");
        if (!modules.hasKey(module)) return false;
        NBTTagCompound tag = modules.getCompoundTag(module);
        return !tag.hasKey("Active") || tag.getBoolean("Active");
    }

    private static boolean nameHas(Class<?> c, String... words) {
        String name = c.getSimpleName()
            .toLowerCase();
        for (String word : words) {
            if (name.contains(word.toLowerCase())) return true;
        }
        return false;
    }
}
