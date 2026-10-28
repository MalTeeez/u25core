package net.sxmaa.u25core.radiation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

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
    /** Resistance and duration of a rad protection pill. */
    private static final double MEDICINE_RESISTANCE = 0.5;
    private static final int MEDICINE_TICKS = 12000;

    public final Kind kind;

    public ItemRadiation(Kind kind, String name) {
        this.kind = kind;
        setUnlocalizedName("u25core." + name);
        setTextureName("u25core:" + name);
        setMaxStackSize(kind == Kind.GEIGER || kind == Kind.DOSIMETER ? 1 : 16);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        switch (kind) {
            case GEIGER, DOSIMETER -> {
                if (!world.isRemote) report(player);
            }
            case ANTI_RAD, RAD_PROTECTION -> player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    private void report(EntityPlayer player) {
        if (kind == Kind.GEIGER) {
            double level = RadiationManager.INSTANCE.getLevel(player.worldObj, player.posX, player.posY, player.posZ);
            double carried = RadiationManager.getCarried(player);
            player.addChatComponentMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("u25core.geiger.level") + " "
                        + RadiationFormat.color(level)
                        + RadiationFormat.format(level, "Sv/h")));
            if (carried > 0) player.addChatComponentMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("u25core.geiger.carried") + " "
                        + RadiationFormat.color(carried)
                        + RadiationFormat.format(carried, "Sv/h")));
        }
        EntityRadiationData data = EntityRadiationData.get(player);
        double dose = data == null ? 0 : data.dose;
        player.addChatComponentMessage(
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
