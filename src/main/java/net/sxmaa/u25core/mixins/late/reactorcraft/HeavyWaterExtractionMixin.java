package net.sxmaa.u25core.mixins.late.reactorcraft;

import net.minecraft.world.World;
import net.sxmaa.u25core.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import Reika.ReactorCraft.TileEntities.TileEntityHeavyPump;

@Mixin(value = TileEntityHeavyPump.HeavyWaterExtraction.class, remap = false)
public abstract class HeavyWaterExtractionMixin {

    @Shadow
    private boolean isOceanFloor(World world, int x, int y, int z) {
        throw new AssertionError();
    }

    // Heavy water only on Europa and Mars, ignoring ReactorCraft's dimension config. Neither has vanilla ocean
    // biomes, so the biome check is dropped; the depth (below y 45) and water column checks stay.
    @Inject(method = "canPerform", at = @At("HEAD"), cancellable = true)
    private void u25core$onlyOnPlanets(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        int dim = world.provider.dimensionId;
        boolean planet = dim == ModConfig.heavyWaterEuropaDimension || dim == ModConfig.heavyWaterMarsDimension;
        cir.setReturnValue(planet && y < 45 && isOceanFloor(world, x, y, z));
    }

    @ModifyReturnValue(method = "getExtractedAmount", at = @At("RETURN"))
    private int u25core$slowOnMars(int amount, @Local(argsOnly = true) World world) {
        return world.provider.dimensionId == ModConfig.heavyWaterMarsDimension
            ? amount / ModConfig.reactorCraftMarsHeavyWaterDivisor
            : amount;
    }
}
