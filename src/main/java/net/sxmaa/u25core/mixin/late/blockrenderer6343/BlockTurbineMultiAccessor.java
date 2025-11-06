package net.sxmaa.u25core.mixin.late.blockrenderer6343;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import Reika.ReactorCraft.Blocks.Multi.BlockTurbineMulti;

@Mixin(value = BlockTurbineMulti.class, remap = false)
public interface BlockTurbineMultiAccessor {

    @Invoker("onCreateFullMultiBlock")
    void invokeOnCreateFullMultiBlock(World world, int x, int y, int z, Boolean complete);
}
