package net.sxmaa.u25core.mixin.late.blockrenderer6343;

import Reika.ReactorCraft.Blocks.Multi.BlockFlywheelMulti;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BlockFlywheelMulti.class, remap = false)
public interface BlockFlywheelMultiAccessor {
    @Invoker("onCreateFullMultiBlock")
    void invokeOnCreateFullMultiBlock(World world, int x, int y, int z, Boolean complete);
}
