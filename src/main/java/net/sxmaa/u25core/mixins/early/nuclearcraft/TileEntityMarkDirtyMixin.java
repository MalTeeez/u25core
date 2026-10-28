package net.sxmaa.u25core.mixins.early.nuclearcraft;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NuclearCraft tiles call markDirty() every tick. Forge's markDirty also fires onNeighborChange on all 6 neighbours
 * (and reads blocks 2 away, loading chunks across borders), which only exists to update comparators. So for NC tiles
 * just mark the chunk for saving, and only do the full update when the block's comparator output actually changed.
 */
@Mixin(TileEntity.class)
public abstract class TileEntityMarkDirtyMixin {

    @Shadow
    protected World worldObj;

    @Shadow
    public int xCoord;

    @Shadow
    public int yCoord;

    @Shadow
    public int zCoord;

    @Shadow
    public abstract Block getBlockType();

    @Unique
    private int u25core$lastComparatorOutput = -1;

    @Inject(method = "markDirty", at = @At("HEAD"), cancellable = true)
    private void u25core$onlyMarkChunk(CallbackInfo ci) {
        if (worldObj == null || !getClass().getName()
            .startsWith("nc.tile.")) return;
        Block block = getBlockType();
        if (block.hasComparatorInputOverride()) {
            // NC's comparator code dereferences the tile at this position, which is gone e.g. after a meltdown
            if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != (Object) this) return;
            int output = block.getComparatorInputOverride(worldObj, xCoord, yCoord, zCoord, 0);
            if (output != u25core$lastComparatorOutput) {
                u25core$lastComparatorOutput = output;
                return;
            }
        }
        worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, (TileEntity) (Object) this);
        ci.cancel();
    }
}
