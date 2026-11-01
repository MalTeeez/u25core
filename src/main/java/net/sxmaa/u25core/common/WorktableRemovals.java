package net.sxmaa.u25core.common;

import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.config.ModConfig;

import Reika.RotaryCraft.Auxiliary.RecipeManagers.WorktableRecipes;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Removes RotaryCraft Worktable recipes by output, listed in worktableRemove. Reika's recipe files can only add
 * Worktable recipes and MineTweaker has no handler, so their copies would bypass the pack's scripted gates. Runs at
 * load complete, after RotaryCraft added its post-load recipes.
 */
public final class WorktableRemovals {

    private WorktableRemovals() {}

    public static void apply() {
        // removeRecipe and its parameter type are protected
        if (ModConfig.worktableRemove.length == 0) return;
        try {
            WorktableRecipes recipes = WorktableRecipes.getInstance();
            Method remove = WorktableRecipes.class.getDeclaredMethod(
                "removeRecipe",
                Class.forName("Reika.RotaryCraft.Auxiliary.RecipeManagers.RecipeHandler$MachineRecipe"));
            remove.setAccessible(true);
            int removed = 0;
            // getRecipeListCopy is a live view, not a copy
            for (WorktableRecipes.WorktableRecipe recipe : new ArrayList<>(recipes.getRecipeListCopy())) {
                if (matches(recipe.getOutput()) && (boolean) remove.invoke(recipes, recipe)) removed++;
            }
            U25Core.LOG.info("Removed {} RotaryCraft Worktable recipes", removed);
        } catch (ReflectiveOperationException e) {
            U25Core.LOG.error("Couldn't remove RotaryCraft Worktable recipes", e);
        }
    }

    /** Entries are mod:name or mod:name:meta, as in the pack's scripts. */
    private static boolean matches(ItemStack out) {
        if (out == null || out.getItem() == null) return false;
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(out.getItem());
        if (id == null) return false;
        String name = id.modId + ":" + id.name;
        for (String entry : ModConfig.worktableRemove) {
            if (entry.equals(name) || entry.equals(name + ":" + out.getItemDamage())) return true;
        }
        return false;
    }
}
