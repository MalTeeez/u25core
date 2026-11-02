package net.sxmaa.u25core.config;

import net.sxmaa.u25core.U25Core;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * Look of the block break and block hit particles. Deliberately not {@code RequiresMcRestart}: every value is read
 * when a particle spawns or ticks, so edits in the in game config apply to the next block you break.
 */
@Config(modid = U25Core.MODID, category = "particles")
public class ParticleConfig {

    @Config.Comment("Draw block break and block hit particles as small tumbling cubes of the block texture instead of flat quads.")
    @Config.DefaultBoolean(true)
    public static boolean fancyBlockParticles;

    @Config.Comment("Cubes spawned per axis of a broken block, so the count is this cubed (4 = 64 cubes). Raise it for a denser burst at a higher cost, or lower it to spend less.")
    @Config.DefaultInt(4)
    @Config.RangeInt(min = 1, max = 8)
    public static int piecesPerAxis;

    @Config.Comment("Size of a break cube, as a multiple of the vanilla digging particle.")
    @Config.DefaultFloat(0.75F)
    @Config.RangeFloat(min = 0.05F, max = 3F)
    public static float pieceScale;

    @Config.Comment("Gravity on the cubes, as a multiple of the block's own particle gravity.")
    @Config.DefaultFloat(1F)
    @Config.RangeFloat(min = 0F, max = 5F)
    public static float pieceGravity;

    @Config.Comment("Tumble speed of the cubes. 0 freezes them in the orientation they spawned in.")
    @Config.DefaultFloat(1F)
    @Config.RangeFloat(min = 0F, max = 5F)
    public static float pieceSpin;

    @Config.Comment("Shortest lifetime of a cube, in ticks.")
    @Config.DefaultInt(10)
    @Config.RangeInt(min = 1, max = 400)
    public static int minLifetime;

    @Config.Comment("Longest lifetime of a cube, in ticks. Each cube picks its own lifetime in between.")
    @Config.DefaultInt(55)
    @Config.RangeInt(min = 1, max = 400)
    public static int maxLifetime;

    @Config.Comment("Give each cube a random size, so a burst does not look uniform.")
    @Config.DefaultBoolean(true)
    public static boolean randomScale;

    @Config.Comment("Let each cube shrink and fade at a slightly different rate.")
    @Config.DefaultBoolean(true)
    public static boolean randomFade;

    @Config.Comment("Cubes bounce off walls instead of sliding to a stop against them.")
    @Config.DefaultBoolean(true)
    public static boolean bounceOffWalls;

    @Config.Comment("Cubes that land stay where they land, snapped flat, instead of sliding on.")
    @Config.DefaultBoolean(true)
    public static boolean restOnFloor;

    @Config.Comment("Cubes keep their speed for longer on the ground.")
    @Config.DefaultBoolean(false)
    public static boolean lowTraction;

    @Config.Comment("Cubes slow down and drift up in water instead of falling through it.")
    @Config.DefaultBoolean(true)
    public static boolean waterPhysics;

    @Config.Comment("Also use cubes for the particles shown while a block is being mined.")
    @Config.DefaultBoolean(true)
    public static boolean hitPieces;

    @Config.Comment("Size of a mining cube, as a multiple of the vanilla digging particle.")
    @Config.DefaultFloat(0.5F)
    @Config.RangeFloat(min = 0.05F, max = 3F)
    public static float hitPieceScale;

    @Config.Comment("Blocks that keep vanilla flat particles, as modid:name or modid:name:meta.")
    @Config.DefaultStringList({ "minecraft:redstone_block" })
    public static String[] blacklist;
}
