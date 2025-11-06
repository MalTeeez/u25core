package net.sxmaa.u25core.mixin.late.blockrenderer6343;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import Reika.ReactorCraft.Blocks.Multi.BlockGeneratorMulti;

@Mixin(value = BlockGeneratorMulti.class, remap = false)
public interface BlockGeneratorMultiAccessor {

    @Invoker("onCreateFullMultiBlock")
    void invokeOnCreateFullMultiBlock(World world, int x, int y, int z, Boolean complete);
}
