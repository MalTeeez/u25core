package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.sxmaa.u25core.U25Core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import codechicken.nei.ItemList;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.registry.GameRegistry;

/** Every NEI crafting handler (methods.json) and every recipe it shows (recipes.jsonl). */
final class RecipeDump {

    private RecipeDump() {}

    /** Returns the number of distinct recipes written. */
    static int write(File dir, JsonObject partInfo) throws IOException {
        List<ICraftingHandler> handlers = new ArrayList<>(GuiCraftingRecipe.craftinghandlers);
        handlers.addAll(GuiCraftingRecipe.serialCraftingHandlers);
        JsonArray methods = new JsonArray();
        Set<String> seen = new HashSet<>();
        int total = 0, failed = 0;
        try (Writer out = DataDump.writer(new File(dir, "recipes.jsonl"))) {
            for (ICraftingHandler handler : handlers) {
                JsonObject method = describe(handler);
                String unit = method.has("unit") ? method.get("unit")
                    .getAsString() : null;
                int count = 0;
                long start = System.currentTimeMillis();
                try {
                    ICraftingHandler all = null;
                    try {
                        if (handler instanceof TemplateRecipeHandler)
                            all = ((TemplateRecipeHandler) handler).getAllRecipeHandler();
                        else if (handler.getOverlayIdentifier() != null)
                            all = handler.getRecipeHandler(handler.getOverlayIdentifier());
                    } catch (Throwable t) {
                        method.addProperty("allError", t.toString());
                    }
                    List<String> lines;
                    if (all != null && all.numRecipes() > 0) {
                        method.addProperty("source", "all");
                        lines = lines(all, handlerId(handler), unit);
                    } else if (handler instanceof TemplateRecipeHandler || handler.getOverlayIdentifier() != null) {
                        method.addProperty("source", "items");
                        lines = byItems(handler, unit, method);
                    } else {
                        // Custom handlers without an id are views, not recipes; some open a GUI when queried
                        method.addProperty("source", "none");
                        lines = new ArrayList<>();
                    }
                    for (String text : lines) {
                        if (seen.add(text)) {
                            out.write(text);
                            out.write('\n');
                            count++;
                        }
                    }
                } catch (Throwable t) {
                    method.addProperty("error", t.toString());
                    failed++;
                }
                long millis = System.currentTimeMillis() - start;
                if (millis > 5000) U25Core.LOG.info("Dumping {} took {}ms", handlerId(handler), millis);
                method.addProperty("millis", millis);
                method.addProperty("recipes", count);
                methods.add(method);
                total += count;
            }
        }
        DataDump.writeJson(new File(dir, "methods.json"), methods);
        partInfo.addProperty("handlers", handlers.size());
        partInfo.addProperty("handlerErrors", failed);
        return total;
    }

    private static JsonObject describe(ICraftingHandler handler) {
        JsonObject method = new JsonObject();
        method.addProperty("handler", handlerId(handler));
        method.addProperty(
            "class",
            handler.getClass()
                .getName());
        method.addProperty("name", handler.getRecipeName());
        method.addProperty("overlay", handler.getOverlayIdentifier());
        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
        if (info != null) {
            method.addProperty("mod", info.getModId());
            String unit = Units.of(info.getModId());
            if (unit != null) method.addProperty("unit", unit);
        }
        JsonArray catalysts = new JsonArray();
        for (PositionedStack catalyst : RecipeCatalysts.getRecipeCatalysts(handler)) {
            catalysts.add(DataDump.prim(id(catalyst.item)));
        }
        method.add("catalysts", catalysts);
        return method;
    }

    private static String handlerId(ICraftingHandler handler) {
        String id = handler.getHandlerId();
        return id != null ? id
            : handler.getClass()
                .getName();
    }

    /**
     * For handlers that can't list all their recipes: asks them for every NEI item, in parallel on NEI's pool like
     * NEI's own recipe dump, until the per-handler budget (-Du25core.dumpItemBudget, seconds) runs out.
     */
    private static List<String> byItems(ICraftingHandler handler, String unit, JsonObject method) throws Exception {
        long deadline = System.currentTimeMillis() + Long.getLong("u25core.dumpItemBudget", 60) * 1000;
        AtomicBoolean truncated = new AtomicBoolean();
        String id = handlerId(handler);
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        ItemList.forkJoinPool.submit(
            () -> ItemList.items.parallelStream()
                .forEach(stack -> {
                    if (System.currentTimeMillis() > deadline) {
                        truncated.set(true);
                        return;
                    }
                    try {
                        lines.addAll(lines(handler.getRecipeHandler("item", stack), id, unit));
                    } catch (Throwable ignored) {}
                }))
            .get();
        if (truncated.get()) method.addProperty("truncated", true);
        return new ArrayList<>(lines);
    }

    private static List<String> lines(ICraftingHandler loaded, String handlerId, String unit) {
        List<String> lines = new ArrayList<>();
        if (loaded == null) return lines;
        for (int i = 0; i < loaded.numRecipes(); i++) {
            try {
                lines.add(line(loaded, i, handlerId, unit));
            } catch (Throwable ignored) {
                // One broken recipe shouldn't cost the handler's others
            }
        }
        return lines;
    }

