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

    @Config.Comment("Prevent Item Frames from ticking in unloaded chunks when AM2 is installed.")
    @Config.DefaultBoolean(true)
    public static boolean preventAM2ItemFrameChunkloading;

    @Config.Comment("Buff the Draconic Evolution dragons and Chaos Guardian, rebalance the armor shields and allow one more upgrade core tier on the armor and weapons.")
    @Config.DefaultBoolean(true)
    public static boolean draconicEvolutionBalance;

    @Config.Comment("Draconic Armor only saves from death while it has emergency charges, which are gained with a near-full shield at a high energy cost.")
    @Config.DefaultBoolean(true)
    public static boolean draconicEvolutionEmergencyCharges;

    @Config.Comment("Restrict heavy water: Mekanism only captures it on Mars, ReactorCraft only extracts it on Europa and (slowly) Mars, without needing an ocean biome.")
    @Config.DefaultBoolean(true)
    public static boolean restrictHeavyWater;

    @Config.Comment("Dimension ID of Mars, for restrictHeavyWater.")
    @Config.DefaultInt(-687)
    public static int heavyWaterMarsDimension;

    @Config.Comment("Dimension ID of Europa, for restrictHeavyWater.")
    @Config.DefaultInt(-92788)
    public static int heavyWaterEuropaDimension;

    @Config.Comment("ReactorCraft heavy water extracted on Mars is divided by this.")
    @Config.DefaultInt(10)
    @Config.RangeInt(min = 1)
    public static int reactorCraftMarsHeavyWaterDivisor;

    @Config.Comment("RF an AE2 RF P2P input tunnel passes on per tick, from all its sources together. 0 removes the cap.")
    @Config.DefaultInt(16000)
    @Config.RangeInt(min = 0)
    public static int p2pRFPerTick;

    @Config.Comment("EU an AE2 EU P2P input tunnel passes on per tick, from all its sources together. 0 removes the cap.")
    @Config.DefaultInt(4000)
    @Config.RangeInt(min = 0)
    public static int p2pEUPerTick;

    @Config.Comment("RF a Thermal Expansion Tesseract sends to its frequency per tick, from all its sources together. 0 removes the cap.")
    @Config.DefaultInt(20000000)
    @Config.RangeInt(min = 0)
    public static int tesseractRFPerTick;

    @Config.Comment("RF a Mekanism Quantum Entangloporter moves per tick in each direction, into its frequency and out of the block, converted to J at Mekanism's own rate. 0 removes the cap.")
    @Config.DefaultInt(320000)
    @Config.RangeInt(min = 0)
    public static int entangloporterRFPerTick;
}
