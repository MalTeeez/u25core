package net.sxmaa.u25core.mixins.late.blockrenderer6343;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import Reika.DragonAPI.Base.BlockTileEnum;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import blockrenderer6343.client.world.ObserverWorld;

@Mixin(value = ObserverWorld.class, remap = false)
public abstract class ObserverWorldMixin {

    @Shadow
    public abstract TileEntity getTileEntity(int x, int y, int z);

    /**
     * When br6343 scans all the structures for the items they require, it uses the damage value of the droppped item
     * stack,
     * which does not always seem to match the ReC blocks.
     * Since the itemstacks are then later mapped back to the blocks, and their damage is used as the metadata,
     * we instead use the metadata here where it makes sense.
     */
    @WrapOperation(
        method = "estimateTierFromInfoContainer",
        at = @At(
            value = "INVOKE",
            target = "Lblockrenderer6343/client/utils/BRUtil;getDamageValue(Lnet/minecraft/world/World;Lnet/minecraft/block/Block;J)I"))
    public int useBlockMetadataForItemstack(World world, Block block, long pos, Operation<Integer> original) {
        int originalResult = original.call(world, block, pos);
        int blockMetadata = world.getBlockMetadata(0, 64, 0);
        if (originalResult == 0 && blockMetadata != 0) {
            return blockMetadata;
        }
        return originalResult;
    }

    /**
     * Resolve block to item placer if DAPI BlockTile.
     */
    @WrapOperation(
        method = "estimateTierFromInfoContainer",
        at = @At(value = "NEW", target = "(Lnet/minecraft/block/Block;II)Lnet/minecraft/item/ItemStack;"))
    public ItemStack handleReikaItemStacks(Block block, int amount, int damage, Operation<ItemStack> original) {
        if (block instanceof BlockTileEnum) {
            ItemStack itemStack = ReikaRenderHelper.getBlockItem(block, damage);
            if (itemStack != null) {
                return itemStack;
            }
            throw new RuntimeException();
        }
        return original.call(block, amount, damage);
    }

    // @WrapOperation(method = "estimateTierFromInfoContainer", at = @At(value = "NEW", target =
    // "(Lnet/minecraft/block/Block;II)Lnet/minecraft/item/ItemStack;"))
    // public ItemStack addDebugToNonMiddleController(Block block, int stackSize, int meta, Operation<ItemStack>
    // original) {
    // TileEntity originalTileEntity = this.getTileEntity(0, 64, 0);
    // if (originalTileEntity == null) {
    // int radius = 32;
    // for (int dA = -radius; dA < radius; dA++) {
    // for (int dB = -radius; dB < radius; dB++) {
    // for (int dC = -radius; dC < radius; dC++) {
    // int posX = dA;
    // int posY = 64 - dB;
    // int posZ = dC;
    // TileEntity te = this.getTileEntity(posX, posY, posZ);
    // if (te != null && IMultiblockInfoContainer.contains(te.getClass())) {
    // U25Core.LOG.error("Failed to get controller multiblock at default pos. Found alternative working TE with offset
    // {} {} {}. ", posX, posY, posZ);
    // }
    // }
    // }
    // }
    // }
    // return original.call(block, stackSize, meta);
    // }
}
