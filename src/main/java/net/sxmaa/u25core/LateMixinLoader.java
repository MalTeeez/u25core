package net.sxmaa.u25core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import ic2.core.ExplosionIC2;
import net.sxmaa.u25core.config.ModConfig;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.common.FMLLog;

@LateMixin
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.u25core.late.json";
    }

    @Nonnull
    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        try {
            ConfigurationManager.registerConfig(ModConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        if (loadedMods.contains("EnderIO") && ModConfig.enderIOTweaks) {
            FMLLog.getLogger()
                .info("EnderIO is present & EnderIO Tweaks are actived, applying mixins");
            mixins.add("enderio.extractupgrade.SpeedUpgradesMixin");
            mixins.add("enderio.extractupgrade.ItemConduitMixin");
            mixins.add("enderio.extractupgrade.InventoryUpgradesMixin");
            mixins.add("enderio.extractupgrade.NetworkedInventoryMixin");
        }

        if (ModConfig.externalStructureLibMultiblocks && loadedMods.contains("structurelib")
            && loadedMods.contains("blockrenderer6343")
            && loadedMods.contains("DragonAPI")) {
            mixins.add("blockrenderer6343.BlocksToPlaceMixin");
            mixins.add("blockrenderer6343.GuiMultiblockHandlerAccessor");
            mixins.add("blockrenderer6343.ObserverWorldMixin");
            if (loadedMods.contains("ReactorCraft")) {
                mixins.add("blockrenderer6343.StructureCompatGuiHandlerMixin");
                mixins.add("blockrenderer6343.StructureUtilityMixin");
            }
        }

        if (ModConfig.addDefenseTechExplosionSoundToIC2Nuke && loadedMods.contains("DefenseTech") && loadedMods.contains("IC2")) {
            mixins.add("ic2.ExplosionIC2Mixin");
        }

        return mixins;
    }
}
