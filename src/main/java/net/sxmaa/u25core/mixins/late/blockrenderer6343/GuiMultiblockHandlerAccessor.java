package net.sxmaa.u25core.mixins.late.blockrenderer6343;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import blockrenderer6343.integration.nei.GuiMultiblockHandler;

@Mixin(value = GuiMultiblockHandler.class, remap = false)
public interface GuiMultiblockHandlerAccessor {

    @Accessor("stackForm")
    ItemStack u25core$getStackForm();

}
