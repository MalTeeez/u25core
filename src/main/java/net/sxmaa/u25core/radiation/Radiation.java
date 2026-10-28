package net.sxmaa.u25core.radiation;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.sxmaa.u25core.api.radiation.RadiationAPI;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

/** Registers the radiation framework. */
public final class Radiation {

    public static Item geigerCounter, dosimeter, antiRad, radProtection;
    public static Block scrubber, geigerBlock;

    private Radiation() {}

    public static void preInit() {
        try {
            ConfigurationManager.registerConfig(RadiationConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
        RadiationAPI.setManager(RadiationManager.INSTANCE);
        if (!RadiationConfig.enabled) return;

        GameRegistry.registerItem(
            geigerCounter = new ItemRadiation(ItemRadiation.Kind.GEIGER, "geiger_counter"),
            "geiger_counter");
        GameRegistry
            .registerItem(dosimeter = new ItemRadiation(ItemRadiation.Kind.DOSIMETER, "dosimeter"), "dosimeter");
        GameRegistry.registerItem(antiRad = new ItemRadiation(ItemRadiation.Kind.ANTI_RAD, "anti_rad"), "anti_rad");
        GameRegistry.registerItem(
            radProtection = new ItemRadiation(ItemRadiation.Kind.RAD_PROTECTION, "rad_protection"),
            "rad_protection");
        GameRegistry.registerBlock(scrubber = new BlockScrubber(), "scrubber");
        GameRegistry.registerBlock(geigerBlock = new BlockGeiger(), "geiger_block");
        GameRegistry.registerTileEntity(TileScrubber.class, "u25core:scrubber");

        RadiationNetwork.init();
        RadiationEventHandler handler = new RadiationEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
    }

    /** After all mods registered their ore dictionary entries. */
    public static void postInit() {
        RadiationRegistry.load();
        if (!RadiationConfig.enabled) return;
        // Defaults, packs can replace them with CraftTweaker
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                geigerCounter,
                "  G",
                "IRI",
                "IQI",
                'G',
                "blockGlass",
                'I',
                "ingotIron",
                'R',
                "dustRedstone",
                'Q',
                "gemQuartz"));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(dosimeter, " S ", "PGP", " P ", 'S', Items.string, 'P', "paneGlass", 'G', "dyeGreen"));
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                new ItemStack(antiRad, 2),
                Items.glass_bottle,
                Items.glass_bottle,
                Items.golden_carrot,
                "dyeOrange",
                Items.sugar));
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                new ItemStack(radProtection, 4),
                Items.sugar,
                Items.potato,
                "dyeBlue",
                Items.glowstone_dust));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                scrubber,
                "IHI",
                "FCF",
                "IHI",
                'I',
                "ingotIron",
                'H',
                Blocks.hopper,
                'F',
                Blocks.iron_bars,
                'C',
                Items.coal));
        GameRegistry.addRecipe(new ShapelessOreRecipe(geigerBlock, geigerCounter, "blockIron", Items.comparator));
    }

    public static void serverStarting(FMLServerStartingEvent event) {
        if (RadiationConfig.enabled) event.registerServerCommand(new CommandRadiation());
    }
}
