package net.sxmaa.u25core.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.sxmaa.u25core.U25Core;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * GTNH's Galacticraft leaves its NASA workbench recipes to the GTNH coremod, so without it rockets, the buggy, the
 * cargo rocket and the astro miner can't be built at all. Puts them back.
 * <p>
 * Tier 1-3 rockets as Galacticraft had them. GTNH reworked the buggy, cargo rocket and astro miner schematics, so those
 * use its layouts, with the GalaxySpace and GregTech parts it adds (rocket control computers, fuel canisters, stainless
 * steel rods and screws, EV motors) left out: those slots have to stay empty.
 */
public final class GalacticraftBenchRecipes {

    private static final int T1 = 1, T2 = 2, T3 = 3, BUGGY = 4, CARGO = 5, ASTRO_MINER = 6;

    private GalacticraftBenchRecipes() {}

    public static void register() {
        if (!Loader.isModLoaded("GalacticraftCore")) return;
        try {
            Class<?> registry = Class.forName("micdoodle8.mods.galacticraft.api.GalacticraftRegistry");
            String[] lists = { null, "getRocketT1Recipes", "getRocketT2Recipes", "getRocketT3Recipes",
                "getBuggyBenchRecipes", "getCargoRocketRecipes", "getAstroMinerRecipes" };
            int added = 0;
            for (int kind = T1; kind <= ASTRO_MINER; kind++) {
                // The coremod's, or the pack's own, take precedence
                if (!((java.util.List<?>) registry.getMethod(lists[kind])
                    .invoke(null)).isEmpty()) continue;
                if (kind >= T2 && !Loader.isModLoaded("GalacticraftMars")) continue;
                added += add(kind);
            }
            U25Core.LOG.info("Added {} Galacticraft NASA workbench recipes", added);
        } catch (Throwable t) {
            U25Core.LOG.error("Adding Galacticraft NASA workbench recipes failed", t);
        }
    }

    private static int add(int kind) throws Exception {
        ItemStack noseCone = gc("item.noseCone", 0), fins = gc("item.rocketFins", 0), engine = gc("item.engine", 0),
            booster = gc("item.engine", 1), plate1 = gc("item.heavyPlating", 0);
        Map<Integer, ItemStack> input = new HashMap<>();
        int count = 0;
        switch (kind) {
            case T1:
                input.put(1, noseCone);
                for (int i = 2; i <= 9; i++) input.put(i, plate1);
                input.put(10, fins);
                input.put(11, fins);
                input.put(12, engine);
                input.put(13, fins);
                input.put(14, fins);
                return chestVariants(kind, "GalacticraftCore", "item.spaceship", input, 15);
            case T2:
                ItemStack plate2 = mars("item.null", 3);
                input.put(1, noseCone);
                for (int i = 2; i <= 11; i++) input.put(i, plate2);
                input.put(12, booster);
                input.put(13, fins);
                input.put(14, fins);
                input.put(15, engine);
                input.put(16, booster);
                input.put(17, fins);
                input.put(18, fins);
                return chestVariants(kind, "GalacticraftMars", "item.spaceshipTier2", input, 19);
            case T3:
                ItemStack plate3 = mars("item.itemBasicAsteroids", 0), heavyFins = mars("item.itemBasicAsteroids", 2);
                input.put(1, mars("item.heavyNoseCone", 0));
                for (int i = 2; i <= 11; i++) input.put(i, plate3);
                input.put(12, booster);
                input.put(13, heavyFins);
                input.put(14, heavyFins);
                input.put(15, mars("item.itemBasicAsteroids", 1));
                input.put(16, booster);
                input.put(17, heavyFins);
                input.put(18, heavyFins);
                return chestVariants(kind, "GalacticraftMars", "item.itemTier3Rocket", input, 19);
            case BUGGY:
                input.put(1, gc("item.basicItem", 19));
                input.put(2, gc("item.buggymat", 1));
                for (int i = 4; i <= 7; i++) input.put(i, gc("item.buggymat", 0));
                for (int i = 12; i <= 16; i++) input.put(i, gc("item.meteoricIronIngot", 1));
                for (int i = 25; i <= 34; i++) input.put(i, plate1);
                empty(input, 34);
                // No chest, then the copper, iron and gold chests GTNH uses
                int[] chests = { -1, 3, 0, 1 };
                for (int meta = 0; meta < 4; meta++) {
                    Map<Integer, ItemStack> variant = new HashMap<>(input);
                    variant.put(35, chests[meta] < 0 ? null : chest(chests[meta]));
                    count += recipe(kind, gc("item.buggy", meta), variant);
                }
                return count;
            case CARGO:
                input.put(1, gc("item.basicItem", 14));
                input.put(7, noseCone);
                for (int i = 8; i <= 15; i++) input.put(i, mars("item.null", 3));
                input.put(16, engine);
                for (int i = 17; i <= 20; i++) input.put(i, fins);
                empty(input, 20);
                int[] cargoChests = { 3, 0, 1 };
                for (int i = 0; i < 3; i++) {
                    Map<Integer, ItemStack> variant = new HashMap<>(input);
                    variant.put(21, chest(cargoChests[i]));
                    count += recipe(kind, mars("item.spaceshipTier2", 11 + i), variant);
                }
                return count;
            case ASTRO_MINER:
                for (int i = 1; i <= 8; i++) input.put(i, mars("item.null", 3));
                input.put(9, gc("item.steelPole", 0));
                input.put(10, gc("item.steelPole", 0));
                for (int i = 11; i <= 13; i++) input.put(i, mars("item.itemBasicAsteroids", 0));
                for (int i = 14; i <= 17; i++) input.put(i, mars("item.orionDrive", 0));
                input.put(19, gc("item.basicItem", 14));
                input.put(20, gc("item.basicItem", 14));
                for (int i = 21; i <= 23; i++) input.put(i, plate1);
                input.put(24, chest(1));
                input.put(25, chest(1));
                input.put(26, mars("item.itemBasicAsteroids", 8));
                input.put(27, mars("tile.beamReceiver", 0));
                empty(input, 29);
                return recipe(kind, mars("item.itemAstroMiner", 0), input);
            default:
                return 0;
        }
    }

