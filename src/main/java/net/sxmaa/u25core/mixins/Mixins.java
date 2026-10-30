package net.sxmaa.u25core.mixins;

import javax.annotation.Nonnull;

import net.sxmaa.u25core.config.ModConfig;
import net.sxmaa.u25core.radiation.RadiationConfig;

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
            .setApplyIf(() -> ModConfig.rtgMountainousTerrainRougher)
            .addRequiredMod(TargetedMod.RTG)
            .setPhase(Phase.LATE)
            .addCommonMixins("rtg.MountainWithPassesEffectMixin")),

    FIX_AM2_ITEMFRAMES_LOADING_CHUNKS(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.preventAM2ItemFrameChunkloading)
            .addRequiredMod(TargetedMod.AM2)
            .setPhase(Phase.LATE)
            .addCommonMixins("am2.ItemFrameWatcherMixin")),

    DRACONIC_EVOLUTION_BALANCE(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.draconicEvolutionBalance)
            .addRequiredMod(TargetedMod.DRACONIC_EVOLUTION)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "draconicevolution.balance.EntityChaosGuardianMixin",
                "draconicevolution.balance.EntityCustomDragonMixin",
                "draconicevolution.balance.EntityDragonProjectileMixin",
                "draconicevolution.balance.TileEnderResurrectionMixin",
                "draconicevolution.balance.DraconicArmorMixin",
                "draconicevolution.balance.WyvernArmorMixin",
                "draconicevolution.balance.UpgradeTierMixin")),

    DRACONIC_EVOLUTION_EMERGENCY_CHARGES(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.draconicEvolutionEmergencyCharges)
            .addRequiredMod(TargetedMod.DRACONIC_EVOLUTION)
            .setPhase(Phase.LATE)
            .addCommonMixins("draconicevolution.emergencycharges.CustomArmorHandlerMixin")
            .addClientMixins("draconicevolution.emergencycharges.HudHandlerMixin")),

    AE2_P2P_RF_CAP(
        new MixinBuilder().setApplyIf(() -> ModConfig.p2pRFPerTick > 0)
            .addRequiredMod(TargetedMod.AE2)
            .setPhase(Phase.LATE)
            .addCommonMixins("ae2.RFP2PBudgetMixin")),

    AE2_P2P_EU_CAP(
        new MixinBuilder().setApplyIf(() -> ModConfig.p2pEUPerTick > 0)
            .addRequiredMod(TargetedMod.AE2)
            .setPhase(Phase.LATE)
            .addCommonMixins("ae2.EUP2PBudgetMixin")),

    TESSERACT_RF_CAP(
        new MixinBuilder().setApplyIf(() -> ModConfig.tesseractRFPerTick > 0)
            .addRequiredMod(TargetedMod.THERMAL_EXPANSION)
            .setPhase(Phase.LATE)
            .addCommonMixins("thermalexpansion.TesseractBudgetMixin")),

    MEKANISM_HEAVY_WATER_ON_MARS(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.restrictHeavyWater)
            .addRequiredMod(TargetedMod.MEKANISM)
            .setPhase(Phase.LATE)
            .addCommonMixins("mekanism.ElectricPumpMixin", "mekanism.ElectrolyticSeparatorMixin")),

    REACTORCRAFT_HEAVY_WATER_ON_PLANETS(
        new MixinBuilder()
            .setApplyIf(() -> ModConfig.restrictHeavyWater)
            .addRequiredMod(TargetedMod.DRAGONAPI)
            .addRequiredMod(TargetedMod.REACTORCRAFT)
            .setPhase(Phase.LATE)
            .addCommonMixins("reactorcraft.HeavyWaterExtractionMixin")),

    REACTORCRAFT_RADIATION(
        new MixinBuilder()
            .setApplyIf(() -> RadiationConfig.enabled && RadiationConfig.replaceReactorCraftRadiation)
            .addRequiredMod(TargetedMod.DRAGONAPI)
            .addRequiredMod(TargetedMod.REACTORCRAFT)
            .setPhase(Phase.LATE)
            .addCommonMixins(
                "reactorcraft.RadiationEffectsMixin",
                "reactorcraft.EntityRadiationMixin",
                "reactorcraft.NuclearCoreSourceMixin",
                "reactorcraft.WasteUnitSourceMixin")),

    IC2_RADIATION(
        new MixinBuilder()
            .setApplyIf(() -> RadiationConfig.enabled)
            .addRequiredMod(TargetedMod.IC2)
            .setPhase(Phase.LATE)
            .addCommonMixins("ic2.ExplosionIC2RadiationMixin", "ic2.NuclearReactorSourceMixin", "ic2.TerraWartMixin")),

    FORESTRY_RADIATION(
        new MixinBuilder()
            .setApplyIf(() -> RadiationConfig.enabled)
            .addRequiredMod(TargetedMod.FORESTRY)
            .setPhase(Phase.LATE)
            .addCommonMixins("forestry.RadioactiveBeeMixin")),

    EXTRAPLANETS_RADIATION(
        new MixinBuilder()
            .setApplyIf(() -> RadiationConfig.enabled)
            .addRequiredMod(TargetedMod.EXTRAPLANETS)
            .setPhase(Phase.LATE)
            .addCommonMixins("extraplanets.RadioactiveWaterMixin")),

    // Early, the targets are vanilla classes
    RADIATION_CORE(
        new MixinBuilder()
            .setApplyIf(() -> RadiationConfig.enabled)
            .setPhase(Phase.EARLY)
            .addCommonMixins("radiation.ChunkSourceTrackingMixin", "radiation.RadiationPotionMixin"))

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
