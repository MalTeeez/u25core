package net.sxmaa.u25core.radiation.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.radiation.ItemRadiation;
import net.sxmaa.u25core.radiation.ItemRadiationGlasses;
import net.sxmaa.u25core.radiation.RadiationClientState;
import net.sxmaa.u25core.radiation.RadiationConfig;
import net.sxmaa.u25core.radiation.RadiationFormat;
import net.sxmaa.u25core.radiation.RadiationRegistry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** HUD, geiger clicks, radiation sickness tint and Cherenkov glow. */
public final class RadiationClientHandler {

    private static final Random RAND = new Random();
    /** Hot source updates older than this are dropped, in ms. */
    private static final long HOT_SOURCE_TIMEOUT = 6000;
    private static final double CHERENKOV_RANGE = 32;
    /** Radiation glasses: particle radius around the player, and the heat scale from low to deadly. */
    private static final double SIGHT_RADIUS = 8;
    private static final float[][] HEAT = { { 0.15F, 0.35F, 1 }, { 1, 0.55F, 0.05F }, { 1, 0.05F, 0.05F },
        { 0.4F, 0.01F, 0.01F } };

    /** Whether an item places a block whose tile entity is a radiation source, by item id and meta. */
    private final Map<Long, Boolean> sourceBlocks = new HashMap<>();

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        double activity = RadiationRegistry.get(event.itemStack);
        if (activity > 0) {
            event.toolTip.add(
                EnumChatFormatting.YELLOW + StatCollector
                    .translateToLocalFormatted("u25core.tooltip.radioactive", RadiationFormat.rate(activity)));
        } else if (isSourceBlock(event.itemStack)) {
            event.toolTip
                .add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("u25core.tooltip.radioactive.active"));
        }
    }

    private boolean isSourceBlock(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == null || Minecraft.getMinecraft().theWorld == null) return false;
        int meta = stack.getItem()
            .getMetadata(stack.getItemDamage());
        long key = (long) Item.getIdFromItem(stack.getItem()) << 16 | (meta & 0xFFFF);
        return sourceBlocks.computeIfAbsent(key, k -> {
            if (!block.hasTileEntity(meta)) return false;
            try {
                TileEntity te = block.createTileEntity(Minecraft.getMinecraft().theWorld, meta);
                return te instanceof IRadiationSource;
            } catch (RuntimeException e) {
                return false;
            }
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.isGamePaused()) return;
        EntityPlayer player = mc.thePlayer;
        if (hasItem(player, ItemRadiation.Kind.GEIGER)) {
            // Background (~0.1 uSv/h) ticks rarely, a few Sv/h is a constant crackle
            double level = RadiationClientState.level;
            double chance = Math.min(0.95, Math.cbrt(level / 0.05));
            if (RAND.nextDouble() < chance) {
                player.playSound("random.click", 0.15F, 1.8F + RAND.nextFloat() * 0.4F);
            }
        }
        if (RadiationConfig.cherenkovLevel > 0) cherenkov(player.worldObj, player);
        if (ItemRadiationGlasses.isWorn(player)) radiationSight(player);
    }

    /** Redstone dust around the player, more and hotter coloured the stronger the level. */
    private void radiationSight(EntityPlayer player) {
        double s = RadiationFormat.severity(RadiationClientState.level);
        if (s <= 0) return;
        // Heat scale: blue, orange, red, dark red
        double pos = s * (HEAT.length - 1);
        int i = Math.min(HEAT.length - 2, (int) pos);
        float f = (float) (pos - i);
        float[] a = HEAT[i], b = HEAT[i + 1];
        int count = RAND.nextInt(1 + (int) (s * 12));
        for (int n = 0; n < count; n++) {
            // reddust takes its colour as the motion arguments
            player.worldObj.spawnParticle(
                "reddust",
                player.posX + (RAND.nextDouble() * 2 - 1) * SIGHT_RADIUS,
                player.posY + (RAND.nextDouble() * 2 - 1) * SIGHT_RADIUS / 2,
                player.posZ + (RAND.nextDouble() * 2 - 1) * SIGHT_RADIUS,
                a[0] + (b[0] - a[0]) * f,
                a[1] + (b[1] - a[1]) * f,
                a[2] + (b[2] - a[2]) * f);
        }
    }

    private void cherenkov(World world, EntityPlayer player) {
        if (System.currentTimeMillis() - RadiationClientState.hotSourcesTime < HOT_SOURCE_TIMEOUT) {
            for (double[] s : RadiationClientState.hotSources) {
                if (player.getDistanceSq(s[0], s[1], s[2]) > CHERENKOV_RANGE * CHERENKOV_RANGE) continue;
                // Stronger sources glow over more of the surrounding water
                int tries = (int) Math.min(6, 1 + Math.log10(s[3] / RadiationConfig.cherenkovLevel) * 2);
                for (int i = 0; i < tries; i++) {
                    glow(
                        world,
                        (int) s[0] + RAND.nextInt(5) - 2,
                        (int) s[1] + RAND.nextInt(5) - 2,
                        (int) s[2] + RAND.nextInt(5) - 2);
                }
            }
        }
        // Spent fuel and other hot items lying in water
        if (world.getTotalWorldTime() % 4 == 0) {
            for (Object o : world.loadedEntityList) {
                if (!(o instanceof EntityItem item) || !item.isInWater()
                    || player.getDistanceSqToEntity(item) > CHERENKOV_RANGE * CHERENKOV_RANGE) continue;
                if (RadiationRegistry.getTotal(item.getEntityItem()) >= RadiationConfig.cherenkovLevel) {
                    glow(world, (int) Math.floor(item.posX), (int) Math.floor(item.posY), (int) Math.floor(item.posZ));
                }
            }
        }
    }

    private void glow(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z)
            .getMaterial() != Material.water) return;
        Minecraft.getMinecraft().effectRenderer.addEffect(
            new EntityCherenkovFX(world, x + RAND.nextDouble(), y + RAND.nextDouble(), z + RAND.nextDouble()));
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        int width = event.resolution.getScaledWidth(), height = event.resolution.getScaledHeight();

        // Sickness tint, independent of instruments
        double severity = RadiationClientState.dose / RadiationConfig.lethalDose;
        if (severity > 0.1) {
            int alpha = (int) (Math.min(1, severity) * 90);
            Gui.drawRect(0, 0, width, height, alpha << 24 | 0x40A020);
        }

        boolean geiger = hasItem(mc.thePlayer, ItemRadiation.Kind.GEIGER);
        boolean dosimeter = geiger || hasItem(mc.thePlayer, ItemRadiation.Kind.DOSIMETER);
        if (!dosimeter || mc.gameSettings.showDebugInfo) return;
        FontRenderer font = mc.fontRenderer;
        int y = 2;
        if (geiger) {
            double level = RadiationClientState.level;
            font.drawStringWithShadow(RadiationFormat.color(level) + RadiationFormat.rate(level), 2, y, 0xFFFFFF);
            y += 10;
        }
        double dose = RadiationClientState.dose;
        font.drawStringWithShadow(
            RadiationFormat.color(dose) + RadiationFormat.format(dose, "Sv")
                + String.format(" (%.0f%%)", dose / RadiationConfig.lethalDose * 100),
            2,
            y,
            0xFFFFFF);
    }

    private static boolean hasItem(EntityPlayer player, ItemRadiation.Kind kind) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemRadiation item && item.kind == kind) return true;
        }
        return false;
    }
}
