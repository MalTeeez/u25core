package net.sxmaa.u25core.radiation;

import net.sxmaa.u25core.U25Core;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = U25Core.MODID, category = "radiation")
@Config.RequiresMcRestart
public class RadiationConfig {

    @Config.Comment("Enable the radiation framework. Without it, the API reports no radiation and releases nothing.")
    @Config.DefaultBoolean(true)
    public static boolean enabled;

    @Config.Comment("Background radiation everywhere, in Sv/h.")
    @Config.DefaultDouble(1.0E-7)
    @Config.RangeDouble(min = 0)
    public static double baseline;

    @Config.Comment("Extra background per dimension, as dimensionId=Sv/h.")
    @Config.DefaultStringList({ "-687=2.0E-5", "-92788=0.2", "-28=1.0E-5" })
    public static String[] dimensionBackground;

    @Config.Comment("Extra background per biome, as biomeName=Sv/h.")
    @Config.DefaultStringList({})
    public static String[] biomeBackground;

    @Config.Comment("Radioactive materials in Sv/h per unit (item, or mB of fluid/gas). Keys: ore:<name>, item:<modid:name>[:meta], fluid:<name>, gas:<name>. Overrides values registered through the API.")
    @Config.DefaultStringList({ "ore:ingotUranium=1.0E-6", "ore:dustUranium=1.0E-6", "ore:oreUranium=2.5E-7",
        "ore:blockUranium=9.0E-6", "ore:ingotPlutonium=2.0E-5", "ore:dustPlutonium=2.0E-5", "ore:ingotThorium=3.0E-7",
        "ore:dustThorium=3.0E-7", "gas:nuclearWaste=0.01", "gas:plutonium=0.02", "gas:polonium=0.05" })
    public static String[] radioactiveMaterials;

    @Config.Comment("Armor resistance against radiation, as modid:item[:meta]=Sv/h. Resistances of worn pieces add up.")
    @Config.DefaultStringList({ "IC2:itemArmorHazmatHelmet=0.25", "IC2:itemArmorHazmatChestplate=0.4",
        "IC2:itemArmorHazmatLeggings=0.2", "IC2:itemArmorRubBoots=0.15", "ReactorCraft:reactorcraft_item_hazhelmet=2.5",
        "ReactorCraft:reactorcraft_item_hazchest=4", "ReactorCraft:reactorcraft_item_hazlegs=2",
        "ReactorCraft:reactorcraft_item_hazboots=1.5", "IC2:itemArmorQuantumHelmet=25",
        "IC2:itemArmorQuantumChestplate=25", "IC2:itemArmorQuantumLegs=25", "IC2:itemArmorQuantumBoots=25" })
    public static String[] armorResistance;

    @Config.Comment("Resistance in Sv/h of each Modular Powersuits armor piece with the Hazmat module installed.")
    @Config.DefaultDouble(2.5)
    @Config.RangeDouble(min = 0)
    public static double mpsHazmatResistance;

    @Config.Comment("Potions other mods use as radiation, as potionName=Sv/h per potion level. Instead of the potion, entities get this rate as dose for its duration.")
    @Config.DefaultStringList({ "ic2.potion.radiation=0.2" })
    public static String[] radiationPotions;

    @Config.Comment("Forestry radioactive bees expose nearby entities to this rate instead of damaging them, in Sv/h.")
    @Config.DefaultDouble(2)
    @Config.RangeDouble(min = 0)
    public static double forestryBeeRadiation;

    @Config.Comment("ExtraPlanets radioactive water exposes entities in it to this rate, in Sv/h.")
    @Config.DefaultDouble(0.5)
    @Config.RangeDouble(min = 0)
    public static double radioactiveWaterRadiation;

