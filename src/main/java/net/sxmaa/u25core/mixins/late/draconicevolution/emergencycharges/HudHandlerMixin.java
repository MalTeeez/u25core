package net.sxmaa.u25core.mixins.late.draconicevolution.emergencycharges;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.brandon3055.draconicevolution.client.handler.HudHandler;
import com.brandon3055.draconicevolution.common.handler.ConfigHandler;
import com.brandon3055.draconicevolution.common.items.armor.ICustomArmor;
import com.brandon3055.draconicevolution.common.utils.ItemNBTHelper;

@Mixin(value = HudHandler.class, remap = false)
public class HudHandlerMixin {

    @Shadow
    private float armorStatsFadeOut;

    // Runs after the stat text is drawn, x is already shifted for the rotated layout at this point
    @Inject(
        method = "drawArmorHUD",
        at = @At(
            value = "INVOKE",
            target = "Lcom/brandon3055/draconicevolution/client/handler/ResourceHandler;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private void u25core$drawCharges(int x, int y, boolean rotated, double scale, CallbackInfo ci) {
        if (ConfigHandler.hudSettings[9] != 1) return;
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack chest = mc.thePlayer.inventory.armorInventory[2];
        if (chest == null || !(chest.getItem() instanceof ICustomArmor)) return;

        String charges = "EC: " + ItemNBTHelper.getInteger(chest, "EmergencyCharges", 0);
        int color = ((int) (Math.min(armorStatsFadeOut, 1F) * 240F) + 0x10 << 24) | 0x00FFFFFF;
        if (rotated) mc.fontRenderer.drawStringWithShadow(charges, x, y + 18, color);
        else mc.fontRenderer.drawStringWithShadow(charges, x + 18, y + 64, color);
    }
}
