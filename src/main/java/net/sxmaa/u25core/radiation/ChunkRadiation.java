package net.sxmaa.u25core.radiation;

import net.minecraft.nbt.NBTTagCompound;

/** Radiation state of one chunk. */
public final class ChunkRadiation {

    /** Current ambient level in Sv/h, relaxing towards its sources. */
    public double level;
    /** Lingering contamination in Sv/h, decaying over time. */
    public double contamination;
    /** Whether the last update found any emitting tile, item or scrubber in the chunk. */
    public boolean hasSources;

    public boolean isIdle() {
        return !hasSources && level < 1.0E-12 && contamination < 1.0E-12;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setDouble("level", level);
        tag.setDouble("contamination", contamination);
    }

    public void readFromNBT(NBTTagCompound tag) {
        level = tag.getDouble("level");
        contamination = tag.getDouble("contamination");
        // Re-checked on the first update
        hasSources = true;
    }
}
