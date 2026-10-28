package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import nc.packet.PacketAssemblerState;

@Mixin(value = PacketAssemblerState.Handler.class, remap = false)
public class PacketAssemblerStateMixin {

    // The server looked up whatever position the client sent, loading chunks anywhere
    @WrapOperation(
        method = "onMessage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getTileEntity(III)Lnet/minecraft/tileentity/TileEntity;",
            remap = true))
    private TileEntity u25core$onlyNearbyLoaded(World world, int x, int y, int z, Operation<TileEntity> original,
        @Local(argsOnly = true) MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (!world.blockExists(x, y, z) || player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) > 64) return null;
        return original.call(world, x, y, z);
    }
}
