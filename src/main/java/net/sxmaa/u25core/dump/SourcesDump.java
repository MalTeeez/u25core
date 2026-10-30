package net.sxmaa.u25core.dump;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Where items come from outside recipes: scans the bytecode of every registered tile entity, entity, block, item, world
 * generator, dimension provider and event handler for references to mod items and blocks (static fields holding them,
 * GameRegistry.findItem and registry lookups by name). Writes sources.jsonl, one line per item and class.
 */
final class SourcesDump {

    private static final Pattern OUTPUT = Pattern.compile(
        "(?i)(func_70299_a|func_72838_d|func_70441_a|func_70099_a|func_145779_a|func_147449_b|func_147465_d|func_149642_a|add|.*(drop|spawn|give|insert|output|setslot|setblock|setinventory|addstack|addto|addor|place).*)");
    private static final Pattern INPUT = Pattern.compile(
        "(?i)(func_77969_a|func_77989_b|func_77970_a|func_77959_d|.*(equal|match|compare|contains|isvalid|accept).*)");
    /** Registry enums' getters for their entry's item (Reika's ItemRegistry, BlockRegistry, MachineRegistry). */
    private static final Set<String> ENUM_GETTERS = new java.util.HashSet<>(
        java.util.Arrays.asList("getItemInstance", "getBlockInstance", "getStackOf", "getCraftedProduct"));
    /** Calls that pass the stack along: getItem, getItemDamage, copy, ItemStack constructors. */
    private static final Pattern PASS = Pattern.compile(
        "(func_77973_b|func_77960_j|func_77946_l|getItem|getItemDamage|copy|<init>|valueOf|intValue|getStackOf.*|getCraftedProduct|getItemInstance|getBlockInstance)");

    private final Map<String, List<String>> fieldTargets = new HashMap<>();
    private static final Map<String, Number> configValues = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<String> rootClasses = new java.util.HashSet<>();
    private final Map<String, Root> roots = new LinkedHashMap<>();

    private SourcesDump() {}

    /** A class to scan and what it is: tile, entity, block, item, worldgen, dimension or event. */
    static final class Root {

        final String kind;
        final String name;
        final Set<String> blocks = new TreeSet<>();
        final Set<String> items = new TreeSet<>();
        final Set<Integer> dims = new TreeSet<>();
        /** Numbers the class reads off other mods' classes, mostly config: a multiblock's draw lives there. */
        final Map<String, Number> config = new java.util.TreeMap<>();
        String entity, via;
        TileEntity tile;

        Root(String kind, String name) {
            this.kind = kind;
            this.name = name;
        }
    }

    /** What one class references, keyed by item id. */
    private static final class Reference {

        String method, evidence, use = "unknown";
    }

    static int write(File dir, JsonObject info) throws IOException {
        return new SourcesDump().run(dir, info);
    }

    /** The registered tiles, blocks, items, entities, world generators, dimensions and event handlers. */
    static Collection<Root> registeredRoots() throws IOException {
        SourcesDump dump = new SourcesDump();
        dump.collectRoots();
        return dump.roots.values();
    }

    private int run(File dir, JsonObject info) throws IOException {
        collectRoots();
        int count = 0, failed = 0;
        try (Writer out = DataDump.writer(new File(dir, "sources.jsonl"))) {
            for (Root root : roots.values()) {
                Map<String, Reference> refs;
                try {
                    refs = scan(root);
                } catch (Throwable t) {
                    failed++;
                    continue;
                }
                JsonObject fields = null, config = null;
                String unit = null;
                if ("tile".equals(root.kind)) {
                    fields = tileFields(root);
                    unit = unit(root);
                    if (!root.config.isEmpty()) config = DataDump.GSON.toJsonTree(root.config)
                        .getAsJsonObject();
                }
                for (Map.Entry<String, Reference> entry : refs.entrySet()) {
                    Reference ref = entry.getValue();
                    JsonObject line = new JsonObject();
                    line.addProperty("item", entry.getKey());
                    line.addProperty("kind", root.kind);
                    line.addProperty("class", root.name);
                    if (root.via != null) line.addProperty("via", root.via);
                    if (!root.blocks.isEmpty()) line.add("blocks", strings(root.blocks));
                    if (!root.items.isEmpty()) line.add("items", strings(root.items));
                    if (root.entity != null) line.addProperty("entity", root.entity);
                    if (!root.dims.isEmpty()) {
                        JsonArray dims = new JsonArray();
                        for (int dim : root.dims) dims.add(new com.google.gson.JsonPrimitive(dim));
                        line.add("dims", dims);
                    }
                    if (fields != null) line.add("fields", fields);
                    if (config != null) line.add("config", config);
                    if (unit != null && (fields != null || config != null)) line.addProperty("unit", unit);
                    line.addProperty("method", ref.method);
                    line.addProperty("use", ref.use);
                    line.addProperty("evidence", ref.evidence);
                    out.write(line.toString());
                    out.write('\n');
                    count++;
                }
            }
        }
        info.addProperty("classes", roots.size());
        if (failed > 0) info.addProperty("unreadable", failed);
        return count;
    }

