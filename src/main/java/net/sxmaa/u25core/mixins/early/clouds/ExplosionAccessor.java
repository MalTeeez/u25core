package net.sxmaa.u25core.mixins.early.clouds;

import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {

    @Accessor("worldObj")
    World u25core$getWorld();
}
