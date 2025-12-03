package net.sxmaa.u25core.mixin.early.crashcatchers;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.IIcon;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemRenderer.class)
public class ItemRendererMixin {

    @WrapMethod(method = "renderInsideOfBlock")
    public void catchRenderInsideOfBlockCrash(float p_78446_1_, IIcon p_78446_2_, Operation<Void> original) {
        try {
            original.call(p_78446_1_, p_78446_2_);
        } catch (NullPointerException ignored) {
        }
    }
}
