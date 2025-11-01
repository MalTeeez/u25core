package net.sxmaa.u25core.mixin.early.crashcatchers;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class)
public class WorldMixin {

    @Inject(method = "func_147467_a", at = @At("HEAD"), cancellable = true)
    public void catchPlayMoodSoundAndCheckLightCrash(int p_147467_1_, int p_147467_2_, Chunk p_147467_3_,
        CallbackInfo ci) {
        if (p_147467_3_ == null) {
            ci.cancel();
        }
    }
}