    /** No chest, one, two or three in the last three slots: the rocket with 0, 18, 36 or 54 slots of storage. */
    private static int chestVariants(int kind, String mod, String rocket, Map<Integer, ItemStack> input, int first)
        throws Exception {
        int count = 0;
        for (int mask = 0; mask < 8; mask++) {
            Map<Integer, ItemStack> variant = new HashMap<>(input);
            for (int bit = 0; bit < 3; bit++)
                variant.put(first + bit, (mask & 1 << bit) != 0 ? new ItemStack(Blocks.chest) : null);
            count += recipe(kind, stack(mod, rocket, Integer.bitCount(mask)), variant);
        }
        return count;
    }

    /** Slots from 1 to last the recipe doesn't use must be empty. */
    private static void empty(Map<Integer, ItemStack> input, int last) {
        for (int i = 1; i <= last; i++) input.putIfAbsent(i, null);
    }

    private static int recipe(int kind, ItemStack output, Map<Integer, ItemStack> input) throws Exception {
        if (output == MISSING || input.containsValue(MISSING)) return 0;
        Object recipe = Class.forName("micdoodle8.mods.galacticraft.core.recipe.NasaWorkbenchRecipe")
            .getConstructor(ItemStack.class, HashMap.class)
            .newInstance(output, new HashMap<>(input));
        String add = kind == T1 ? "addT1RocketRecipe"
            : kind == T2 ? "addT2RocketRecipe"
                : kind == T3 ? "addT3RocketRecipe"
                    : kind == BUGGY ? "addMoonBuggyRecipe"
                        : kind == CARGO ? "addCargoRocketRecipe" : "addAstroMinerRecipe";
        Class.forName("micdoodle8.mods.galacticraft.api.GalacticraftRegistry")
            .getMethod(add, Class.forName("micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe"))
            .invoke(null, recipe);
        return 1;
    }

    /** Marks a part that isn't registered, so no recipe is made with a hole in it. */
    private static final ItemStack MISSING = new ItemStack(Blocks.air);

    private static ItemStack gc(String name, int meta) {
        return stack("GalacticraftCore", name, meta);
    }

    private static ItemStack mars(String name, int meta) {
        return stack("GalacticraftMars", name, meta);
    }

    private static ItemStack stack(String mod, String name, int meta) {
        Item item = GameRegistry.findItem(mod, name);
        if (item == null) {
            U25Core.LOG.warn("Galacticraft part {}:{} isn't registered", mod, name);
            return MISSING;
        }
        return new ItemStack(item, 1, meta);
    }

    /** Iron Chests' chest of this meta when it's installed (copper 3, iron 0, gold 1), else a plain chest. */
    private static ItemStack chest(int meta) {
        Item ironChest = GameRegistry.findItem("IronChest", "BlockIronChest");
        return ironChest != null ? new ItemStack(ironChest, 1, meta) : new ItemStack(Blocks.chest);
    }
}
