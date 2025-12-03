package net.sxmaa.u25core;

import cpw.mods.fml.common.Loader;

public class ModIntegration {

    public static boolean ENDER_IO;
    public static boolean STRUCTURELIB;
    public static boolean BR_6343;

    static void init() {
        ENDER_IO = Loader.isModLoaded("EnderIO");
        STRUCTURELIB = Loader.isModLoaded("structurelib");
        BR_6343 = Loader.isModLoaded("blockrenderer6343");
    }
}
