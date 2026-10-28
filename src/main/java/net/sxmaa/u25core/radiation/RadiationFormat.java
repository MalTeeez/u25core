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

    /** 0 at 10 uSv(/h) up to 1 at 100 Sv(/h), logarithmic. */
    public static double severity(double value) {
        if (value <= 1.0E-5) return 0;
        return Math.min(1, (Math.log10(value) + 5) / 7);
    }

    public static EnumChatFormatting color(double value) {
        double s = severity(value);
        if (s <= 0) return EnumChatFormatting.GREEN;
        if (s < 0.3) return EnumChatFormatting.YELLOW;
        if (s < 0.6) return EnumChatFormatting.GOLD;
        if (s < 0.85) return EnumChatFormatting.RED;
        return EnumChatFormatting.DARK_RED;
    }
}
