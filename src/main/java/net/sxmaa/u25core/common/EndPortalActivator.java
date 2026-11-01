package net.sxmaa.u25core.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.config.ModConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * With endPortalActivator set, eyes of ender still find strongholds but no longer go into End portal frames; the
 * configured item does instead. It runs the eye's own frame code (filling, sounds, portal check), then is used up.
 */
public final class EndPortalActivator {

    /** Set while the activator runs the eye's frame code, so the eye block lets it through. */
    private static boolean activating;
    private static String parsed;
    private static Item item;
    private static int meta;

    public static boolean enabled() {
        return !ModConfig.endPortalActivator.isEmpty();
    }

    /** Called by the eye of ender mixin: true if the eye must not go into this block. */
    public static boolean blocksEye(World world, int x, int y, int z) {
        return enabled() && !activating && world.getBlock(x, y, z) == Blocks.end_portal_frame;
    }

    public static void hint(EntityPlayer player) {
        resolve();
        String name = item == null ? ModConfig.endPortalActivator
            : new ItemStack(item, 1, Math.max(meta, 0)).getDisplayName();
        player.addChatMessage(new ChatComponentTranslation("u25core.endportal.activator", name));
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.world.isRemote) return;
        EntityPlayer player = event.entityPlayer;
        ItemStack held = player.getCurrentEquippedItem();
        if (!isActivator(held) || event.world.getBlock(event.x, event.y, event.z) != Blocks.end_portal_frame) return;
        boolean filled;
        activating = true;
        try {
            filled = Items.ender_eye.onItemUse(
                new ItemStack(Items.ender_eye),
                player,
                event.world,
                event.x,
                event.y,
                event.z,
                event.face,
                0.5F,
                0.5F,
                0.5F);
        } finally {
            activating = false;
        }
        if (!filled) return;
        if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
        event.setCanceled(true);
    }

    private static boolean isActivator(ItemStack stack) {
        resolve();
        return stack != null && item != null && stack.getItem() == item && (meta < 0 || stack.getItemDamage() == meta);
    }

    /** Parses mod:name[:meta] once per config value. */
    private static void resolve() {
        String entry = ModConfig.endPortalActivator;
        if (entry.equals(parsed)) return;
        parsed = entry;
        String[] parts = entry.split(":");
        item = parts.length >= 2 ? GameRegistry.findItem(parts[0], parts[1]) : null;
        try {
            meta = parts.length >= 3 ? Integer.parseInt(parts[2]) : -1;
        } catch (NumberFormatException e) {
            item = null;
        }
        if (item == null)
            U25Core.LOG.error("endPortalActivator {} isn't an item, End portal frames can't be activated", entry);
    }
}
