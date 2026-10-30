package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.registry.GameData;

/**
 * How each registered dimension is reached. Galacticraft bodies with their rocket tier, the rocket items by tier, and
 * the blocks, items, entities and event handlers whose code sends players to a dimension: calls to
 * transferPlayerToDimension or travelToDimension, followed back through the mods' own teleport helpers, with the
 * target id worked out from what the bytecode passes (constants, config fields and getters, the portal block's own
 * field). Writes dimensions.json.
 */
final class DimensionsDump {

    private static final int DEPTH = 5;
    private static final Set<String> KINDS = new HashSet<>(
        java.util.Arrays.asList("tile", "block", "item", "entity", "event"));

    /**
     * Where a dimension id comes from, as far as the bytecode tells: a constant, static fields and getters, fields and
     * getters on those, the object the method runs on, or one of its parameters.
     */
    private static final class Expr {

        static final int CONST = 0, STATIC = 1, STATIC_CALL = 2, FIELD = 3, CALL = 4, THIS = 5, PARAM = 6;

        final int op, value;
        final Expr of;
        final String owner, name;

        Expr(int op, int value, Expr of, String owner, String name) {
            this.op = op;
            this.value = value;
            this.of = of;
            this.owner = owner;
            this.name = name;
        }

        boolean has(int kind) {
            return op == kind || of != null && of.has(kind);
        }

        /** The caller's view: this and parameters become the receiver and arguments it passed; null when unknown. */
        Expr substitute(Expr receiver, List<Expr> args) {
            if (op == THIS) return receiver;
            if (op == PARAM) return value < args.size() ? args.get(value) : null;
            if (of == null) return this;
            Expr inner = of.substitute(receiver, args);
            return inner == null ? null : new Expr(op, value, inner, owner, name);
        }

        Object eval(Object self) throws Exception {
            switch (op) {
                case CONST:
                    return value;
                case THIS:
                    return self;
                case STATIC:
                case STATIC_CALL: {
                    Class<?> clazz = Class.forName(owner.replace('/', '.'), true, Launch.classLoader);
                    return op == STATIC ? read(clazz, null, name) : invoke(clazz, null, name);
                }
                case FIELD:
                case CALL: {
                    Object target = of.eval(self);
                    if (target == null) return null;
                    return op == FIELD ? read(target.getClass(), target, name)
                        : invoke(target.getClass(), target, name);
                }
                default:
                    return null;
            }
        }

        private static Object read(Class<?> clazz, Object target, String name) throws Exception {
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
                try {
                    java.lang.reflect.Field field = c.getDeclaredField(name);
                    field.setAccessible(true);
                    return field.get(target);
                } catch (NoSuchFieldException ignored) {}
            }
            return null;
        }

        private static Object invoke(Class<?> clazz, Object target, String name) throws Exception {
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
                try {
                    Method method = c.getDeclaredMethod(name);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (NoSuchMethodException ignored) {}
            }
            return null;
        }

