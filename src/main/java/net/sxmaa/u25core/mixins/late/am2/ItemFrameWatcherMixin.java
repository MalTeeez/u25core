package net.sxmaa.u25core.mixins.late.am2;

import am2.ItemFrameWatcher;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemFrameWatcher.class, remap = false)
public class ItemFrameWatcherMixin {

    @Inject(
        method = "checkFrameRadius",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>()V"),
        cancellable = true,
        require = 1
    )
    private void checkFrameRadius(@Coerce Object frameComp, CallbackInfoReturnable<Boolean> cir, @Local(name = "frame") EntityItemFrame frame, @Local(name = "radius") int radius) {
        if (!frame.worldObj.checkChunksExist(
            (int) (frame.posX - radius), (int) (frame.posY - radius), (int) (frame.posZ - radius),
            (int) (frame.posX + radius), (int) (frame.posY + radius), (int) (frame.posZ + radius))) {
            cir.setReturnValue(false);
        }
    }
}
