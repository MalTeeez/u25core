package net.sxmaa.u25core.radiation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Lowers the radiation of its chunk while it has filters, see {@link RadiationConfig#scrubberEfficiency}. Takes
 * charcoal or coal as filters, from a hopper or by right-click.
 */
public class TileScrubber extends TileEntity implements ISidedInventory {

    private static final int[] SLOTS = { 0 };

    private ItemStack filters;
    /** Ticks left on the current filter. */
    private int filterTicks;

    public static boolean isFilter(ItemStack stack) {
        return stack != null && stack.getItem() == Items.coal;
    }

    public boolean isRunning() {
        return filterTicks > 0 || filters != null && filters.stackSize > 0;
    }

    /** Uses up filter time, called when its chunk updates. */
    void consume(int ticks) {
        filterTicks -= ticks;
        while (filterTicks <= 0 && filters != null && filters.stackSize > 0) {
            filters.stackSize--;
            if (filters.stackSize <= 0) filters = null;
            filterTicks += RadiationConfig.scrubberFilterTicks;
        }
        if (filterTicks < 0) filterTicks = 0;
        markDirty();
    }

    public int getFilterCount() {
        return filters == null ? 0 : filters.stackSize;
    }

    public int getFilterTicks() {
        return filterTicks;
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null) RadiationManager.INSTANCE.notifySource(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("filterTicks", filterTicks);
        if (filters != null) tag.setTag("filters", filters.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        filterTicks = tag.getInteger("filterTicks");
        filters = tag.hasKey("filters") ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("filters")) : null;
    }

    // region Inventory, one input slot for filters

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return filters;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (filters == null) return null;
        ItemStack taken = filters.splitStack(Math.min(amount, filters.stackSize));
        if (filters.stackSize <= 0) filters = null;
        markDirty();
        return taken;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        filters = stack;
        markDirty();
        if (worldObj != null && !worldObj.isRemote) RadiationManager.INSTANCE.notifySource(this);
    }

    @Override
    public String getInventoryName() {
        return "container.u25core.scrubber";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isFilter(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isFilter(stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return false;
    }

    // endregion
}
