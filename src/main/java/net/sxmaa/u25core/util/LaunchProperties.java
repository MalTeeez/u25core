package net.sxmaa.u25core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import net.minecraft.launchwrapper.Launch;

/**
 * Launch options can also come from u25core-dump.properties in the game directory, for launchers that don't pass
 * extra JVM arguments through (Prism rewrites them while it runs). Real -D properties win.
 */
public final class LaunchProperties {

    public static final String FILE = "u25core-dump.properties";

    private LaunchProperties() {}

    /** Copies the file's keys starting with {@code prefix} into the system properties that aren't set yet. */
    public static void load(String prefix) {
        File file = new File(Launch.minecraftHome, FILE);
        if (!file.isFile()) return;
        Properties props = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            System.err.println("Couldn't read " + file + ": " + e);
            return;
        }
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix) && System.getProperty(key) == null) {
                System.setProperty(key, props.getProperty(key));
            }
        }
    }
}
