package net.sxmaa.u25core.radiation;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

/** Radiation state of one chunk. */
public final class ChunkRadiation {

    /** Levels below this (10% of the default baseline) count as nothing, so the chunk stops being tracked. */
    static final double IDLE_LEVEL = 1.0E-8;

    /** Current ambient level in Sv/h, relaxing towards its sources. */
    public double level;
    /** Lingering contamination in Sv/h, decaying over time. */
    public double contamination;
    /** Whether the last update found any emitting tile, item or scrubber in the chunk. */
    public boolean hasSources;
    /** World time of the last update, so a chunk that was unloaded catches up on decay. */
    long lastUpdate;
    /** Sources strong enough for Cherenkov glow found in the last update, or null: x, y, z, emission. */
    List<double[]> hotSources;

    public boolean isIdle() {
        return !hasSources && level < IDLE_LEVEL && contamination < IDLE_LEVEL;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setDouble("level", level);
        tag.setDouble("contamination", contamination);
        tag.setLong("lastUpdate", lastUpdate);
    }

    public void readFromNBT(NBTTagCompound tag) {
        level = tag.getDouble("level");
        contamination = tag.getDouble("contamination");
        lastUpdate = tag.getLong("lastUpdate");
        // Re-checked on the first update
        hasSources = true;
    }
}