    @Config.Comment("Dose an IC2 Terra Wart removes, in Sv.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double terraWartTreatment;

    @Config.Comment("Emission of an active ReactorCraft fuel core, in Sv/h per fuel item in it.")
    @Config.DefaultDouble(0.5)
    @Config.RangeDouble(min = 0)
    public static double reactorCraftCoreEmission;

    @Config.Comment("Emission of stored ReactorCraft waste, in Sv/h per waste item.")
    @Config.DefaultDouble(0.05)
    @Config.RangeDouble(min = 0)
    public static double reactorCraftWasteEmission;

    @Config.Comment("Emission of a running IC2 nuclear reactor, in Sv/h per EU/t it outputs.")
    @Config.DefaultDouble(0.01)
    @Config.RangeDouble(min = 0)
    public static double ic2ReactorEmission;

    @Config.Comment("Replace ReactorCraft's own radiation (potion exposure, radiation clouds) with this framework.")
    @Config.DefaultBoolean(true)
    public static boolean replaceReactorCraftRadiation;

    @Config.Comment("Shielding blocks, as key=neutronAbsorb,radiationBlock[,moderates] with fractions 0..1. Keys: block:<modid:name>[:meta] or ore:<name>.")
    @Config.DefaultStringList({ "ore:blockLead=0.75,0.5", "ore:blockSteel=0.9,0.95", "ore:blockBoron=0.95,0.3",
        "block:minecraft:obsidian=0.5,0.8", "block:minecraft:water=0.3,0.1,true",
        "block:minecraft:flowing_water=0.3,0.1,true", "ore:blockGraphite=0.05,0.3,true",
        "block:minecraft:iron_block=0.6,0.7", "block:minecraft:bedrock=0.975,1" })
    public static String[] shielding;

    @Config.Comment("Chunks updated per world per tick. Higher reacts faster, costs more.")
    @Config.DefaultInt(10)
    @Config.RangeInt(min = 1, max = 1000)
    public static int chunksPerTick;

    @Config.Comment("Fraction a chunk level moves towards its sources per second when rising.")
    @Config.DefaultDouble(0.2)
    @Config.RangeDouble(min = 0, max = 1)
    public static double riseRate;

    @Config.Comment("Fraction a chunk level moves towards its sources per second when falling.")
    @Config.DefaultDouble(0.005)
    @Config.RangeDouble(min = 0, max = 1)
    public static double fallRate;

    @Config.Comment("Fraction of the level difference that spreads to each neighbouring chunk per second.")
    @Config.DefaultDouble(0.02)
    @Config.RangeDouble(min = 0, max = 0.25)
    public static double spreadRate;

    @Config.Comment("Half-life of contamination in ticks (72000 = one hour).")
    @Config.DefaultInt(144000)
    @Config.RangeInt(min = 20)
    public static int contaminationHalfLife;

    @Config.Comment("Rain speeds up contamination decay by this factor.")
    @Config.DefaultDouble(3)
    @Config.RangeDouble(min = 1)
    public static double rainCleanupFactor;

    @Config.Comment("Ticks between player dose updates.")
    @Config.DefaultInt(10)
    @Config.RangeInt(min = 1, max = 200)
    public static int playerUpdateInterval;

    @Config.Comment("Dose in Sv at which an entity dies.")
    @Config.DefaultDouble(8)
    @Config.RangeDouble(min = 0.01)
    public static double lethalDose;

    @Config.Comment("Half-life of accumulated dose in ticks, i.e. natural recovery (72000 = one hour).")
    @Config.DefaultInt(288000)
    @Config.RangeInt(min = 20)
    public static int doseHalfLife;

    @Config.Comment("Effects by dose, as percentOfLethalDose=potion@amplifier;potion@amplifier. Potion names as in /effect.")
    @Config.DefaultStringList({ "10=hunger@0", "25=hunger@1;weakness@0", "50=hunger@1;weakness@1;moveSlowdown@0",
        "75=hunger@2;weakness@2;moveSlowdown@1;confusion@0;wither@0" })
    public static String[] doseEffects;

    @Config.Comment("Fraction of the dose kept on respawn.")
    @Config.DefaultDouble(0.75)
    @Config.RangeDouble(min = 0, max = 1)
    public static double respawnDoseKept;

    @Config.Comment("Ticks of radiation immunity after respawning.")
    @Config.DefaultInt(1800)
    @Config.RangeInt(min = 0)
    public static int respawnImmunity;

    @Config.Comment("Undead mobs are immune to radiation.")
    @Config.DefaultBoolean(true)
    public static boolean undeadImmune;

    @Config.Comment("Entity class names (simple or full) that are immune to radiation.")
    @Config.DefaultStringList({})
    public static String[] immuneEntities;

    @Config.Comment("Chunk level in Sv/h above which plants wither and grass dies. 0 disables.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double terrainDamageLevel;

    @Config.Comment("Emission in Sv/h above which a source makes nearby water glow blue (Cherenkov radiation). 0 disables.")
    @Config.DefaultDouble(1)
    @Config.RangeDouble(min = 0)
    public static double cherenkovLevel;

    @Config.Comment("Maximum distance in blocks a neutron travels.")
    @Config.DefaultInt(16)
    @Config.RangeInt(min = 1, max = 64)
    public static int neutronRange;

    @Config.Comment("Scrubbers: fraction of the chunk level each one removes, with diminishing returns for several.")
    @Config.DefaultDouble(0.5)
    @Config.RangeDouble(min = 0, max = 1)
    public static double scrubberEfficiency;

    @Config.Comment("Ticks a scrubber runs on one filter item.")
    @Config.DefaultInt(6000)
    @Config.RangeInt(min = 1)
    public static int scrubberFilterTicks;

    @Config.Comment("Contamination in Sv/h at the centre of an IC2 nuke, scaled by its power relative to a default nuke.")
    @Config.DefaultDouble(50)
    @Config.RangeDouble(min = 0)
    public static double ic2NukeContamination;
}
