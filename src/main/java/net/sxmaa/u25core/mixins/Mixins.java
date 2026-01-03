package net.sxmaa.u25core.mixins;

import javax.annotation.Nonnull;

import net.sxmaa.u25core.config.ModConfig;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    LIGHT_AND_MOOD_UPDATE_CRASH_CATCH(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.enableLightAndMoodUpdateCrashPrevention)
            .setPhase(Phase.EARLY)
            .addCommonMixins("crashcatchers.WorldMixin")),

    INSIDE_OF_BLOCK_CRASH_CATCH(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.catchInsideOfBlockCrash)
            .setPhase(Phase.EARLY)
            .addCommonMixins("crashcatchers.ItemRendererMixin")),

    ENDER_IO_EXTRACT_UPGRADE_TWEAKS(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.enderIOTweaks)
            .addRequiredMod(TargetedMod.ENDER_IO)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "enderio.extractupgrade.SpeedUpgradesMixin",
                "enderio.extractupgrade.ItemConduitMixin",
                "enderio.extractupgrade.InventoryUpgradesMixin",
                "enderio.extractupgrade.NetworkedInventoryMixin")),

    DAPI_MULTIBLOCKS_SUPPORT(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.externalStructureLibMultiblocks)
            .addRequiredMod(TargetedMod.STRUCTURELIB)
            .addRequiredMod(TargetedMod.BR_6343)
            .addRequiredMod(TargetedMod.DRAGONAPI)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "blockrenderer6343.BlocksToPlaceMixin",
                "blockrenderer6343.GuiMultiblockHandlerAccessor",
                "blockrenderer6343.ObserverWorldMixin")),

    REACTORCRAFT_MULTIBLOCKS_SUPPORT(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.externalStructureLibMultiblocks)
            .addRequiredMod(TargetedMod.STRUCTURELIB)
            .addRequiredMod(TargetedMod.BR_6343)
            .addRequiredMod(TargetedMod.DRAGONAPI)
            .addRequiredMod(TargetedMod.REACTORCRAFT)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "blockrenderer6343.StructureCompatGuiHandlerMixin",
                "blockrenderer6343.StructureUtilityMixin",
                "blockrenderer6343.BlockTurbineMultiAccessor",
                "blockrenderer6343.BlockGeneratorMultiAccessor",
                "blockrenderer6343.BlockFlywheelMultiAccessor")),

    IC2_NUKE_EXPLOSION_SOUND(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.addDefenseTechExplosionSoundToIC2Nuke)
            .addRequiredMod(TargetedMod.DEFENSE_TECH)
            .addRequiredMod(TargetedMod.IC2)
            .setPhase(Phase.LATE)
            .addCommonMixins("ic2.ExplosionIC2Mixin")),

    CHROMATICRAFT_PENDANT_NERF(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.nerfCCEnhancedPendants)
            .addRequiredMod(TargetedMod.CHROMATICRAFT)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "chromaticraft.pendantnerf.ItemBoostedPendantMixin",
                "chromaticraft.pendantnerf.ItemPendantMixin")),

    CHROMATICRAFT_GUNS_NERF(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.nerfCCGuns)
            .addRequiredMod(TargetedMod.CHROMATICRAFT)
            .setPhase(Phase.LATE)
            .addCommonMixins("chromaticraft.gunnerf.ItemProjectileFiringToolMixin")),

    RTG_INCREASE_BASE_NOISE(
        new MixinBuilder()
            .setApplyIf(() -> true)
            .addRequiredMod(TargetedMod.RTG)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "rtg.TerrainBaseMixin",
                "rtg.MountainWithPassesEffectMixin"))

    ;
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
