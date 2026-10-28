package net.sxmaa.u25core.mixins.late.nuclearcraft;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import nc.packet.PacketSideConfig;

@Mixin(value = PacketSideConfig.Handler.class, remap = false)
public class PacketSideConfigMixin {

    // The tile may not be loaded on the client yet
    @Inject(
        method = "onMessage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntity;getWorldObj()Lnet/minecraft/world/World;",
            remap = true),
        cancellable = true)
    private void u25core$skipMissingTile(CallbackInfoReturnable<IMessage> cir, @Local TileEntity te) {
        if (te == null) cir.setReturnValue(null);
    }
}
