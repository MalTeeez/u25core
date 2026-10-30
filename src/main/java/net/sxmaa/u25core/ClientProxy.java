package net.sxmaa.u25core;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.sxmaa.u25core.dump.CommandDump;
import net.sxmaa.u25core.dump.DataDump;
import net.sxmaa.u25core.dump.HeadlessDump;
import net.sxmaa.u25core.radiation.RadiationConfig;
import net.sxmaa.u25core.radiation.client.RadiationClientHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        if (RadiationConfig.enabled) {
            RadiationClientHandler handler = new RadiationClientHandler();
            MinecraftForge.EVENT_BUS.register(handler);
            FMLCommonHandler.instance()
                .bus()
                .register(handler);
        }
        if (Loader.isModLoaded("NotEnoughItems")) Dumps.register();
    }

    // Keeps NEI classes out of the proxy
    private static final class Dumps {

        static void register() {
            ClientCommandHandler.instance.registerCommand(new CommandDump());
            FMLCommonHandler.instance()
                .bus()
                .register(new DataDump.Ticker());
            HeadlessDump headless = HeadlessDump.fromLaunch();
            if (headless != null) FMLCommonHandler.instance()
                .bus()
                .register(headless);
        }
    }

}
