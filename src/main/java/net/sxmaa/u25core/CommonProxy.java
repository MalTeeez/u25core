package net.sxmaa.u25core;

import net.minecraft.item.Item;
import net.sxmaa.u25core.common.multiblocks.DebugItem;
import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.registry.U25Multiblock;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    static Item debugItem;

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(ModConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        ModIntegration.init();

        if (ModConfig.externalStructureLibMultiblocks) {
            U25Multiblock.init();
        }

        GameRegistry.registerItem(debugItem = new DebugItem(), debugItem.getUnlocalizedName());
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
