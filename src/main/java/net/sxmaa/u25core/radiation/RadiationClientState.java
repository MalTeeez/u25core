package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.List;

/** Radiation values synced to this client. Plain fields, only touched from the client thread and packet handler. */
public final class RadiationClientState {

    public static volatile double dose, rate, level;
    public static volatile List<double[]> hotSources = new ArrayList<>();
    public static volatile long hotSourcesTime;

    private RadiationClientState() {}
}
