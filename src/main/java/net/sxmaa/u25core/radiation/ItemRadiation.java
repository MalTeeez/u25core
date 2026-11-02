package net.sxmaa.u25core.radiation;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Geiger counter, dosimeter and the two medicines, one item class per kind. */
public class ItemRadiation extends Item {

    public enum Kind {
        GEIGER,
        DOSIMETER,
        ANTI_RAD,
        RAD_PROTECTION
    }

    /** Dose an anti-rad treatment removes, Sv. */
    private static final double TREATMENT = 2;
    /** Fraction blocked and duration of a rad protection pill. */
    private static final double MEDICINE_RESISTANCE = 0.5;
    private static final int MEDICINE_TICKS = 12000;

    public final Kind kind;
    /** Geiger counter screens: off, then one per severity tier. */
    @SideOnly(Side.CLIENT)
    private IIcon[] screens;

    public ItemRadiation(Kind kind, String name) {
        this.kind = kind;
        setUnlocalizedName("u25core." + name);
        setTextureName("u25core:" + name);
        setMaxStackSize(kind == Kind.GEIGER || kind == Kind.DOSIMETER ? 1 : 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        if (kind != Kind.GEIGER) {
            super.registerIcons(register);
            return;
        }
        screens = new IIcon[6];
        for (int i = 0; i < screens.length; i++) screens[i] = register.registerIcon(iconString + "_" + i);
        itemIcon = screens[0];
    }

    /** The geiger counter's screen shows the level around this client's player. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return kind == Kind.GEIGER ? screens[1 + RadiationFormat.tier(RadiationClientState.level)] : itemIcon;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        switch (kind) {
            case GEIGER, DOSIMETER -> {
                if (!world.isRemote) report(player, player, kind == Kind.GEIGER);
            }
            case ANTI_RAD, RAD_PROTECTION -> player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    /** What a geiger counter (full) or dosimeter shows about a player, sent to {@code to}. */
    static void report(EntityPlayer player, ICommandSender to, boolean geiger) {
        if (geiger) {
            double level = RadiationManager.INSTANCE.getLevel(player.worldObj, player.posX, player.posY, player.posZ);
            double carried = RadiationManager.getCarried(player);
            to.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("u25core.geiger.level") + " "
                        + RadiationFormat.color(level)
                        + RadiationFormat.rate(level)));
            long clear = RadiationManager.INSTANCE.secondsUntilClear(player.worldObj, player.posX, player.posZ);
            if (clear != 0) to.addChatMessage(
                new ChatComponentText(
                    clear < 0 ? StatCollector.translateToLocal("u25core.geiger.clear.never")
                        : StatCollector.translateToLocal("u25core.geiger.clear") + " "
                            + RadiationFormat.duration(clear)));
            if (carried > 0) to.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("u25core.geiger.carried") + " "
                        + RadiationFormat.color(carried)
                        + RadiationFormat.rate(carried)));
        }
        EntityRadiationData data = EntityRadiationData.get(player);
        double dose = data == null ? 0 : data.dose;
        to.addChatMessage(
            new ChatComponentText(
                StatCollector.translateToLocal("u25core.geiger.dose") + " "
                    + RadiationFormat.color(dose)
                    + RadiationFormat.format(dose, "Sv")
                    + String.format(" (%.0f%%)", dose / RadiationConfig.lethalDose * 100)));
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        EntityRadiationData data = EntityRadiationData.get(player);
        if (!world.isRemote && data != null) {
            if (kind == Kind.ANTI_RAD) {
                data.treatment += TREATMENT;
            } else if (kind == Kind.RAD_PROTECTION) {
                data.medicineResistance = MEDICINE_RESISTANCE;
                data.medicineTicks = MEDICINE_TICKS;
            }
        }
        if (!player.capabilities.isCreativeMode) stack.stackSize--;
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return kind == Kind.ANTI_RAD || kind == Kind.RAD_PROTECTION ? 32 : 0;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return kind == Kind.ANTI_RAD ? EnumAction.drink
            : kind == Kind.RAD_PROTECTION ? EnumAction.eat : EnumAction.none;
    }
}
