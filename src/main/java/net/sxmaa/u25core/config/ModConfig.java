package net.sxmaa.u25core.config;

import net.sxmaa.u25core.U25Core;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = U25Core.MODID)
@Config.RequiresMcRestart
public class ModConfig {

    @Config.Comment("Toggle some unused features in EnderIO if present, such as Gas Conduits, make item conduits more useful.")
    @Config.DefaultBoolean(true)
    public static boolean enderIOTweaks;

    @Config.Comment("Add StructureLib support for other mods multiblocks so they show up in NEI & ingame with the projector.")
    @Config.DefaultBoolean(true)
    public static boolean externalStructureLibMultiblocks;

    @Config.Comment("Prevents a light & mood update (World.func_147467_a) if the provided chunk parameter (p_147467_3_) is null. ")
    @Config.DefaultBoolean(true)
    public static boolean enableLightAndMoodUpdateCrashPrevention;

    @Config.Comment("Add the DefenseTech Thermobaric/Nuclear Explosion sound to IC2 Nuke Explosions. Requires both mods to be active.")
    @Config.DefaultBoolean(true)
    public static boolean addDefenseTechExplosionSoundToIC2Nuke;

    @Config.Comment("Nerf the ChromatiCraft Enhanced Potion Pendants.")
    @Config.DefaultBoolean(true)
    public static boolean nerfCCEnhancedPendants;

    @Config.Comment("Add a cast cost to the ChromatiCraft handheld guns.")
    @Config.DefaultBoolean(true)
    public static boolean nerfCCGuns;

    @Config.Comment("Catch rare crashes due to a missing blockrenderer if inside of that block. Seems to be caused by chromaticraft.")
    @Config.DefaultBoolean(false)
    public static boolean catchInsideOfBlockCrash;

    @Config.Comment("Modify RTG World Gen to be more noisy and raggy in mountainous terrain.")
    @Config.DefaultBoolean(true)
    public static boolean rtgMountainousTerrainRougher;

    @Config.Comment("Increase the rarity of RTG Volcanoes, on top of the seemingly broken config rarity.")
    @Config.DefaultBoolean(true)
    public static boolean rtgIncreaseVolcanoRarity;

    @Config.Comment("Set the Git Revision property on startup for showing with modernsplash.")
    @Config.DefaultBoolean(true)
    public static boolean setGitRev;

    @Config.Comment("Optional prefix to use with the Git Revision property.")
    @Config.DefaultString("")
    public static String gitRevPrefix;
}
