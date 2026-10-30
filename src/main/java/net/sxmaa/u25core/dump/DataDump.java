package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.sxmaa.u25core.U25Core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import codechicken.nei.ItemList;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.DataDumper;
import codechicken.nei.config.Option;
import codechicken.nei.config.OptionList;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Data dumps for pack balancing, written to .minecraft/dumps/u25core. Client only, needs NEI. */
public final class DataDump {

    /**
     * recipes: methods.json and recipes.jsonl. items: items.jsonl. nei: NEI's own dumpers. sources: sources.jsonl,
     * where code makes or uses items. worldgen: worldgen.json, sampled terrain per dimension. icons: icons.png and
     * icons.json for the items in recipes.jsonl, rendered on the client thread after the others.
     */
    public static final List<String> PARTS = Arrays.asList("recipes", "items", "nei", "sources", "worldgen", "icons");

    static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private DataDump() {}

    public static boolean ready() {
        return NEIClientConfig.isLoaded() && ItemList.loadFinished;
    }

    public static File dir() {
        return new File(Minecraft.getMinecraft().mcDataDir, "dumps/u25core");
    }

    private static final Queue<Runnable> MAIN_THREAD = new ConcurrentLinkedQueue<>();
    private static volatile boolean running;

    /**
     * Runs the dump on its own thread so the game keeps running (NEI queries handlers off-thread too), then hands the
     * summary to {@code done} on the client thread. Returns false when a dump is already running.
     */
    public static synchronized boolean runAsync(Collection<String> parts, Consumer<String> done) {
        if (running) return false;
        running = true;
        List<String> threaded = new java.util.ArrayList<>(parts);
        boolean icons = threaded.remove("icons");
        Thread thread = new Thread(() -> {
            String summary = "failed";
            try {
                summary = threaded.isEmpty() ? "" : run(threaded);
            } finally {
                String result = summary;
                MAIN_THREAD.add(() -> {
                    if (icons) {
                        Minecraft.getMinecraft()
                            .displayGuiScreen(new IconDump(dir(), info -> {
                                writeManifest(dir(), part("icons", info));
                                running = false;
                                done.accept(result + " icons " + info.get("count") + " in " + dir().getAbsolutePath());
                            }));
                    } else {
                        running = false;
                        done.accept(result);
                    }
                });
            }
        }, "u25core dump");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    /** Hands finished dumps back to the client thread. */
    public static final class Ticker {

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent event) {
            for (Runnable task; (task = MAIN_THREAD.poll()) != null;) task.run();
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            Runnable task = event.phase == TickEvent.Phase.END ? WorldgenDump.SERVER_THREAD.poll() : null;
            if (task != null) task.run();
        }
    }

    /** Runs the parts, writes manifest.json and returns a summary line. */
    public static String run(Collection<String> parts) {
        File dir = dir();
        dir.mkdirs();
        JsonObject results = new JsonObject();
        StringBuilder summary = new StringBuilder();
        for (String part : parts) {
            JsonObject info = new JsonObject();
            long start = System.currentTimeMillis();
            try {
                int count = "recipes".equals(part) ? RecipeDump.write(dir, info)
                    : "items".equals(part) ? items(dir)
                        : "sources".equals(part) ? SourcesDump.write(dir, info)
                            : "worldgen".equals(part) ? WorldgenDump.write(dir, info) : neiDumps(dir, info);
                info.addProperty("count", count);
                summary.append(part)
                    .append(' ')
                    .append(count)
                    .append(", ");
            } catch (Throwable t) {
                U25Core.LOG.error("Dump part {} failed", part, t);
                info.addProperty("error", t.toString());
                summary.append(part)
                    .append(" failed, ");
            }
            info.addProperty("millis", System.currentTimeMillis() - start);
            results.add(part, info);
        }
        writeManifest(dir, results);
        return summary + "in " + dir.getAbsolutePath();
    }

