package net.sxmaa.u25core;

import cpw.mods.fml.common.Loader;

public class ModIntegration {

    public static boolean isEnderIOLoaded;

    static void init() {
        isEnderIOLoaded = Loader.isModLoaded("enderio");
    }
}
