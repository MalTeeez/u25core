package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.sxmaa.u25core.U25Core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Unattended dump: with -Du25core.dump=all (or env U25CORE_DUMP=all) the client dumps once a world is loaded and NEI is
 * ready, and with -Du25core.dumpExit=true (or U25CORE_DUMP_EXIT=true) quits. HeadlessNH joins the world, with
 * -Dheadlessnh.active=true -Dheadlessnh.singleplayer=true.
 */
public class HeadlessDump {

    /**
     * Written to the game dir when the dump is done. Doubles as HeadlessNH's gate, so it stays in the world until then:
     * -Dheadlessnh.gate.worldloaded=.u25core-dump.done
     */
    public static final String DONE_MARKER = ".u25core-dump.done";

    private final List<String> parts;
    private final boolean exit;

    private HeadlessDump(List<String> parts, boolean exit) {
        this.parts = parts;
        this.exit = exit;
    }

    /** The trigger asked for at launch, or null. */
    public static HeadlessDump fromLaunch() {
        String request = setting("u25core.dump", "U25CORE_DUMP");
        if (request == null) return null;
        List<String> parts = CommandDump.parts(request);
        if (parts == null) {
            U25Core.LOG.error("Unknown dump part {}, use all or one of {}", request, DataDump.PARTS);
            return null;
        }
        if (!Loader.isModLoaded("headlessnh"))
            U25Core.LOG.warn("Dump waits for a world, but HeadlessNH isn't loaded to join one");
        return new HeadlessDump(parts, Boolean.parseBoolean(setting("u25core.dumpExit", "U25CORE_DUMP_EXIT")));
    }

    private static String setting(String property, String env) {
        String value = System.getProperty(property);
        return value != null ? value : System.getenv(env);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || !DataDump.ready()) return;
        FMLCommonHandler.instance()
            .bus()
            .unregister(this);
        DataDump.runAsync(parts, summary -> {
            U25Core.LOG.info("Dump finished: {}", summary);
            try {
                new File(mc.mcDataDir, DONE_MARKER).createNewFile();
            } catch (IOException e) {
                U25Core.LOG.error("Couldn't write {}", DONE_MARKER, e);
            }
            if (exit) mc.shutdown();
        });
    }
}
