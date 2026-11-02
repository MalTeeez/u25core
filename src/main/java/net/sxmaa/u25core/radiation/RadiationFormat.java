package net.sxmaa.u25core.radiation;

import net.minecraft.util.EnumChatFormatting;

/** Formatting of Sv values, and the shared log severity scale. */
public final class RadiationFormat {

    private static final String[] PREFIXES = { "n", "u", "m", "", "k", "M" };

    private RadiationFormat() {}

    /** e.g. 12.3 mSv, with the unit suffix appended. */
    public static String format(double value, String unit) {
        if (value <= 0) return "0 " + unit;
        int exp = (int) Math.floor(Math.log10(value) / 3);
        exp = Math.max(-3, Math.min(2, exp));
        double scaled = value / Math.pow(1000, exp);
        return String.format(
            java.util.Locale.ROOT,
            scaled >= 100 ? "%.0f %s%s" : scaled >= 10 ? "%.1f %s%s" : "%.2f %s%s",
            scaled,
            PREFIXES[exp + 3],
            unit);
    }

    /** Sv per roentgen, exposure in air converted to dose. */
    private static final double SV_PER_ROENTGEN = 0.00877;

    /** A rate in both units, e.g. 5.00 Sv/h (570 R/h). */
    public static String rate(double svPerHour) {
        return format(svPerHour, "Sv/h") + " (" + format(svPerHour / SV_PER_ROENTGEN, "R/h") + ")";
    }

    /** Levels up to this count as background, Sv/h. */
    public static final double SAFE = 1.0E-5;

    /** 0 at 10 uSv(/h) up to 1 at 100 Sv(/h), logarithmic. */
    public static double severity(double value) {
        if (value <= SAFE) return 0;
        return Math.min(1, (Math.log10(value) + 5) / 7);
    }

    private static final EnumChatFormatting[] COLORS = { EnumChatFormatting.GREEN, EnumChatFormatting.YELLOW,
        EnumChatFormatting.GOLD, EnumChatFormatting.RED, EnumChatFormatting.DARK_RED };

    /** 0 (background) to 4 (deadly). */
    public static int tier(double value) {
        double s = severity(value);
        return s <= 0 ? 0 : s < 0.3 ? 1 : s < 0.6 ? 2 : s < 0.85 ? 3 : 4;
    }

    public static EnumChatFormatting color(double value) {
        return COLORS[tier(value)];
    }

    /** e.g. 1h 23m, 4m 05s. */
    public static String duration(long seconds) {
        if (seconds >= 3600) return String.format("%dh %02dm", seconds / 3600, seconds / 60 % 60);
        return String.format("%dm %02ds", seconds / 60, seconds % 60);
    }
}
