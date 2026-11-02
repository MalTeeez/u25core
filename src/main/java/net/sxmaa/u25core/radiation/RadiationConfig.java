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

    @Config.Comment("Fraction of radiation an armor piece blocks for the body part it covers (head 15%, chest 45%, legs 30%, feet 10%), as modid:item[:meta]=0..1. A missing piece lets its share through.")
    @Config.DefaultStringList({ "IC2:itemArmorHazmatHelmet=0.999", "IC2:itemArmorHazmatChestplate=0.999",
        "IC2:itemArmorHazmatLeggings=0.999", "IC2:itemArmorRubBoots=0.999",
        "ReactorCraft:reactorcraft_item_hazhelmet=0.999", "ReactorCraft:reactorcraft_item_hazchest=0.999",
        "ReactorCraft:reactorcraft_item_hazlegs=0.999", "ReactorCraft:reactorcraft_item_hazboots=0.999",
        "IC2:itemArmorQuantumHelmet=0.9999", "IC2:itemArmorQuantumChestplate=0.9999", "IC2:itemArmorQuantumLegs=0.9999",
        "IC2:itemArmorQuantumBoots=0.9999" })
    public static String[] armorProtection;

    @Config.Comment("Fraction of radiation each Modular Powersuits armor piece with the Hazmat module installed blocks.")
    @Config.DefaultDouble(0.999)
    @Config.RangeDouble(min = 0, max = 1)
    public static double mpsHazmatProtection;

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

    @Config.Comment("Shielding blocks, as key=neutronAbsorb,radiationBlock[,moderates] with fractions 0..1 for one block (a metre). Keys: block:<modid:name>[:meta] or ore:<name>. Blocks without an entry shield by material: dense solids 99.9%, wood 92%, wool and snow 80%, glass and partial blocks 90%, leaves 10%.")
    @Config.DefaultStringList({ "ore:blockLead=0.75,1", "ore:blockSteel=0.9,1", "ore:blockBoron=0.95,1",
        "block:minecraft:obsidian=0.5,1", "block:minecraft:water=0.3,0.99,true",
        "block:minecraft:flowing_water=0.3,0.99,true", "ore:blockGraphite=0.05,1,true",
        "block:minecraft:iron_block=0.6,1", "block:minecraft:bedrock=0.975,1", "block:IC2:blockAlloy=0.6,1",
        "block:IC2:blockAlloyGlass=0.4,1", "block:IC2:blockReinforcedFoam=0.6,1", "block:minecraft:stone=0.1,1",
        "block:minecraft:cobblestone=0.1,1", "block:minecraft:dirt=0.05,0.998", "block:minecraft:sand=0.05,0.998" })
    public static String[] shielding;

    @Config.Comment("Fraction of neutrons absorbed by one block of a fluid that has no shielding entry.")
    @Config.DefaultDouble(0.2)
    @Config.RangeDouble(min = 0, max = 1)
    public static double fluidNeutronAbsorb;

    @Config.Comment("Fraction of radiation blocked by one block of a fluid that has no shielding entry (a metre of water stops ~99% of gamma, scatter included).")
    @Config.DefaultDouble(0.99)
    @Config.RangeDouble(min = 0, max = 1)
    public static double fluidRadiationBlock;

    @Config.Comment("Shielding blocks around an entity (up to 8 blocks in each direction) reduce the ambient radiation it gets, like a bunker. Sources are always shielded by the blocks around them.")
    @Config.DefaultBoolean(true)
    public static boolean entityShielding;

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

    @Config.Comment("Ticks one hour of exposure lasts, so Sv/h rates build dose faster than real time (72000). At 4000, 5 minutes of play are 1.5 hours of exposure and 5 Sv/h reaches the default lethal dose in about 5 minutes.")
    @Config.DefaultInt(4000)
    @Config.RangeInt(min = 1)
    public static int doseHourTicks;

    @Config.Comment("Dose in Sv from which an entity dies within seconds instead of over a minute.")
    @Config.DefaultDouble(50)
    @Config.RangeDouble(min = 0.01)
    public static double instantDeathDose;

    @Config.Comment("Dose in Sv from which an entity dies over about a minute, unless treatment brings it back below.")
    @Config.DefaultDouble(8)
    @Config.RangeDouble(min = 0.01)
    public static double lethalDose;

    @Config.Comment("Half-life of accumulated dose in ticks, i.e. natural recovery (72000 = one hour of play).")
    @Config.DefaultInt(72000)
    @Config.RangeInt(min = 20)
    public static int doseHalfLife;

    @Config.Comment("Effects by dose, as percentOfLethalDose=potion@amplifier;potion@amplifier. Potion names as in /effect.")
    @Config.DefaultStringList({ "10=hunger@0", "25=hunger@1;weakness@0", "50=hunger@1;weakness@1;moveSlowdown@0",
        "75=hunger@2;weakness@2;moveSlowdown@1;confusion@0;wither@0" })
    public static String[] doseEffects;

    @Config.Comment("Effects while exposed, by the rate getting past armor and shielding, as Sv/h=potion@amplifier;potion@amplifier. Combined with the dose effects, the stronger one wins.")
    @Config.DefaultStringList({ "50=weakness@0", "150=weakness@0;confusion@0" })
    public static String[] rateEffects;

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
