package net.sxmaa.u25core.mixin.late.chromaticraft.gunnerf;

import Reika.ChromatiCraft.Auxiliary.Ability.AbilityHelper;
import Reika.ChromatiCraft.Base.ItemProjectileFiringTool;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Magic.PlayerElementBuffer;
import Reika.ChromatiCraft.Registry.ChromaItems;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.Chromabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemProjectileFiringTool.class, remap = false)
public abstract class ItemProjectileFiringToolMixin {

    @Inject(method = "fire", at = @At(value = "HEAD"), cancellable = true)
    public void addCostToFiring(ItemStack is, World world, EntityPlayer ep, boolean randomVec, CallbackInfo ci) {
        Item item = is.getItem();
        boolean isChainGun = item == ChromaItems.CHAINGUN.getItemInstance();
        boolean isSplashGun = item == ChromaItems.SPLASHGUN.getItemInstance();
        if (isChainGun || isSplashGun) {
            ElementTagCompound cost = AbilityHelper.instance.getUsageElementsFor(Chromabilities.LASER, ep).scale(isChainGun ? 4f : 10f);
            if (PlayerElementBuffer.instance.playerHas(ep, cost)) {
                PlayerElementBuffer.instance.removeFromPlayer(ep, cost);
            } else {
                ChromaSounds.FAIL.playSound(ep);
                ci.cancel();
            }
        }
    }
}
