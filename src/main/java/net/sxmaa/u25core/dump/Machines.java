package net.sxmaa.u25core.dump;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * Fixed machine rates, read off a catalyst's tile entity: most machines keep their energy per tick, processing time or
 * shaft requirements in fields set by the constructor (Mekanism BASE_ENERGY_PER_TICK/BASE_TICKS_REQUIRED, IC2
 * defaultEnergyConsume/defaultOperationLength, RotaryCraft minimum torque/omega/power...).
 */
final class Machines {

    static final Pattern RATE = Pattern.compile(
        "(?i).*(energy|power|usage|consum|eu|rf|mj|tick|time|duration|operation|length|torque|omega|speed|mana|cost|fuel|heat|temp).*");
    static final Pattern NOISE = Pattern.compile(
        "(?i).*(stored|last|prev|client|render|anim|rotation|phi|sound|update|counter|tickcount|sincesync|sched|cache|slot|timeout|failed|sync).*");

    private static Map<String, String> modsBySource;

    private Machines() {}

    /** Rate fields of the machine this catalyst places, or null when it isn't a tile entity block. */
    static JsonObject read(ItemStack stack) {
        try {
            if (stack == null || stack.getItem() == null) return null;
            JsonObject rotary = rotaryCraft(stack);
            if (rotary != null) return rotary;
            TileEntity tile = tile(stack);
            if (tile == null) return null;
            JsonObject fields = fields(tile);
            if (fields.entrySet()
                .isEmpty()) return null;
            JsonObject machine = new JsonObject();
            machine.addProperty("id", RecipeDump.id(stack));
            machine.addProperty(
                "tile",
                tile.getClass()
                    .getName());
            machine.add("fields", fields);
            return machine;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Rate-like numbers a tile entity holds, by field name. */
    static JsonObject fields(TileEntity tile) {
        JsonObject fields = new JsonObject();
        // ReactorCraft's shaft power receivers report their minimums through getters
        for (String getter : new String[] { "getMinTorque", "getMinSpeed", "getMinPower" }) {
            try {
                Object value = tile.getClass()
                    .getMethod(getter)
                    .invoke(tile);
                if (value instanceof Number && ((Number) value).longValue() > 1) fields.addProperty(
                    getter.substring(3, 4)
                        .toLowerCase() + getter.substring(4),
                    (Number) value);
            } catch (Throwable ignored) {}
        }
        for (Class<?> c = tile.getClass(); c != null && c != TileEntity.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                String name = field.getName();
                if (field.isSynthetic() || fields.has(name)
                    || !RATE.matcher(name)
                        .matches()
                    || NOISE.matcher(name)
                        .matches())
                    continue;
                Class<?> type = field.getType();
                if (!(type.isPrimitive() && type != boolean.class && type != char.class)) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(Modifier.isStatic(field.getModifiers()) ? null : tile);
                    if (((Number) value).doubleValue() != 0) fields.addProperty(name, (Number) value);
                } catch (Throwable ignored) {}
            }
        }
        return fields;
    }

    /** The tile entity a catalyst places, built without a world. */
    private static TileEntity tile(ItemStack stack) throws Exception {
        // ReactorCraft places its machines with one item, damage = ReactorTiles index
        if ("Reika.ReactorCraft.Items.ItemReactorPlacer".equals(
            stack.getItem()
                .getClass()
                .getName())) {
            Object[] tiles = (Object[]) Class.forName("Reika.ReactorCraft.Registry.ReactorTiles")
                .getField("TEList")
                .get(null);
            Object entry = tiles[stack.getItemDamage()];
            return (TileEntity) entry.getClass()
                .getMethod("createTEInstanceForRender")
                .invoke(entry);
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == null || block == Blocks.air) return null;
        int meta = stack.getItem()
            .getMetadata(stack.getItemDamage());
        return block.hasTileEntity(meta) ? block.createTileEntity(null, meta) : null;
    }

    /**
     * RotaryCraft machines are placed by one item (damage = machine index) and need shaft power: minimum torque (Nm),
     * omega (rad/s) and power (W) from its PowerReceivers registry, per stage for multi-valued ones. 1 means no
     * minimum.
     */
    private static JsonObject rotaryCraft(ItemStack stack) throws Exception {
        if (!"Reika.RotaryCraft.Items.Placers.ItemMachinePlacer".equals(
            stack.getItem()
                .getClass()
                .getName()))
            return null;
        Object receiver = Class.forName("Reika.RotaryCraft.Registry.PowerReceivers")
            .getMethod("getEnumFromMachineIndex", int.class)
            .invoke(null, stack.getItemDamage());
        if (receiver == null) return null;
        JsonObject fields = new JsonObject();
        String[][] names = { { "minT", "minTorque" }, { "minS", "minOmega" }, { "minP", "minPower" },
            { "torques", "torques" }, { "speeds", "omegas" }, { "powers", "powers" } };
        for (String[] name : names) {
            Field field = receiver.getClass()
                .getDeclaredField(name[0]);
            field.setAccessible(true);
            Object value = field.get(receiver);
            if (value instanceof Number && ((Number) value).longValue() > 1) {
                fields.addProperty(name[1], (Number) value);
            } else if (value != null && value.getClass()
                .isArray()) {
                    JsonArray stages = new JsonArray();
                    for (int i = 0; i < Array.getLength(value); i++)
                        stages.add(new JsonPrimitive((Number) Array.get(value, i)));
                    if (stages.size() > 0) fields.add(name[1], stages);
                }
        }
        if (fields.entrySet()
            .isEmpty()) return null;
        JsonObject machine = new JsonObject();
        machine.addProperty("id", RecipeDump.id(stack));
        machine.addProperty("tile", receiver.toString());
        machine.addProperty("unit", "W");
        machine.add("fields", fields);
        return machine;
    }

    /** The mod whose jar a class comes from, for handlers NEI doesn't attribute. */
    static synchronized String modOf(Class<?> clazz) {
        if (modsBySource == null) {
            modsBySource = new HashMap<>();
            for (ModContainer mod : Loader.instance()
                .getActiveModList()) {
                File source = mod.getSource();
                // First mod per jar wins, it's usually the main one
                if (source != null) modsBySource.putIfAbsent(source.getAbsolutePath(), mod.getModId());
            }
        }
        try {
            URL location = clazz.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            String path = location.getPath();
            int jar = path.indexOf(".jar");
            if (jar >= 0) path = path.substring(0, jar + 4);
            if (path.startsWith("file:")) path = path.substring(5);
            return modsBySource.get(new File(java.net.URLDecoder.decode(path, "UTF-8")).getAbsolutePath());
        } catch (Throwable t) {
            return null;
        }
    }
}
