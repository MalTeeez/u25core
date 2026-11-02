package net.sxmaa.u25core.radiation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Shows radiation around the wearer as particles, worn as a helmet or in a Baubles head slot. */
@Optional.Interface(iface = "baubles.api.expanded.IBaubleExpanded", modid = "Baubles")
public class ItemRadiationGlasses extends ItemArmor implements IBaubleExpanded {

    /** No protection, durability 0 makes them unbreakable. */
    private static final ArmorMaterial MATERIAL = EnumHelper
        .addArmorMaterial("U25CORE_RADIATION_GLASSES", 0, new int[] { 0, 0, 0, 0 }, 15);
    private static final boolean BAUBLES = Loader.isModLoaded("Baubles");

    public ItemRadiationGlasses() {
        super(MATERIAL, 0, 0);
        setUnlocalizedName("u25core.radiation_glasses");
        setTextureName("u25core:radiation_glasses");
        if (BAUBLES) BaubleExpandedSlots.tryAssignSlotOfType(BaubleExpandedSlots.headType);
    }

    public static boolean isWorn(EntityPlayer player) {
        if (isGlasses(player.inventory.armorInventory[3])) return true;
        if (!BAUBLES) return false;
        IInventory baubles = BaublesApi.getBaubles(player);
        if (baubles == null) return false;
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            if (isGlasses(baubles.getStackInSlot(i))) return true;
        }
        return false;
    }

    private static boolean isGlasses(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemRadiationGlasses;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        return "u25core:textures/models/armor/radiation_glasses.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return true;
    }

    // region Baubles

    @Override
    @Optional.Method(modid = "Baubles")
    public String[] getBaubleTypes(ItemStack stack) {
        return new String[] { BaubleExpandedSlots.headType };
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return null;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    // endregion
}