        @Override
        public String toString() {
            String simple = owner == null ? "" : owner.substring(owner.lastIndexOf('/') + 1);
            switch (op) {
                case CONST:
                    return String.valueOf(value);
                case STATIC:
                    return simple + "." + name;
                case STATIC_CALL:
                    return simple + "." + name + "()";
                case FIELD:
                    return of + "." + name;
                case CALL:
                    return of + "." + name + "()";
                case THIS:
                    return "this";
                default:
                    return "arg" + value;
            }
        }
    }

    /** A teleport a method makes, to wherever expr says; expr is null when it can't be told. */
    private static final class Effect {

        Expr expr;
        String evidence;
    }

    private final Map<String, List<Effect>> summaries = new HashMap<>();
    private final Set<String> inProgress = new HashSet<>();
    private final Map<String, ClassNode> nodes = new LinkedHashMap<String, ClassNode>(256, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ClassNode> eldest) {
            return size() > 4000;
        }
    };

    private DimensionsDump() {}

    static int write(File dir, JsonObject info) throws IOException {
        return new DimensionsDump().run(dir, info);
    }

    private int run(File dir, JsonObject info) throws IOException {
        Map<Integer, JsonArray> entries = new TreeMap<>();
        JsonArray unresolved = new JsonArray();
        for (SourcesDump.Root root : SourcesDump.registeredRoots()) {
            if (!KINDS.contains(root.kind)) continue;
            Map<String, Effect> found = new LinkedHashMap<>();
            for (String className : withNested(root.name)) {
                ClassNode node = node(className);
                if (node == null) continue;
                for (MethodNode method : node.methods) {
                    for (Effect effect : summary(node.name, method.name, method.desc, 0)) {
                        // A teleport to whatever the caller passes in, from outside: not an entry of its own
                        if (effect.expr != null && effect.expr.has(Expr.PARAM)) continue;
                        found.putIfAbsent(effect.expr + " " + effect.evidence, effect);
                    }
                }
            }
            for (Effect effect : found.values()) {
                Map<Integer, Set<String>> targets = targets(root, effect.expr);
                if (targets.isEmpty()) unresolved.add(entry(root, effect, null));
                for (Map.Entry<Integer, Set<String>> target : targets.entrySet())
                    entries.computeIfAbsent(target.getKey(), d -> new JsonArray())
                        .add(entry(root, effect, target.getValue()));
            }
        }

        vanilla(entries);
        placements(dir, entries);
        Map<Integer, JsonObject> bodies = bodies();
        JsonArray dims = new JsonArray();
        int tiered = 0, entered = 0, none = 0;
        for (int id : DimensionManager.getStaticDimensionIDs()) {
            JsonObject dim = new JsonObject();
            dim.addProperty("id", id);
            try {
                net.minecraft.world.WorldProvider provider = DimensionManager.createProviderFor(id);
                dim.addProperty(
                    "provider",
                    provider.getClass()
                        .getName());
                dim.addProperty("mod", Machines.modOf(provider.getClass()));
                dim.addProperty("name", provider.getDimensionName());
            } catch (Throwable ignored) {}
            JsonObject body = bodies.get(id);
            if (body != null) {
                dim.add("body", body);
                tiered++;
            }
            JsonArray entry = entries.get(id);
            dim.add("entry", entry);
            if (entry != null) entered++;
            if (body == null && entry == null) none++;
            dims.add(dim);
        }
        JsonObject json = new JsonObject();
        json.add("dims", dims);
        json.add("rockets", rockets());
        json.add("unresolved", unresolved);
        DataDump.writeJson(new File(dir, "dimensions.json"), json);
        info.addProperty("withBody", tiered);
        info.addProperty("withEntry", entered);
        info.addProperty("withNothing", none);
        info.addProperty("unresolved", unresolved.size());
        return dims.size();
    }

    /**
     * Dimension ids an effect goes to, with the registered blocks or items going there when it depends on which
     * instance runs it (one portal block per target, say). Empty when it can't be told.
     */
    private static Map<Integer, Set<String>> targets(SourcesDump.Root root, Expr expr) {
        Map<Integer, Set<String>> targets = new TreeMap<>();
        if (expr == null) return targets;
        Map<String, Object> selves = new LinkedHashMap<>();
        if (!expr.has(Expr.THIS)) selves.put(null, null);
        else if ("block".equals(root.kind) || "item".equals(root.kind)) {
            boolean block = "block".equals(root.kind);
            for (Object object : block ? GameData.getBlockRegistry() : GameData.getItemRegistry()) {
                if (!object.getClass()
                    .getName()
                    .equals(root.name)) continue;
                selves.put(
                    block ? GameData.getBlockRegistry()
                        .getNameForObject(object)
                        : GameData.getItemRegistry()
                            .getNameForObject(object),
                    object);
            }
        } else if ("tile".equals(root.kind) && root.tile != null) selves.put(null, root.tile);
        for (Map.Entry<String, Object> self : selves.entrySet()) {
            try {
                Object value = expr.eval(self.getValue());
                if (!(value instanceof Integer)) continue;
                Set<String> ids = targets.computeIfAbsent((Integer) value, k -> new TreeSet<>());
                if (self.getKey() != null) ids.add(self.getKey());
            } catch (Throwable ignored) {}
        }
        return targets;
    }

    /** The vanilla portals, whose classes aren't scanned. */
    private static void vanilla(Map<Integer, JsonArray> entries) {
        String[][] portals = {
            { "-1", "minecraft:portal", "minecraft:obsidian", "minecraft:flint_and_steel",
                "vanilla obsidian frame + fire" },
            { "1", "minecraft:end_portal", "minecraft:end_portal_frame", "minecraft:ender_eye",
                "vanilla stronghold end portal frame + eyes of ender" } };
        for (String[] portal : portals) {
            JsonObject entry = new JsonObject();
            entry.addProperty("kind", "block");
            entry.add("blocks", SourcesDump.strings(Collections.singletonList(portal[1])));
            entry.add("frame", SourcesDump.strings(Collections.singletonList(portal[2])));
            entry.add("activator", SourcesDump.strings(Collections.singletonList(portal[3])));
            entry.addProperty("evidence", portal[4]);
            entry.addProperty("placedInWorld", true);
            entries.computeIfAbsent(Integer.parseInt(portal[0]), d -> new JsonArray())
                .add(entry);
        }
    }

    /**
     * What places each entry block in the world, from sources.jsonl when the sources part has run: the other classes
     * that hand the block to setBlock and the like.
     */
    private static void placements(File dir, Map<Integer, JsonArray> entries) throws IOException {
        File sources = new File(dir, "sources.jsonl");
        if (!sources.isFile()) return;
        Map<String, Set<String>> wanted = new HashMap<>();
        for (JsonArray list : entries.values()) for (com.google.gson.JsonElement element : list) {
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("blocks") || entry.has("placedInWorld")) continue;
            for (com.google.gson.JsonElement block : entry.getAsJsonArray("blocks"))
                wanted.computeIfAbsent(base(block.getAsString()), b -> new HashSet<>());
        }
        Map<String, List<JsonObject>> placers = new HashMap<>();
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        try (java.io.BufferedReader in = java.nio.file.Files
            .newBufferedReader(sources.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
            for (String text; (text = in.readLine()) != null;) {
                if (!text.contains("\"use\":\"output\"")) continue;
                JsonObject line = parser.parse(text)
                    .getAsJsonObject();
                String block = base(
                    line.get("item")
                        .getAsString());
                if (!wanted.containsKey(block)) continue;
                JsonObject placer = new JsonObject();
                for (String key : new String[] { "kind", "class", "method", "evidence" })
                    if (line.has(key)) placer.add(key, line.get(key));
                placers.computeIfAbsent(block, b -> new ArrayList<>())
                    .add(placer);
            }
        }
        for (JsonArray list : entries.values()) for (com.google.gson.JsonElement element : list) {
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("blocks") || entry.has("placedInWorld")) continue;
            JsonArray placedBy = new JsonArray();
            Set<String> seen = new HashSet<>();
            for (com.google.gson.JsonElement block : entry.getAsJsonArray("blocks")) {
                for (JsonObject placer : placers.getOrDefault(base(block.getAsString()), Collections.emptyList())) {
                    String clazz = placer.get("class")
                        .getAsString();
                    // The portal's own code refers to itself all the time
                    if (clazz.equals(
                        entry.get("class")
                            .getAsString())
                        || !seen.add(clazz)) continue;
                    if (placedBy.size() < 10) placedBy.add(placer);
                }
            }
            if (placedBy.size() > 0) {
                entry.add("placedBy", placedBy);
                entry.addProperty("placedInWorld", true);
            }
        }
    }

    /** mod:name of a mod:name(:meta) id. */
    private static String base(String id) {
        String[] parts = id.split(":");
        return parts.length >= 2 ? parts[0] + ":" + parts[1] : id;
    }

    private static JsonObject entry(SourcesDump.Root root, Effect effect, Set<String> instances) {
        JsonObject entry = new JsonObject();
        entry.addProperty("kind", root.kind);
        if (instances != null && !instances.isEmpty())
            entry.add("item".equals(root.kind) ? "items" : "blocks", SourcesDump.strings(instances));
        else {
            if (!root.blocks.isEmpty()) entry.add("blocks", SourcesDump.strings(root.blocks));
            if (!root.items.isEmpty()) entry.add("items", SourcesDump.strings(root.items));
        }
        if (root.entity != null) entry.addProperty("entity", root.entity);
        entry.addProperty("class", root.name);
        if (effect.expr != null && effect.expr.op != Expr.CONST) entry.addProperty("config", effect.expr.toString());
        entry.addProperty("evidence", effect.evidence);
        // Portal blocks that have no item are built in the world: sources.jsonl says what places them
        if ("block".equals(root.kind)) {
            boolean itemless = true;
            for (Object object : GameData.getBlockRegistry()) {
                if (object.getClass()
                    .getName()
                    .equals(root.name) && Item.getItemFromBlock((Block) object) != null) itemless = false;
            }
            if (itemless) entry.addProperty("itemless", true);
        }
        return entry;
    }

    private List<String> withNested(String className) throws IOException {
        List<String> names = new ArrayList<>();
        names.add(className);
        for (int i = 0; i < names.size(); i++) {
            ClassNode node = node(names.get(i));
            if (node == null) continue;
            for (InnerClassNode inner : node.innerClasses) {
                String name = inner.name.replace('/', '.');
                if (inner.name.startsWith(node.name + "$") && !names.contains(name)) names.add(name);
            }
        }
        return names;
    }

    private ClassNode node(String className) throws IOException {
        ClassNode node = nodes.get(className);
        if (node == null && !nodes.containsKey(className)) {
            node = SourcesDump.node(className);
            nodes.put(className, node);
        }
        return node;
    }

    /** The dimension argument's index in a vanilla teleport call, or -1. */
    private static int vanillaDimArg(MethodInsnNode call) {
        switch (call.name) {
            case "transferPlayerToDimension":
            case "func_72356_a":
                return call.desc.startsWith("(Lnet/minecraft/entity/player/EntityPlayerMP;I") ? 1 : -1;
            case "travelToDimension":
            case "func_71027_c":
                return "(I)V".equals(call.desc) ? 0 : -1;
            default:
                return -1;
        }
    }

    /** Teleports a method makes, itself or through the mod methods it calls, up to DEPTH calls down. */
    private List<Effect> summary(String owner, String name, String desc, int depth) {
        String key = owner + "." + name + desc;
        List<Effect> cached = summaries.get(key);
        if (cached != null) return cached;
        if (depth > DEPTH || !inProgress.add(key)) return Collections.emptyList();
        List<Effect> effects = new ArrayList<>();
        try {
            MethodNode method = null;
            String declaring = owner;
            for (String c = owner; c != null && !SourcesDump.vanilla(c) && method == null;) {
                ClassNode node = node(c.replace('/', '.'));
                if (node == null) break;
                for (MethodNode m : node.methods) if (m.name.equals(name) && m.desc.equals(desc)) method = m;
                declaring = c;
                c = node.superName;
            }
            if (method != null && method.instructions.size() > 0) effects = analyze(declaring, method, depth);
        } catch (Throwable ignored) {} finally {
            inProgress.remove(key);
        }
        summaries.put(key, effects);
        return effects;
    }

    private List<Effect> analyze(String owner, MethodNode method, int depth) throws Exception {
        // Cheap pass first: most methods teleport nowhere, only those get the data flow analysis
        Map<AbstractInsnNode, List<Effect>> calls = new LinkedHashMap<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) insn;
            int arg = vanillaDimArg(call);
            if (arg >= 0) {
                Effect effect = new Effect();
                effect.expr = new Expr(Expr.PARAM, arg, null, null, null);
                effect.evidence = call.name;
                calls.put(call, Collections.singletonList(effect));
            } else if (!SourcesDump.vanilla(call.owner)) {
                List<Effect> sub = summary(call.owner, call.name, call.desc, depth + 1);
                if (!sub.isEmpty()) calls.put(call, sub);
            }
        }
        if (calls.isEmpty()) return Collections.emptyList();
        Frame<SourceValue>[] frames = new Analyzer<>(new SourceInterpreter()).analyze(owner, method);
        List<Effect> effects = new ArrayList<>();
        String here = owner.substring(owner.lastIndexOf('/') + 1) + "." + method.name;
        for (Map.Entry<AbstractInsnNode, List<Effect>> call : calls.entrySet()) {
            MethodInsnNode insn = (MethodInsnNode) call.getKey();
            Frame<SourceValue> frame = frames[method.instructions.indexOf(insn)];
            if (frame == null) continue;
            // One stack slot per value here, whatever its size
            int args = Type.getArgumentTypes(insn.desc).length, top = frame.getStackSize();
            Expr receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null
                : expr(frame.getStack(top - args - 1), method, frames, 0);
            List<Expr> argExprs = new ArrayList<>();
            for (int k = 0; k < args; k++) argExprs.add(expr(frame.getStack(top - args + k), method, frames, 0));
            for (Effect sub : call.getValue()) {
                Effect effect = new Effect();
                effect.evidence = here + " > " + sub.evidence;
                effect.expr = sub.expr == null ? null : sub.expr.substitute(receiver, argExprs);
                effects.add(effect);
            }
        }
        return effects;
    }

    /** What a stack or local value is, following loads of locals back to their stores; null when it can't be told. */
    private static Expr expr(SourceValue value, MethodNode method, Frame<SourceValue>[] frames, int depth) {
        if (value == null || value.insns.size() != 1 || depth > 6) return null;
        AbstractInsnNode insn = value.insns.iterator()
            .next();
        Frame<SourceValue> frame = frames[method.instructions.indexOf(insn)];
        Integer constant = SourcesDump.intConstant(insn);
        if (constant != null) return new Expr(Expr.CONST, constant, null, null, null);
        int op = insn.getOpcode();
        if (op == Opcodes.GETSTATIC) {
            FieldInsnNode field = (FieldInsnNode) insn;
            return new Expr(Expr.STATIC, 0, null, field.owner, field.name);
        }
        if (frame == null) return null;
        SourceValue top = frame.getStackSize() > 0 ? frame.getStack(frame.getStackSize() - 1) : null;
        if (op == Opcodes.GETFIELD) {
            FieldInsnNode field = (FieldInsnNode) insn;
            Expr of = expr(top, method, frames, depth + 1);
            return of == null ? null : new Expr(Expr.FIELD, 0, of, field.owner, field.name);
        }
        if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).desc.startsWith("()")) {
            MethodInsnNode call = (MethodInsnNode) insn;
            if (SourcesDump.vanilla(call.owner)) return null;
            if (op == Opcodes.INVOKESTATIC) return new Expr(Expr.STATIC_CALL, 0, null, call.owner, call.name);
            Expr of = expr(top, method, frames, depth + 1);
            return of == null ? null : new Expr(Expr.CALL, 0, of, call.owner, call.name);
        }
        if (op == Opcodes.CHECKCAST) return expr(top, method, frames, depth + 1);
        if (insn instanceof VarInsnNode) {
            int slot = ((VarInsnNode) insn).var;
            if (op >= Opcodes.ISTORE && op <= Opcodes.ASTORE) return expr(top, method, frames, depth + 1);
            if (slot == 0 && (method.access & Opcodes.ACC_STATIC) == 0) return new Expr(Expr.THIS, 0, null, null, null);
            int param = paramIndex(method, slot);
            if (param >= 0) return new Expr(Expr.PARAM, param, null, null, null);
            return expr(frame.getLocal(slot), method, frames, depth + 1);
        }
        return null;
    }

    /** The argument index a local variable slot holds, or -1 when it's a local of the method body. */
    private static int paramIndex(MethodNode method, int slot) {
        int at = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        Type[] args = Type.getArgumentTypes(method.desc);
        for (int i = 0; i < args.length; i++) {
            if (at == slot) return i;
            at += args[i].getSize();
        }
        return -1;
    }

    /** Galacticraft (and ExtraPlanets) celestial bodies by dimension id, with the rocket tier they need. */
    private static Map<Integer, JsonObject> bodies() {
        Map<Integer, JsonObject> bodies = new HashMap<>();
        try {
            Class<?> registry = Class.forName("micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry");
            for (String kind : new String[] { "Planets", "Moons", "Satellites" }) {
                Map<?, ?> registered = (Map<?, ?>) registry.getMethod("getRegistered" + kind)
                    .invoke(null);
                for (Object body : registered.values()) {
                    JsonObject json = new JsonObject();
                    json.addProperty(
                        "kind",
                        kind.substring(0, kind.length() - 1)
                            .toLowerCase());
                    json.addProperty("name", (String) call(body, "getName"));
                    json.addProperty("tier", (Integer) call(body, "getTierRequirement"));
                    json.addProperty("reachable", (Boolean) call(body, "getReachable"));
                    Object parent = "Planets".equals(kind) ? call(body, "getParentSolarSystem")
                        : call(body, "getParentPlanet");
                    if (parent != null) json.addProperty("parent", (String) call(parent, "getName"));
                    if ("Planets".equals(kind) && parent != null)
                        json.addProperty("system", (String) call(parent, "getName"));
                    else if (parent != null) {
                        Object system = call(parent, "getParentSolarSystem");
                        if (system != null) json.addProperty("system", (String) call(system, "getName"));
                    }
                    int dim = (Integer) call(
                        body,
                        "Satellites".equals(kind) ? "getDimensionIdStatic" : "getDimensionID");
                    // ExtraPlanets parks unreachable bodies on taken ids (Haumea on 0): reachable ones win
                    JsonObject previous = bodies.get(dim);
                    if (previous == null || !previous.get("reachable")
                        .getAsBoolean()) bodies.put(dim, json);
                }
            }
        } catch (ClassNotFoundException ignored) {} catch (Throwable t) {
            net.sxmaa.u25core.U25Core.LOG.warn("Reading Galacticraft's bodies failed", t);
        }
        return bodies;
    }

    private static Object call(Object target, String method) {
        try {
            return target.getClass()
                .getMethod(method)
                .invoke(target);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Items that place a Galacticraft tiered rocket entity, with the rocket's tier. */
    private JsonArray rockets() throws IOException {
        JsonArray rockets = new JsonArray();
        Class<?> tiered;
        try {
            tiered = Class.forName(
                "micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket",
                false,
                Launch.classLoader);
        } catch (ClassNotFoundException e) {
            return rockets;
        }
        Map<Class<?>, Integer> tiers = new HashMap<>();
        for (Object object : GameData.getItemRegistry()) {
            Item item = (Item) object;
            ClassNode node = node(
                item.getClass()
                    .getName());
            if (node == null) continue;
            Set<String> created = new TreeSet<>();
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
                    if (insn.getOpcode() == Opcodes.NEW) created.add(((TypeInsnNode) insn).desc.replace('/', '.'));
            }
            for (String entity : created) {
                try {
                    Class<?> clazz = Class.forName(entity, false, Launch.classLoader);
                    if (!tiered.isAssignableFrom(clazz)) continue;
                    Integer tier = tiers.computeIfAbsent(clazz, DimensionsDump::tier);
                    JsonObject rocket = new JsonObject();
                    rocket.addProperty(
                        "item",
                        GameData.getItemRegistry()
                            .getNameForObject(item));
                    rocket.addProperty("entity", entity);
                    if (tier != null) rocket.addProperty("tier", tier);
                    rockets.add(rocket);
                } catch (Throwable ignored) {}
            }
        }
        return rockets;
    }

    /** getRocketTier off an instance made without a world, or without running a constructor at all. */
    private static Integer tier(Class<?> rocket) {
        Object instance = null;
        try {
            instance = rocket.getConstructor(World.class)
                .newInstance((World) null);
        } catch (Throwable ignored) {}
        try {
            if (instance == null) {
                java.lang.reflect.Field unsafe = Class.forName("sun.misc.Unsafe")
                    .getDeclaredField("theUnsafe");
                unsafe.setAccessible(true);
                Object u = unsafe.get(null);
                instance = u.getClass()
                    .getMethod("allocateInstance", Class.class)
                    .invoke(u, rocket);
            }
            return (Integer) rocket.getMethod("getRocketTier")
                .invoke(instance);
        } catch (Throwable t) {
            return null;
        }
    }
}
