package net.sxmaa.u25core.mixins.late.deepercaves;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * DeeperCaves registers incendite ore with a drop range (randval) of 0, so quantityDropped does rand.nextInt(0) and
 * throws when the ore is broken or NEI/RotaryCraft show its drops. The fortune path goes through quantityDropped too.
 * DeeperCaves isn't a compile dependency, so quantityDropped is targeted by its obfuscated name.
 */
@Pseudo
@Mixin(targets = "com.kpabr.DeeperCaves.block.BlockOreBase", remap = false)
public abstract class BlockOreBaseDropMixin {

    @Shadow
    public int quantity;

    @Shadow
    public int randval;

    @Inject(method = "func_149745_a", at = @At("HEAD"), cancellable = true)
    private void u25core$fixEmptyRange(Random rand, CallbackInfoReturnable<Integer> cir) {
        if (randval <= 0) cir.setReturnValue(quantity);
    }
}
