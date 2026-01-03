package net.sxmaa.u25core.mixins.late.blockrenderer6343;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import Reika.ReactorCraft.Items.ItemReactorPlacer;

@Mixin(value = StructureUtility.class, remap = false)
public class StructureUtilityMixin {

    /**
     * Disable IllegalArgumentException for non ItemBlocks if block is DAPI BlockTile
     */
    @Definition(id = "ItemBlock", type = ItemBlock.class)
    @Expression("? instanceof ItemBlock")
    @ModifyExpressionValue(
        method = "survivalPlaceBlock(Lnet/minecraft/item/ItemStack;Lcom/gtnewhorizon/structurelib/util/ItemStackPredicate$NBTMode;Lnet/minecraft/nbt/NBTTagCompound;ZLnet/minecraft/world/World;IIILcom/gtnewhorizon/structurelib/structure/IItemSource;Lnet/minecraft/entity/player/EntityPlayer;Ljava/util/function/Consumer;)Lcom/gtnewhorizon/structurelib/structure/IStructureElement$PlaceResult;",
        at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static boolean expandItemBlockCheck(boolean original, ItemStack stack) {
        return original || stack.getItem() instanceof ItemReactorPlacer;
    }
}
