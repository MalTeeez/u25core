package net.sxmaa.u25core.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

import cpw.mods.fml.common.Loader;

public enum TargetedMod implements ITargetMod {

    DRAGONAPI("Reika.DragonAPI.Auxiliary.DragonAPIASMHandler", "DragonAPI"),
    ENDER_IO("EnderIO"),
    STRUCTURELIB("structurelib"),
    BR_6343("blockrenderer6343"),
    REACTORCRAFT("ReactorCraft"),
    DEFENSE_TECH("DefenseTech"),
    IC2("IC2"),
    CHROMATICRAFT("ChromatiCraft"),
    RTG("RTG");

    private final TargetModBuilder builder;
    private final String id;

    TargetedMod(String modId) {
        this(null, modId, null);
    }

    TargetedMod(String coreModClass, String modId) {
        this(coreModClass, modId, null);
    }

    TargetedMod(String coreModClass, String modId, String targetClass) {
        this.id = modId;
        this.builder = new TargetModBuilder().setCoreModClass(coreModClass)
            .setModId(modId)
            .setTargetClass(targetClass);
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(id);
    }

    @Nonnull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
