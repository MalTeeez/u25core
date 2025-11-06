package net.sxmaa.u25core.mixin.late.ic2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import ic2.core.ExplosionIC2;

@Mixin(value = ExplosionIC2.class, remap = false)
public abstract class ExplosionIC2Mixin {

    @Shadow
    protected abstract boolean isNuclear();

    @ModifyArgs(
        method = "doExplosion",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSoundEffect(DDDLjava/lang/String;FF)V"))
    public void useDefenseTechSound(Args args) {
        if (this.isNuclear()) {
            args.set(3, "defense:explosion");
            args.set(4, 7F);
        }
    }
}
