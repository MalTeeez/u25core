package net.sxmaa.u25core.dump;

import cpw.mods.fml.common.Loader;

/** Energy unit a mod's recipes use, and its RF value. */
final class Units {

    private Units() {}

    static String of(String modId) {
        if (modId == null) return null;
        switch (modId) {
            case "IC2":
                return "EU";
            case "Mekanism":
            case "MekanismGenerators":
                return "J";
            case "ThermalExpansion":
            case "EnderIO":
            case "NuclearCraft":
            case "DraconicEvolution":
                return "RF";
            case "Botania":
                return "mana";
            case "AWWayofTime":
                return "LP";
            case "Thaumcraft":
                return "vis";
            default:
                return null;
        }
    }

    /** RF per unit, 0 when it doesn't convert. EU at the usual 1:4. */
    static double toRF(String unit) {
        switch (unit) {
            case "RF":
                return 1;
            case "EU":
                return 4;
            case "J":
                return Loader.isModLoaded("Mekanism") ? Mekanism.toRF() : 0.4;
            default:
                return 0;
        }
    }

    private static final class Mekanism {

        static double toRF() {
            return mekanism.api.MekanismConfig.general.TO_TE;
        }
    }
}
