package net.sxmaa.u25core.mixins.early.perf;

import net.minecraft.world.storage.SaveHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla re-reads session.lock from disk on every chunk save. A passed check is trusted for a second, so another
 * instance taking the world over is still caught within a second; a mismatch still throws.
 */
@Mixin(SaveHandler.class)
public abstract class SaveHandlerSessionLockMixin {

    @Unique
    private static final long u25core$TRUST_NANOS = 1_000_000_000L;

    /** When the lock last checked out, 0 for never. */
    @Unique
    private volatile long u25core$checkedAt;

    @Inject(method = "checkSessionLock", at = @At("HEAD"), cancellable = true)
    private void u25core$skipRecentCheck(CallbackInfo ci) {
        long checkedAt = u25core$checkedAt;
        if (checkedAt != 0 && System.nanoTime() - checkedAt < u25core$TRUST_NANOS) ci.cancel();
    }

    // Only reached when the check passed; a mismatch throws before this
    @Inject(method = "checkSessionLock", at = @At("RETURN"))
    private void u25core$rememberCheck(CallbackInfo ci) {
        u25core$checkedAt = System.nanoTime();
    }
}
