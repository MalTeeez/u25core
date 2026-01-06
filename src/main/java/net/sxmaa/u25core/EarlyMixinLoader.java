package net.sxmaa.u25core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.mixins.Mixins;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.sxmaa.u25core.util.GitRevisionChecker;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public EarlyMixinLoader() {
        try {
            ConfigurationManager.registerConfig(ModConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        if (ModConfig.setGitRev) {
            GitRevisionChecker.GitInfo gitInfo = GitRevisionChecker.getGitInfo();
            if (gitInfo.isAvailable) {
                String gitRev = ModConfig.gitRevPrefix + gitInfo.revision + (gitInfo.isDirty ? "-dirty" : "");
                FMLLog.getLogger().info("Running with Git Rev : {} (detected at runtime)", gitRev);
                System.setProperty("modpack.gitrev", gitRev);
            }
        }
    }

    @Override
    public String getMixinConfig() {
        return "mixins.u25core.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixins.class, loadedCoreMods);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
