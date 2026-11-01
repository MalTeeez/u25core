package net.sxmaa.u25core.util;

import net.sxmaa.u25core.config.ModConfig;

import Reika.ReactorCraft.Registry.ReactorType;

/** A ReactorCraft turbine that knows which reactors its steam came from (the HiP turbine). */
public interface ReactorSteamSource {

    /** Output multiplier for the steam it took in last, weighted by source. */
    double u25core$steamMultiplier();

    static double multiplierFor(ReactorType type) {
        switch (type) {
            case FISSION:
                return ModConfig.reactorFissionSteamMultiplier;
            case BREEDER:
            case HTGR:
            case THORIUM:
                return ModConfig.reactorSodiumSteamMultiplier;
            case FUSION:
                return ModConfig.reactorFusionSteamMultiplier;
            default: // Solar exchanger, unknown
                return 1;
        }
    }
}
