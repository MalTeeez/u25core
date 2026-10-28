package net.sxmaa.u25core;

import net.minecraftforge.common.MinecraftForge;
import net.sxmaa.u25core.radiation.RadiationConfig;
import net.sxmaa.u25core.radiation.client.RadiationClientHandler;

import cpw.mods.fml.common.FMLCommonHandler;
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
    }

}
