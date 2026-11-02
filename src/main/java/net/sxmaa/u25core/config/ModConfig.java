package net.sxmaa.u25core.config;

import net.sxmaa.u25core.U25Core;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = U25Core.MODID)
@Config.RequiresMcRestart
public class ModConfig {

    @Config.Comment("Toggle some unused features in EnderIO if present, such as Gas Conduits, make item conduits more useful.")
    @Config.DefaultBoolean(true)
    public static boolean enderIOTweaks;

    @Config.Comment("Add Galacticraft's NASA workbench recipes (rockets, buggy, cargo rocket, astro miner) when nothing else has. GTNH's Galacticraft leaves them to the GTNH coremod.")
    @Config.DefaultBoolean(true)
    public static boolean galacticraftBenchRecipes;

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

    @Config.Comment("Trust a passed world session.lock check for a second instead of re-reading the file on every chunk save. Another instance taking the world over is still caught within a second.")
    @Config.DefaultBoolean(true)
    public static boolean cacheSessionLockCheck;

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

    @Config.Comment("Fix an EnderIO dimensional transceiver power dupe: a sender offered its full budget to every receiver on a channel and was only charged for what it had left, so extra receivers got power for free.")
    @Config.DefaultBoolean(true)
    public static boolean fixTransceiverPowerDupe;

    @Config.Comment("RotaryCraft Worktable recipes to remove, by output, as mod:name or mod:name:meta. Applied when loading completes.")
    @Config.DefaultStringList({ "ElectriCraft:electricraft_item_rfbattery", "ElectriCraft:electricraft_item_placer:6",
        "ElectriCraft:electricraft_item_placer:11", "ElectriCraft:electricraft_item_battery:4",
        "ElectriCraft:electricraft_item_battery:5", "ReactorCraft:reactorcraft_item_placer:33",
        "ReactorCraft:reactorcraft_item_placer:36", "RotaryCraft:rotarycraft_item_machine:71" })
    public static String[] worktableRemove;

    @Config.Comment("RF per tick all AE2 RF/EU P2P tunnels of one ME grid may send across Quantum Network Bridges, per live bridge link in that grid. EU counts as 4 RF. Stacks with the per-tunnel caps. 0 removes the cap.")
    @Config.DefaultInt(40000)
    @Config.RangeInt(min = 0)
    public static int qnbRFPerTick;

    @Config.Comment("Caps ChromatiCraft's energy adjacency upgrade boost per generator/reactor, summed over all upgrades around it, as a fraction (0.5 = at most +50% output; uncapped tier 8 is +3100%, +1500% on IC2 reactors). Below 0 disables the cap.")
    @Config.DefaultDouble(0.5)
    @Config.RangeDouble(min = -1, max = 100)
    public static double chromaAdjacencyMaxBoost;

    @Config.Comment("Multiplier on a normal ReactorCraft turbine's torque and power (so its generator's RF/EU and any RotaryCraft shaft output), whatever the steam source. Its steam comes as steam blocks, which don't record which reactor made them.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double reactorTurbineMultiplier;

    @Config.Comment("Multiplier on a ReactorCraft HiP turbine's output for the share of its steam from fission (water) reactors.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double reactorFissionSteamMultiplier;

    @Config.Comment("Multiplier on a ReactorCraft HiP turbine's output for the share of its steam from breeder (sodium), thorium and HTGR (pebble bed) reactors.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double reactorSodiumSteamMultiplier;

    @Config.Comment("Multiplier on a ReactorCraft HiP turbine's output for the share of its steam from the fusion reactor (tokamak).")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double reactorFusionSteamMultiplier;

    @Config.Comment("RF per tick a NuclearCraft lithium-ion battery block takes in and puts out. 0 leaves NuclearCraft's (its whole capacity).")
    @Config.DefaultInt(20000)
    @Config.RangeInt(min = 0)
    public static int ncLiIonBlockRFPerTick;

    @Config.Comment("RF/t a Galacticraft aluminium wire network (any plain wire in it) hands each acceptor. 0 removes the cap.")
    @Config.DefaultInt(2000)
    @Config.RangeInt(min = 0)
    public static int gcWireTier1RFPerTick;

    @Config.Comment("RF/t a Galacticraft heavy aluminium wire network (all heavy wire) hands each acceptor. 0 removes the cap.")
    @Config.DefaultInt(20000)
    @Config.RangeInt(min = 0)
    public static int gcWireTier2RFPerTick;

    @Config.Comment("Fix DeeperCaves ores with a drop range of 0 (incendite) throwing when broken or shown in NEI.")
    @Config.DefaultBoolean(true)
    public static boolean deeperCavesOreDrops;

    @Config.Comment("Item that activates End portal frames instead of eyes of ender, as mod:name or mod:name:meta. Eyes still find strongholds and work in recipes. Empty keeps vanilla.")
    @Config.DefaultString("")
    public static String endPortalActivator;

    @Config.Comment("With NotSoSimpleClouds: modded explosions push the clouds away, weather machines open them up, rockets, missiles, fast projectiles, flying players, jetpacks and lightning leave trails through them.")
    @Config.DefaultBoolean(true)
    public static boolean cloudCompat;

    @Config.Comment("Cloud trail radius of rockets and missiles, in blocks per block of entity width (at least 8).")
    @Config.DefaultFloat(8)
    @Config.RangeFloat(min = 0)
    public static float cloudRocketTrailRadiusPerWidth;

    @Config.Comment("Radius of the tunnel players flying, falling or using a jetpack clear through the clouds, in blocks. Clouds are 8-block cubes.")
    @Config.DefaultFloat(18)
    @Config.RangeFloat(min = 0)
    public static float cloudPlayerTrailRadius;

    @Config.Comment("Radius of the tunnel a player gliding with a hang glider clears through the clouds, in blocks.")
    @Config.DefaultFloat(8)
    @Config.RangeFloat(min = 0)
    public static float cloudGliderTrailRadius;

    @Config.Comment("Seconds of a player's current speed the tunnel is opened ahead of them. Raise it if fast flyers end up at the front edge of the opening; the opening drifts with the wind meanwhile, so a long lead can leave them at its side.")
    @Config.DefaultFloat(0.4F)
    @Config.RangeFloat(min = 0)
    public static float cloudPlayerTrailLeadSeconds;

    @Config.Comment("Seconds until the clouds have flowed back into a player's tunnel.")
    @Config.DefaultFloat(180)
    @Config.RangeFloat(min = 1)
    public static float cloudPlayerTrailSeconds;

    @Config.Comment("Radius of the sphere of sky a DefenseTech or NuclearCraft nuke clears, in blocks. A full IC2 nuke and DefenseTech's antimatter clear twice as far, NuclearCraft's antimatter bomb three times. The shockwave pushes the clouds further out for a while.")
    @Config.DefaultFloat(325)
    @Config.RangeFloat(min = 0)
    public static float cloudNukeRadius;

    @Config.Comment("Seconds until the clouds have flowed back over a nuclear blast.")
    @Config.DefaultFloat(600)
    @Config.RangeFloat(min = 1)
    public static float cloudNukeSeconds;

    @Config.Comment("Radius of the opening in the clouds above a weather machine (Draconic, RotaryCraft, EnderIO's weather obelisk) when it changes the weather, in blocks.")
    @Config.DefaultFloat(48)
    @Config.RangeFloat(min = 0)
    public static float cloudWeatherMachineRadius;
}
