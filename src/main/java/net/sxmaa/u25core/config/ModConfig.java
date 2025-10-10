package net.sxmaa.u25core.config;

import net.sxmaa.u25core.U25Core;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = U25Core.MODID)
public class ModConfig {

    @Config.Comment("Toggle some unused features in EnderIO if present, such as Gas Conduits, make item conduits more useful.")
    @Config.DefaultBoolean(false)
    public static boolean enderIOTweaks;
}
