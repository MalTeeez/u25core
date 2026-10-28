package net.sxmaa.u25core.radiation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.oredict.OreDictionary;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.api.radiation.IRadiationProtection;

import cpw.mods.fml.common.registry.GameRegistry;

/** Lookup tables built from config and API registrations: radioactivity, armor resistance, shielding, background. */
public final class RadiationRegistry {

    /** Shielding of one block type. */
    public static final class Shield {

        public static final Shield NONE = new Shield(0, 0, false);
        public final double neutronAbsorb, radiationBlock;
        public final boolean moderates;

        Shield(double neutronAbsorb, double radiationBlock, boolean moderates) {
            this.neutronAbsorb = neutronAbsorb;
            this.radiationBlock = radiationBlock;
            this.moderates = moderates;
        }
    }

    private static final Map<String, Double> radioactive = new HashMap<>();
    private static final Map<String, Double> apiRadioactive = new HashMap<>();
    private static final Map<String, Double> resistance = new HashMap<>();
    private static final Map<String, Shield> shieldKeys = new HashMap<>();
    private static final Map<Integer, Double> dimensionBackground = new HashMap<>();
    private static final Map<String, Double> biomeBackground = new HashMap<>();

    private static final Map<Integer, Double> radiationPotions = new HashMap<>();

    // Resolved per item/block id and meta, cleared whenever the tables change. Concurrent since the client thread
    // reads them too in singleplayer
    private static final Map<Long, Double> stackCache = new ConcurrentHashMap<>();
    private static final Map<Long, Shield> shieldCache = new ConcurrentHashMap<>();

    private RadiationRegistry() {}

    public static void load() {
        radioactive.clear();
        radioactive.putAll(apiRadioactive);
        parseDoubles(RadiationConfig.radioactiveMaterials, radioactive);
        resistance.clear();
        parseDoubles(RadiationConfig.armorResistance, resistance);
        shieldKeys.clear();
        for (String entry : RadiationConfig.shielding) {
            String[] kv = entry.split("=", 2);
            String[] values = kv.length == 2 ? kv[1].split(",") : new String[0];
            if (values.length < 2) {
                U25Core.LOG.warn("Invalid radiation shielding entry: {}", entry);
                continue;
            }
            try {
                shieldKeys.put(
                    kv[0].trim(),
                    new Shield(
                        Double.parseDouble(values[0].trim()),
                        Double.parseDouble(values[1].trim()),
                        values.length > 2 && Boolean.parseBoolean(values[2].trim())));
            } catch (NumberFormatException e) {
                U25Core.LOG.warn("Invalid radiation shielding entry: {}", entry);
            }
        }
        dimensionBackground.clear();
        Map<String, Double> dims = new HashMap<>();
        parseDoubles(RadiationConfig.dimensionBackground, dims);
        for (Map.Entry<String, Double> e : dims.entrySet()) {
            try {
                dimensionBackground.put(Integer.parseInt(e.getKey()), e.getValue());
            } catch (NumberFormatException ex) {
                U25Core.LOG.warn("Invalid radiation dimension id: {}", e.getKey());
            }
        }
        biomeBackground.clear();
        parseDoubles(RadiationConfig.biomeBackground, biomeBackground);
        radiationPotions.clear();
        Map<String, Double> potions = new HashMap<>();
        parseDoubles(RadiationConfig.radiationPotions, potions);
        for (Map.Entry<String, Double> e : potions.entrySet()) {
            Potion potion = findPotion(e.getKey());
            if (potion != null) radiationPotions.put(potion.id, e.getValue());
            else U25Core.LOG.info("Radiation potion {} not found, skipping", e.getKey());
        }
        stackCache.clear();
        shieldCache.clear();
    }

    private static void parseDoubles(String[] entries, Map<String, Double> into) {
        for (String entry : entries) {
            String[] kv = entry.split("=", 2);
            try {
                into.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
            } catch (RuntimeException e) {
                U25Core.LOG.warn("Invalid radiation config entry: {}", entry);
            }
        }
    }

    public static void register(String key, double value) {
        apiRadioactive.put(key, value);
        // Config entries take precedence
        if (!configHasKey(key)) radioactive.put(key, value);
        stackCache.clear();
    }

    private static boolean configHasKey(String key) {
        for (String entry : RadiationConfig.radioactiveMaterials) {
            if (entry.split("=", 2)[0].trim()
                .equals(key)) return true;
        }
        return false;
    }

    public static double get(String key) {
        Double value = radioactive.get(key);
        return value == null ? 0 : value;
    }