    static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) array.add(DataDump.prim(value));
        return array;
    }

    private Root root(String kind, Class<?> clazz) {
        // TileEntity has other class-keyed static maps besides the registry, mixins' among them, null keys included
        if (clazz == null) return null;
        String name = clazz.getName();
        if (vanilla(name.replace('.', '/'))) return null;
        return roots.computeIfAbsent(kind + " " + name, k -> new Root(kind, name));
    }

    static boolean vanilla(String internalName) {
        return internalName.startsWith("net/minecraft/") || internalName.startsWith("java")
            || internalName.startsWith("cpw/")
            || internalName.startsWith("sun/")
            || internalName.startsWith("kotlin/")
            || internalName.startsWith("scala/")
            || internalName.startsWith("com/google/")
            || internalName.startsWith("org/apache/")
            || internalName.startsWith("io/netty/");
    }

    @SuppressWarnings("unchecked")
    private void collectRoots() throws IOException {
        // Tile entities, with the blocks and metas that create them
        Map<Class<?>, String> tiles = null;
        for (Field field : TileEntity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Map<?, ?> map = (Map<?, ?>) field.get(null);
                // classToNameMap: class keys, registry name values
                Map.Entry<?, ?> first = map.isEmpty() ? null
                    : map.entrySet()
                        .iterator()
                        .next();
                if (first != null && first.getKey() instanceof Class && first.getValue() instanceof String)
                    tiles = (Map<Class<?>, String>) map;
            } catch (Throwable ignored) {}
        }
        if (tiles != null) for (Class<?> tile : tiles.keySet()) root("tile", tile);
        for (Object object : GameData.getBlockRegistry()) {
            Block block = (Block) object;
            String name = GameData.getBlockRegistry()
                .getNameForObject(block);
            Root blockRoot = root("block", block.getClass());
            if (blockRoot != null) blockRoot.blocks.add(name);
            for (int meta = 0; meta < 16; meta++) {
                try {
                    if (!block.hasTileEntity(meta)) continue;
                    TileEntity tile = block.createTileEntity(null, meta);
                    Root tileRoot = tile == null ? null : root("tile", tile.getClass());
                    if (tileRoot == null) continue;
                    tileRoot.blocks.add(name + ":" + meta);
                    if (tileRoot.tile == null) tileRoot.tile = tile;
                } catch (Throwable ignored) {}
            }
        }
        for (Object object : GameData.getItemRegistry()) {
            Root itemRoot = root("item", object.getClass());
            if (itemRoot != null) itemRoot.items.add(
                GameData.getItemRegistry()
                    .getNameForObject(object));
        }
        for (Map.Entry<? extends Class<?>, String> entity : EntityList.classToStringMapping.entrySet()) {
            Root root = root("entity", entity.getKey());
            if (root != null) root.entity = entity.getValue();
        }
        Set<Object> generators = ReflectionHelper.getPrivateValue(GameRegistry.class, null, "worldGenerators");
        List<Root> followed = new ArrayList<>();
        for (Object generator : generators) addIfPresent(followed, root("worldgen", generator.getClass()));
        Map<Integer, Class<?>> providers = ReflectionHelper.getPrivateValue(DimensionManager.class, null, "providers");
        for (int dim : DimensionManager.getStaticDimensionIDs()) {
            Class<?> provider = providers.get(DimensionManager.getProviderType(dim));
            Root root = provider == null ? null : root("dimension", provider);
            if (root == null) continue;
            root.dims.add(dim);
            addIfPresent(followed, root);
        }
        for (EventBus bus : new EventBus[] { MinecraftForge.EVENT_BUS, MinecraftForge.TERRAIN_GEN_BUS,
            MinecraftForge.ORE_GEN_BUS, FMLCommonHandler.instance()
                .bus() }) {
            Map<Object, ?> listeners = ReflectionHelper.getPrivateValue(EventBus.class, bus, "listeners");
            for (Object listener : listeners.keySet()) {
                Class<?> clazz = listener instanceof Class ? (Class<?>) listener : listener.getClass();
                if (clazz.getName()
                    .startsWith("net.sxmaa.u25core")) continue;
                root("event", clazz);
            }
        }
        // World generators and chunk providers place their ores through helper classes they create
        for (Root root : roots.values()) rootClasses.add(root.name);
        for (Root root : followed) follow(root, root.name, 3);
        // A block with the same tile on every meta is just the block
        for (Root root : roots.values()) {
            if (!"tile".equals(root.kind)) continue;
            Map<String, Integer> metas = new HashMap<>();
            for (String block : root.blocks) metas.merge(block.substring(0, block.lastIndexOf(':')), 1, Integer::sum);
            for (Map.Entry<String, Integer> block : metas.entrySet()) {
                if (block.getValue() < 16) continue;
                root.blocks.removeIf(b -> b.startsWith(block.getKey() + ":"));
                root.blocks.add(block.getKey());
            }
        }
    }

    private static void addIfPresent(List<Root> list, Root root) {
        if (root != null && !list.contains(root)) list.add(root);
    }

    /** Adds the classes this one creates (NEW) or names (LDC type), under the same kind, up to depth levels down. */
    private void follow(Root from, String name, int depth) throws IOException {
        if (depth == 0) return;
        ClassNode node = node(name);
        if (node == null) return;
        Set<String> found = new TreeSet<>();
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                String type = null;
                if (insn.getOpcode() == Opcodes.NEW) type = ((TypeInsnNode) insn).desc;
                else if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Type
                    && ((Type) ((LdcInsnNode) insn).cst).getSort() == Type.OBJECT)
                    type = ((Type) ((LdcInsnNode) insn).cst).getInternalName();
                if (type != null && !vanilla(type) && !type.startsWith(node.name + "$")) found.add(type);
            }
        }
        for (String type : found) {
            String className = type.replace('/', '.');
            String key = from.kind + " " + className;
            if (roots.containsKey(key) || rootClasses.contains(className)) continue;
            Root root = new Root(from.kind, className);
            root.via = from.via != null ? from.via : from.name;
            root.dims.addAll(from.dims);
            roots.put(key, root);
            follow(root, className, depth - 1);
        }
    }

    static ClassNode node(String className) throws IOException {
        byte[] bytes = Launch.classLoader.getClassBytes(className);
        if (bytes == null) return null;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        return node;
    }

    private static String unit(Root root) {
        try {
            return Units.of(Machines.modOf(Class.forName(root.name, false, Launch.classLoader)));
        } catch (Throwable t) {
            return null;
        }
    }

    /** The machine's rate fields, off the tile its block made or a fresh one; null when there are none. */
    private static JsonObject tileFields(Root root) {
        try {
            TileEntity tile = root.tile != null ? root.tile
                : (TileEntity) Class.forName(root.name, false, Launch.classLoader)
                    .newInstance();
            JsonObject fields = Machines.fields(tile);
            return fields.entrySet()
                .isEmpty() ? null : fields;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Item references of a class and its nested classes. */
    private Map<String, Reference> scan(Root root) throws IOException {
        Map<String, Reference> refs = new LinkedHashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(root.name);
        Set<String> seen = new TreeSet<>();
        while (!queue.isEmpty()) {
            String name = queue.poll();
            if (!seen.add(name)) continue;
            ClassNode node = node(name);
            if (node == null) continue;
            for (InnerClassNode inner : node.innerClasses)
                if (inner.name.startsWith(node.name + "$")) queue.add(inner.name.replace('/', '.'));
            for (MethodNode method : node.methods) scan(node.name, method, refs, root.config);
        }
        return refs;
    }

    private void scan(String owner, MethodNode method, Map<String, Reference> refs, Map<String, Number> config) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            List<String> ids = null;
            String evidence = null;
            if (insn.getOpcode() == Opcodes.GETSTATIC) {
                FieldInsnNode field = (FieldInsnNode) insn;
                // javac inlines true constants, so a number read off another class is nearly always config
                if ("IJFDS".contains(field.desc) && field.desc.length() == 1
                    && !vanilla(field.owner)
                    && !field.owner.startsWith(owner)
                    && !owner.startsWith(field.owner)
                    && !Machines.NOISE.matcher(field.name)
                        .matches()) {
                    Number value = configValue(field);
                    if (value != null)
                        config.put(field.owner.substring(field.owner.lastIndexOf('/') + 1) + "." + field.name, value);
                    continue;
                }
                if (!field.desc.startsWith("L") || vanilla(field.owner)) continue;
                ids = resolve(field);
                evidence = "GETSTATIC " + field.owner + "." + field.name;
            } else if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                String name = byName(call);
                if (name != null && !name.startsWith("minecraft:")) {
                    ids = java.util.Collections.singletonList(name);
                    evidence = call.owner
                        .substring(call.owner.lastIndexOf('/') + 1) + "." + call.name + " \"" + name + "\"";
                }
            }
            if (ids == null || ids.isEmpty()) continue;
            AbstractInsnNode after = insn;
            Integer meta = null;
            // new ItemStack(item, count, meta): the constants right before the constructor
            List<Integer> constants = new ArrayList<>();
            for (AbstractInsnNode next = real(insn.getNext()); next != null; next = real(next.getNext())) {
                Integer constant = intConstant(next);
                if (constant != null) {
                    constants.add(constant);
                    continue;
                }
                if (next instanceof MethodInsnNode && "<init>".equals(((MethodInsnNode) next).name)
                    && "net/minecraft/item/ItemStack".equals(((MethodInsnNode) next).owner)) {
                    String desc = ((MethodInsnNode) next).desc;
                    if (desc.endsWith(";II)V") && constants.size() == 2) meta = constants.get(1);
                    else if (desc.endsWith(";I)V") || desc.endsWith(";)V")) meta = 0;
                    after = next;
                }
                break;
            }
            String use = use(after);
            for (String id : ids) {
                String full = meta != null && meta != 32767 && id.split(":").length == 2 ? id + ":" + meta : id;
                Reference ref = refs.get(full);
                if (ref == null) {
                    ref = new Reference();
                    refs.put(full, ref);
                } else if (!"output".equals(use) && !("input".equals(use) && "unknown".equals(ref.use))) continue;
                ref.method = method.name;
                ref.evidence = evidence;
                ref.use = use;
            }
        }
    }

    static Number configValue(FieldInsnNode insn) {
        String key = insn.owner + "." + insn.name;
        if (configValues.containsKey(key)) return configValues.get(key);
        Number value = null;
        try {
            for (Class<?> c = Class.forName(insn.owner.replace('/', '.'), false, Launch.classLoader); c != null
                && value == null; c = c.getSuperclass()) {
                try {
                    Field field = c.getDeclaredField(insn.name);
                    field.setAccessible(true);
                    value = (Number) field.get(null);
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Throwable ignored) {}
        if (value != null) configValues.put(key, value);
        return value;
    }

    /** GameRegistry.findItem/findBlock/findItemStack("mod", "name") and registry lookups by a constant name. */
    private static String byName(MethodInsnNode call) {
        List<String> strings = new ArrayList<>();
        AbstractInsnNode previous = call.getPrevious();
        for (int i = 0; i < 3 && previous != null; previous = previous.getPrevious()) {
            if (previous.getOpcode() < 0) continue;
            i++;
            if (previous instanceof LdcInsnNode && ((LdcInsnNode) previous).cst instanceof String)
                strings.add(0, (String) ((LdcInsnNode) previous).cst);
            else if (intConstant(previous) == null) break;
        }
        if (strings.isEmpty()) return null;
        if ("cpw/mods/fml/common/registry/GameRegistry".equals(call.owner) && call.name.startsWith("find")
            && strings.size() >= 2) return strings.get(strings.size() - 2) + ":" + strings.get(strings.size() - 1);
        if (call.owner.startsWith("net/minecraft/")
            && (call.name.equals("getObject") || call.name.equals("func_82594_a")
                || call.name.equals("getBlockFromName")
                || call.name.equals("func_149684_b"))) {
            String name = strings.get(strings.size() - 1);
            return name.contains(":") ? name : "minecraft:" + name;
        }
        return null;
    }

    /** Ids of the items a static field holds: an Item, Block, ItemStack, or an enum entry with an item getter. */
    private List<String> resolve(FieldInsnNode insn) {
        return fieldTargets.computeIfAbsent(insn.owner + "." + insn.name, k -> {
            List<String> ids = new ArrayList<>();
            try {
                ClassLoader loader = Launch.classLoader;
                Class<?> type = Class.forName(
                    Type.getType(insn.desc)
                        .getClassName(),
                    false,
                    loader);
                if (!Item.class.isAssignableFrom(type) && !Block.class.isAssignableFrom(type)
                    && type != ItemStack.class
                    && !type.isEnum()) return ids;
                Field field = null;
                for (Class<?> c = Class.forName(insn.owner.replace('/', '.'), false, loader); c != null
                    && field == null; c = c.getSuperclass()) {
                    try {
                        field = c.getDeclaredField(insn.name);
                    } catch (NoSuchFieldException ignored) {}
                }
                if (field == null) return ids;
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof Enum) value = fromEnum(value);
                String id = id(value);
                if (id != null) ids.add(id);
            } catch (Throwable ignored) {}
            return ids;
        });
    }

    /** Mods keep items in enums too (Reika's ItemRegistry/BlockRegistry): an item getter on the entry. */
    private static Object fromEnum(Object entry) {
        Object found = null;
        for (Method method : entry.getClass()
            .getDeclaredMethods()) {
            Class<?> type = method.getReturnType();
            if (method.getParameterTypes().length > 0 || Modifier.isStatic(method.getModifiers())
                || !(type == ItemStack.class || Item.class.isAssignableFrom(type) || Block.class.isAssignableFrom(type))
                || !ENUM_GETTERS.contains(method.getName())) continue;
            try {
                method.setAccessible(true);
                Object value = method.invoke(entry);
                if (value instanceof ItemStack) value = ((ItemStack) value).getItem();
                if (value != null) found = value;
            } catch (Throwable ignored) {}
        }
        return found;
    }

    /** mod:name, mod:name:meta for stacks; null for vanilla and unregistered things. */
    static String id(Object value) {
        try {
            if (value instanceof Block) {
                Item item = Item.getItemFromBlock((Block) value);
                value = item != null ? item : value;
                if (item == null) {
                    String name = GameData.getBlockRegistry()
                        .getNameForObject(value);
                    return name == null || name.startsWith("minecraft:") ? null : name;
                }
            }
            String name;
            int meta = -1;
            if (value instanceof ItemStack) {
                ItemStack stack = (ItemStack) value;
                if (stack.getItem() == null) return null;
                meta = stack.getItemDamage();
                value = stack.getItem();
            }
            if (!(value instanceof Item)) return null;
            name = GameData.getItemRegistry()
                .getNameForObject(value);
            if (name == null || name.startsWith("minecraft:")) return null;
            return meta >= 0 && meta != 32767 ? name + ":" + meta : name;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Best effort: what the code does with the reference within the next few instructions. Into an inventory slot, an
     * array, the world or a returned list is output; compared is input.
     */
    private static String use(AbstractInsnNode from) {
        int seen = 0;
        for (AbstractInsnNode insn = real(from.getNext()); insn != null && seen < 12; insn = real(insn.getNext())) {
            seen++;
            int op = insn.getOpcode();
            if (op == Opcodes.IF_ACMPEQ || op == Opcodes.IF_ACMPNE) return "input";
            if (op == Opcodes.AASTORE || op == Opcodes.ARETURN) return "output";
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if ("<init>".equals(call.name) && "net/minecraft/entity/item/EntityItem".equals(call.owner))
                    return "output";
                if (PASS.matcher(call.name)
                    .matches()) continue;
                if (INPUT.matcher(call.name)
                    .matches()) return "input";
                if (OUTPUT.matcher(call.name)
                    .matches()) return "output";
            }
        }
        return "unknown";
    }

    /** Skips labels, line numbers and frames. */
    private static AbstractInsnNode real(AbstractInsnNode insn) {
        while (insn != null && insn.getOpcode() < 0) insn = insn.getNext();
        return insn;
    }

    static Integer intConstant(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5 && insn instanceof InsnNode) return op - Opcodes.ICONST_0;
        if ((op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) && insn instanceof IntInsnNode)
            return ((IntInsnNode) insn).operand;
        if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer)
            return (Integer) ((LdcInsnNode) insn).cst;
        return null;
    }
}
