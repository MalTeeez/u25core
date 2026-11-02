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
    /** Faster descents than this, in blocks per tick, count as falling, not flying. Free fall passes it in 4 ticks. */
    private static final double MAX_FLIGHT_DESCENT = 0.25;
    /** Clouds sit at y 128-200; trails far below them would only evict effects that show. */
    private static final double MIN_Y = 100;
    /** Clouds are 8-block cubes, smaller trails barely show. */
    private static final float MIN_RADIUS = 8;
    /** Seconds a player's trail takes to open: a fast cushion of air shoving the clouds aside ahead of them. */
    private static final float PLAYER_EXPAND_SECONDS = 0.2F;
    /**
     * Largest explosion core, in blocks. Its outer pressure wave reaches 1.5x as far, ~1500, the cloud render distance.
     */
    private static final float MAX_EXPLOSION_RADIUS = 1000;
    /**
     * The renderer pushes clouds out by this fraction of an effect's radius; that much is cleared, the rest is the wall
     * of shoved cloud around it.
     */
    private static final float CLEARED_FRACTION = 0.4F;
    /** Lightning and weather machine columns reach this far up, through any cloud layer. */
    private static final double COLUMN_HEIGHT = 400;

    private static final float[] NO_WIND = new float[3];

    /** Squared step per tick an entity class needs to leave a trail, infinite for none. */
    private final Map<Class<?>, Double> minStepSq = new HashMap<>();
    private final Map<Class<?>, Boolean> isJetpack = new HashMap<>();
    private final Map<Class<?>, Boolean> isGlider = new HashMap<>();
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
            } else if (e[0] == CloudCompat.NUKE) {
                nuke(e[1], e[2], e[3], e[4]);
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
                0,
                0,
                0);
        }
    }

    private static double minStepSq(Class<?> c) {
        if (nameHas(c, "Rocket", "Missile", "Meteor")) return MIN_STEP_SQ;
        // OpenBlocks' hang glider follows its player, whose own trail covers it
        if (nameHas(c, "Glider")) return Double.POSITIVE_INFINITY;
        // Vanilla arrows, snowballs etc. are too common, mobs are not projectiles
        if (EntityLivingBase.class.isAssignableFrom(c) || c.getName()
            .startsWith("net.minecraft.")) return Double.POSITIVE_INFINITY;
        return FAST_STEP_SQ;
    }

    private void player(EntityPlayer player) {
        ItemStack held = player.getHeldItem();
        boolean gliding = held != null && isGlider.computeIfAbsent(
            held.getItem()
                .getClass(),
            c -> nameHas(c, "Glider"));
        boolean fast = stepSq(player) > FAST_PLAYER_SQ;
        double dy = player.posY - player.prevPosY;
        // A plain fall isn't flight: without a jetpack or glider only flying players (descending slowly, or flying
        // as far as this client knows, which is only true for its own player) cut through the clouds
        boolean flying = player.capabilities.isFlying || dy > -MAX_FLIGHT_DESCENT;
        boolean active = player.ridingEntity == null && clearBelow(player)
            && (jetpack(player) ? fast || dy > MIN_CLIMB : fast && (gliding || flying));
        float radius = (gliding ? ModConfig.cloudGliderTrailRadius : ModConfig.cloudPlayerTrailRadius)
            / CLEARED_FRACTION;
        float expand = PLAYER_EXPAND_SECONDS;
        // Laid ahead of the player, so they fly into open air rather than a wall; a longer lead drifts further
        // with the wind before they get there
        trail(
            player,
            active,
            radius,
            ModConfig.cloudPlayerTrailLeadSeconds * 20,
            expand,
            ModConfig.cloudPlayerTrailSeconds);
    }

    /** A core that stays open for minutes, and a wider pressure wave that shoves the clouds beyond it for a while. */
    private static void nuke(double x, double y, double z, float scale) {
        float core = ModConfig.cloudNukeRadius * scale / CLEARED_FRACTION, wave = core * 1.5F;
        CloudEffects.shockwave(x, y, z, core, CloudEffects.defaultExpandSeconds(core), ModConfig.cloudNukeSeconds);
        CloudEffects
            .shockwave(x, y, z, wave, CloudEffects.defaultExpandSeconds(wave), CloudEffects.defaultHealSeconds(wave));
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

    /**
     * Extends the entity's trail to where it will be in {@code leadTicks} at its current speed. Heal seconds 0 uses the
     * default timing for the radius.
     */
    private static void trail(Entity entity, boolean active, float radius, float leadTicks, float expandSeconds,
        float healSeconds) {
        if (active && entity.posY > MIN_Y) {
            // The lead point drifts with the clouds until the entity gets there, so lead by the velocity relative to
            // them: the hole then opens where the entity will cross the drifting clouds
            float[] wind = leadTicks > 0 ? CloudEffects.windVelocity() : NO_WIND;
            double x = entity.posX + (entity.posX - entity.prevPosX - wind[0] / 20) * leadTicks,
                y = entity.posY + (entity.posY - entity.prevPosY - wind[1] / 20) * leadTicks,
                z = entity.posZ + (entity.posZ - entity.prevPosZ - wind[2] / 20) * leadTicks;
            if (healSeconds > 0) CloudEffects.trailPoint(entity, x, y, z, radius, expandSeconds, healSeconds);
            else CloudEffects.trailPoint(entity, x, y, z, radius);
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