    /** Radioactivity of one item of this stack in Sv/h. */
    public static double get(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;
        long id = ((long) Item.getIdFromItem(stack.getItem()) << 16) | (stack.getItemDamage() & 0xFFFF);
        Double cached = stackCache.get(id);
        if (cached != null) return cached;
        double value = 0;
        String name = GameRegistry.findUniqueIdentifierFor(stack.getItem()) != null
            ? GameRegistry.findUniqueIdentifierFor(stack.getItem())
                .toString()
            : null;
        if (name != null) {
            Double exact = radioactive.get("item:" + name + ":" + stack.getItemDamage());
            if (exact == null) exact = radioactive.get("item:" + name);
            if (exact != null) value = exact;
        }
        if (value == 0) {
            for (int oreId : OreDictionary.getOreIDs(stack)) {
                Double ore = radioactive.get("ore:" + OreDictionary.getOreName(oreId));
                if (ore != null) {
                    value = ore;
                    break;
                }
            }
        }
        stackCache.put(id, value);
        return value;
    }

    /** Radioactivity of a whole stack in Sv/h. */
    public static double getTotal(ItemStack stack) {
        return stack == null ? 0 : get(stack) * stack.stackSize;
    }

    /** Radioactivity of a whole fluid stack in Sv/h. */
    public static double get(FluidStack stack) {
        return stack == null || stack.getFluid() == null ? 0
            : get(
                "fluid:" + stack.getFluid()
                    .getName())
                * stack.amount;
    }

    public static double getResistance(ItemStack stack, EntityLivingBase wearer) {
        if (stack == null || stack.getItem() == null) return 0;
        if (stack.getItem() instanceof IRadiationProtection protection) {
            return protection.getRadiationResistance(stack, wearer);
        }
        // Modular Powersuits keeps installed modules in this tag, keyed by module name
        if (stack.hasTagCompound() && stack.getTagCompound()
            .getCompoundTag("mmmpsmod")
            .hasKey("Radiation Shielding")) return RadiationConfig.mpsHazmatResistance;
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (id == null) return 0;
        Double value = resistance.get(id + ":" + stack.getItemDamage());
        if (value == null) value = resistance.get(id.toString());
        return value == null ? 0 : value;
    }

    public static Shield getShield(Block block, int meta) {
        long id = ((long) Block.getIdFromBlock(block) << 16) | (meta & 0xFFFF);
        Shield cached = shieldCache.get(id);
        if (cached != null) return cached;
        Shield shield = Shield.NONE;
        GameRegistry.UniqueIdentifier name = GameRegistry.findUniqueIdentifierFor(block);
        if (name != null) {
            Shield exact = shieldKeys.get("block:" + name + ":" + meta);
            if (exact == null) exact = shieldKeys.get("block:" + name);
            if (exact != null) shield = exact;
        }
        Item item = Item.getItemFromBlock(block);
        if (shield == Shield.NONE && item != null) {
            for (int oreId : OreDictionary.getOreIDs(new ItemStack(item, 1, meta))) {
                Shield ore = shieldKeys.get("ore:" + OreDictionary.getOreName(oreId));
                if (ore != null) {
                    shield = ore;
                    break;
                }
            }
        }
        // Any other fluid shields a little, water has its own entry since it also slows neutrons down
        if (shield == Shield.NONE && (block instanceof BlockLiquid || block instanceof IFluidBlock)) {
            shield = new Shield(RadiationConfig.fluidNeutronAbsorb, RadiationConfig.fluidRadiationBlock, false);
        }
        shieldCache.put(id, shield);
        return shield;
    }

    /** Background radiation in Sv/h, without any sources. */
    public static double getBackground(World world, int x, int z) {
        double level = RadiationConfig.baseline;
        Double dim = dimensionBackground.get(world.provider.dimensionId);
        if (dim != null) level += dim;
        if (!biomeBackground.isEmpty()) {
            BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
            Double value = biome == null ? null : biomeBackground.get(biome.biomeName);
            if (value != null) level += value;
        }
        return level;
    }

    /** Sv/h per potion level for a potion other mods use as radiation, 0 if it isn't one. */
    public static double getPotionRate(int potionId) {
        Double rate = radiationPotions.get(potionId);
        return rate == null ? 0 : rate;
    }

    /** A potion by its name, with or without the "potion." prefix. */
    public static Potion findPotion(String name) {
        for (Potion potion : Potion.potionTypes) {
            if (potion != null && (potion.getName()
                .equals("potion." + name)
                || potion.getName()
                    .equals(name)))
                return potion;
        }
        return null;
    }
}