    private static JsonObject part(String name, JsonObject info) {
        JsonObject results = new JsonObject();
        results.add(name, info);
        return results;
    }

    /**
     * Writes manifest.json with these parts' results; parts that didn't run keep their files, so their entries stay.
     */
    private static synchronized void writeManifest(File dir, JsonObject results) {
        JsonObject manifest = new JsonObject();
        manifest.addProperty(
            "timestamp",
            Instant.now()
                .toString());
        JsonArray mods = new JsonArray();
        for (ModContainer mod : Loader.instance()
            .getActiveModList()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", mod.getModId());
            entry.addProperty("version", mod.getVersion());
            mods.add(entry);
        }
        manifest.add("mods", mods);
        try (java.io.Reader in = Files
            .newBufferedReader(new File(dir, "manifest.json").toPath(), StandardCharsets.UTF_8)) {
            JsonObject previous = new com.google.gson.JsonParser().parse(in)
                .getAsJsonObject()
                .getAsJsonObject("parts");
            for (java.util.Map.Entry<String, JsonElement> entry : previous.entrySet())
                if (!results.has(entry.getKey())) results.add(entry.getKey(), entry.getValue());
        } catch (Exception ignored) {}
        manifest.add("parts", results);
        try {
            writeJson(new File(dir, "manifest.json"), manifest);
        } catch (IOException e) {
            U25Core.LOG.error("Writing the dump manifest failed", e);
        }
    }

    /** Every item NEI lists, with NBT variants: id, display name, ore names. */
    private static int items(File dir) throws IOException {
        int count = 0;
        try (Writer out = writer(new File(dir, "items.jsonl"))) {
            for (ItemStack stack : ItemList.items) {
                JsonObject item = new JsonObject();
                item.addProperty("id", RecipeDump.id(stack));
                try {
                    item.addProperty("name", stack.getDisplayName());
                } catch (Throwable ignored) {}
                JsonArray ores = new JsonArray();
                for (int ore : OreDictionary.getOreIDs(stack)) ores.add(prim(OreDictionary.getOreName(ore)));
                if (ores.size() > 0) item.add("ore", ores);
                out.write(item.toString());
                out.write('\n');
                count++;
            }
        }
        return count;
    }

    /** Runs every NEI data dumper (items, blocks, handlers, item panel, biomes, potions, ...) into nei/. */
    private static int neiDumps(File dir, JsonObject info) throws IOException {
        File nei = new File(dir, "nei");
        nei.mkdirs();
        JsonArray failed = new JsonArray();
        int count = 0;
        for (DataDumper dumper : dumpers(NEIClientConfig.getOptionList(), new java.util.ArrayList<>())) {
            String name = dumper.name.substring(dumper.name.lastIndexOf('.') + 1);
            // dumpTo reports back through the options GUI, which isn't open: write the rows ourselves, as CSV
            try (Writer out = writer(new File(nei, name + ".csv"))) {
                out.write(DataDumper.concat(dumper.header()));
                out.write('\n');
                for (String[] row : dumper.dump(0)) {
                    out.write(DataDumper.concat(row));
                    out.write('\n');
                }
                count++;
            } catch (Throwable t) {
                failed.add(prim(name + ": " + t));
            }
        }
        if (failed.size() > 0) info.add("errors", failed);
        return count;
    }

    private static List<DataDumper> dumpers(OptionList list, List<DataDumper> found) {
        for (Option option : list.optionList) {
            if (option instanceof DataDumper) found.add((DataDumper) option);
            else if (option instanceof OptionList) dumpers((OptionList) option, found);
        }
        return found;
    }

    static Writer writer(File file) throws IOException {
        return Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
    }

    static void writeJson(File file, JsonElement json) throws IOException {
        try (Writer out = writer(file)) {
            GSON.toJson(json, out);
        }
    }

    static JsonPrimitive prim(String value) {
        return new JsonPrimitive(value);
    }
}
