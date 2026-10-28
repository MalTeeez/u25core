package net.sxmaa.u25core.mixins.early.radiation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.sxmaa.u25core.api.radiation.IRadiationSource;
import net.sxmaa.u25core.radiation.RadiationManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks the chunk of every radiation source as it's placed or loaded, so sources don't have to announce themselves.
 */
@Mixin(Chunk.class)
public class ChunkSourceTrackingMixin {

    @Shadow
    public World worldObj;

    @Inject(method = "func_150812_a", at = @At("TAIL"))
    private void u25core$trackSource(int x, int y, int z, TileEntity tile, CallbackInfo ci) {
        if (tile instanceof IRadiationSource && worldObj != null && !worldObj.isRemote) {
            RadiationManager.INSTANCE.notifySource(tile);
        }
    }
}
