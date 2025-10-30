package net.sxmaa.u25core.mixin.late.blockrenderer6343;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import blockrenderer6343.client.renderer.ImmediateWorldSceneRenderer;
import blockrenderer6343.integration.nei.GuiMultiblockHandler;

/**
 * Accessor for the static renderer field declared on GuiMultiblockHandler.
 * Because the field is static, the accessor methods are static.
 */
@Mixin(value = GuiMultiblockHandler.class, remap = false)
public interface GuiMultiblockHandlerAccessor {

    @Accessor("renderer")
    static ImmediateWorldSceneRenderer u25core$getRenderer() {
        throw new RuntimeException();
    };

    @Accessor("stackForm")
    ItemStack u25core$getStackForm();

}