    private static String line(ICraftingHandler loaded, int i, String handlerId, String unit) {
        JsonObject line = new JsonObject();
        line.addProperty("handler", handlerId);
        line.add("inputs", stacks(loaded.getIngredientStacks(i), false));
        JsonArray outputs = new JsonArray();
        PositionedStack result = loaded.getResultStack(i);
        if (result != null) outputs.add(stack(result, false));
        for (PositionedStack other : loaded.getOtherStacks(i)) outputs.add(stack(other, true));
        line.add("outputs", outputs);
        if (loaded instanceof TemplateRecipeHandler && i < ((TemplateRecipeHandler) loaded).arecipes.size()) {
            CachedReader.read(((TemplateRecipeHandler) loaded).arecipes.get(i), line);
        }
        if (line.has("energy") && unit != null) {
            line.addProperty("unit", unit);
            double rf = Units.toRF(unit);
            if (rf > 0) line.addProperty(
                "rf",
                line.get("energy")
                    .getAsDouble() * rf);
        }
        return line.toString();
    }

    private static JsonArray stacks(List<PositionedStack> stacks, boolean other) {
        JsonArray array = new JsonArray();
        for (PositionedStack stack : stacks) array.add(stack(stack, other));
        return array;
    }

    private static JsonObject stack(PositionedStack positioned, boolean other) {
        JsonObject json = new JsonObject();
        JsonArray ids = new JsonArray();
        Set<String> ore = null;
        for (ItemStack permutation : positioned.items) {
            ids.add(DataDump.prim(id(permutation)));
            Set<String> names = new LinkedHashSet<>();
            for (int oreId : OreDictionary.getOreIDs(permutation)) names.add(OreDictionary.getOreName(oreId));
            if (ore == null) ore = names;
            else ore.retainAll(names);
        }
        json.add("ids", ids);
        if (positioned.items.length > 1 && ore != null && !ore.isEmpty()) json.addProperty(
            "ore",
            ore.iterator()
                .next());
        json.addProperty("count", positioned.items.length > 0 ? positioned.items[0].stackSize : 0);
        if (positioned.getChance() != PositionedStack.CHANCE_FULL)
            json.addProperty("chance", positioned.getChance() / (double) PositionedStack.CHANCE_FULL);
        if (other) json.addProperty("other", true);
        return json;
    }

    /** mod:name:meta, with NBT appended when present. */
    static String id(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "null";
        String name;
        try {
            name = GameRegistry.findUniqueIdentifierFor(stack.getItem())
                .toString();
        } catch (Throwable t) {
            // Unregistered items (display-only ones some mods put in NEI)
            name = "unregistered:" + stack.getItem()
                .getClass()
                .getName();
        }
        String id = name + ":" + stack.getItemDamage();
        return stack.hasTagCompound() ? id + stack.getTagCompound() : id;
    }

    /**
     * Best-effort energy, time and fluids from a handler's cached recipe and the objects one level below it, by field
     * name. Many machine handlers keep the mod's recipe object there.
     */
    static final class CachedReader {

        private CachedReader() {}

        static void read(Object cached, JsonObject line) {
            JsonArray fluids = new JsonArray();
            scan(cached, "", line, fluids, 0);
            if (fluids.size() > 0) line.add("fluids", fluids);
        }

        private static void scan(Object object, String path, JsonObject line, JsonArray fluids, int depth) {
            for (Class<?> c = object.getClass(); c != null && c != Object.class
                && c != TemplateRecipeHandler.CachedRecipe.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    // Synthetic this$0 leads to the handler, whose fields aren't per recipe (animation ticks)
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                    Object value;
                    try {
                        field.setAccessible(true);
                        value = field.get(object);
                    } catch (Throwable t) {
                        continue;
                    }
                    if (value == null) continue;
                    String name = path + field.getName();
                    if (value instanceof FluidStack) fluids.add(fluid((FluidStack) value, name));
                    else if (value instanceof Number) number(name, (Number) value, line);
                    else if (value instanceof Collection) {
                        for (Object element : (Collection<?>) value)
                            if (element instanceof FluidStack) fluids.add(fluid((FluidStack) element, name));
                    } else if (depth == 0 && !(value instanceof ItemStack || value instanceof PositionedStack
                        || value instanceof Map
                        || value instanceof CharSequence
                        || value.getClass()
                            .isArray()
                        || value.getClass()
                            .getName()
                            .startsWith("java."))) {
                                scan(value, name + ".", line, fluids, depth + 1);
                            }
                }
            }
        }

        private static void number(String name, Number value, JsonObject line) {
            String lower = name.toLowerCase();
            String leaf = lower.substring(lower.lastIndexOf('.') + 1);
            String key = null;
            if (leaf.matches(".*(energy|power|eu|rf|mana|essence|lp|cost|fuel).*")) key = "energy";
            else if (leaf.matches(".*(time|duration|tick).*")) key = "ticks";
            if (key == null || line.has(key) || value.doubleValue() <= 0) return;
            line.addProperty(key, value);
            line.addProperty(key + "Field", name);
        }

        private static JsonObject fluid(FluidStack stack, String field) {
            JsonObject json = new JsonObject();
            json.addProperty(
                "id",
                stack.getFluid() != null ? stack.getFluid()
                    .getName() : "null");
            json.addProperty("mb", stack.amount);
            json.addProperty("field", field);
            return json;
        }
    }
}
