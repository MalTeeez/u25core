package net.sxmaa.u25core.common.multiblocks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.constructable.IMultiblockInfoContainer;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import blockrenderer6343.api.utils.CreativeItemSource;
import blockrenderer6343.client.world.TrackedDummyWorld;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Abstract base class to provide a StructureLib implementation of external multiblock structures.
 * <p>
 * This class provides a framework for defining multiblock structures from external mods
 * by implementing the {@link IMultiblockInfoContainer} interface. It uses the Template Method
 * pattern where subclasses provide structure definitions through abstract methods that are
 * called during construction to initialize immutable configuration fields.
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>
 *
 * {
 *     &#64;code
 *     public class TelepadMultiblock extends IExternalMultiblock<TileTelePad> {
 *
 *         &#64;Override
 *         protected String[][] getStructureBlueprint() {
 *             return new String[][] { { "TTT", "TTT", "TTT" }, { "MMM", "MMM", "MMM" }, { "BBB", "BBB", "BBB" } };
 *         }
 *
 *         &#64;Override
 *         protected IStructureDefinition<TileTelePad> getStructureDefinition() {
 *             return StructureDefinition.<TileTelePad>builder()
 *                 .addShape("main", STRUCTURE_BLUEPRINT)
 *                 .addElement('T', StructureUtility.ofBlockAnyMeta(Blocks.gold_block))
 *                 .addElement('M', StructureUtility.ofBlockAnyMeta(Blocks.diamond_block))
 *                 .addElement('B', StructureUtility.ofBlockAnyMeta(Blocks.iron_block))
 *                 .build();
 *         }
 *
 *         &#64;Override
 *         protected Vector3i getControllerOffset() {
 *             return new Vector3i(1, 0, -1);
 *         }
 *
 *         &#64;Override
 *         public String getRequiredMod() {
 *             return "EnderIO";
 *         }
 *
 *         public static void register() {
 *             registerInstance(new TelepadMultiblock());
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * <b>Registration:</b><br>
 * Subclasses should implement a static {@code register()} method that creates an instance
 * and calls {@link #registerInstance(IExternalMultiblock)} during mod initialization.
 * <p>
 * <b>Important Notes:</b>
 * <ul>
 * <li>All abstract methods are called during construction to initialize final fields</li>
 * <li>Methods should return new objects and not depend on subclass instance state</li>
 * <li>The multiblock will only be registered if the required mod is loaded</li>
 * </ul>
 *
 * <p>
 * Notes:
 * If you get a crash for java.lang.NullPointerException: Cannot invoke "Object.getClass()" because "tTileEntity" is
 * null
 * That is due to structurelib checking the controller position (including offset) and finding a tile entity,
 * Probably due to the controller offset being wrong
 * </p>
 *
 * @param <T> The type of TileEntity that serves as the multiblock controller
 * @see IMultiblockInfoContainer
 * @see StructureDefinition
 */
public abstract class IExternalMultiblock<T extends TileEntity> implements IMultiblockInfoContainer<T> {

    @Nonnull
    public String[][] STRUCTURE_BLUEPRINT;

    /**
     * Provides the structure blueprint in the form of a 3D array of chars.
     * Called once during construction.
     * <p>
     * Blocks are evaluated as char[x][y][z], but can be switched to char[x][z][y]
     * with {@link StructureUtility#transpose(String[][])}.
     * <p>
     * For special chars & variants see
     * {@link com.gtnewhorizon.structurelib.structure.StructureDefinition.Builder#addShape(String, String[][])}.
     * <p>
     * The rough usage with the example STRUCTURE_BLUEPRINT should be:
     *
     * <pre>
     * {@code
     * // spotless:off
     * new String[][] {
     *      { "TTT", "TTT", "TTT" },
     *      { "MMM", "MMM", "MMM" },
     *      { "BBB", "BBB", "BBB" },
     * };
     * // spotless:on
     * }
     * </pre>
     */
    protected abstract String[][] getStructureBlueprint();

    @Nonnull
    public IStructureDefinition<T> STRUCTURE_DEFINITION_CREATIVE;
    @Nonnull
    public IStructureDefinition<T> STRUCTURE_DEFINITION_SURVIVAL;

    /**
     * Builds the multiblock definition using
     * {@link com.gtnewhorizon.structurelib.structure.StructureDefinition.Builder}
     * and the previously set STRUCTURE_BLUEPRINT.
     * Called once during construction.
     * <p>
     * <b>WARN: If you tamper with the constructor, make sure to first set STRUCTURE_BLUEPRINT.</b>
     * <p>
     * Basic form should be:
     *
     * <pre>
     * {@code
     * StructureDefinition
     *         .<TileEntity>builder()
     *         .addShape("main", getAllowHotswap() ? getStructureBlueprint() : STRUCTURE_BLUEPRINT)
     *         .addElement('T', StructureUtility.ofBlockAnyMeta(Blocks.gold_block))
     *         .addElement('M', StructureUtility.ofBlockAnyMeta(Blocks.diamond_block))
     *         .addElement('B', StructureUtility.ofBlockAnyMeta(Blocks.iron_block))
     *         .build();
     * }
     * </pre>
     */
    protected abstract IStructureDefinition<T> getStructureDefinition(String[][] blueprint);

    protected IStructureDefinition<T> getStructureDefinition() {
        String[][] blueprint = getAllowHotswap() ? getStructureBlueprint() : STRUCTURE_BLUEPRINT;
        return getStructureDefinition(blueprint);
    }

    /**
     * Constructs a multiblock for an external mod with the given structure definition.
     * Uses all abstract getters to set constants to default values.
     * Do not call the super if overriding this.
     */
    public IExternalMultiblock() {
        STRUCTURE_BLUEPRINT = getStructureBlueprint();
        STRUCTURE_DEFINITION_CREATIVE = getStructureDefinition();
        STRUCTURE_DEFINITION_SURVIVAL = getStructureDefinition(getSafeBlueprint());
    }

    @Override
    public void construct(ItemStack item, boolean hintsOnly, T tileEntity, ExtendedFacing aSide) {
        ExtendedFacing facing = getDefaultStructureFacing(aSide, tileEntity) == null ? aSide : getDefaultStructureFacing(aSide, tileEntity);
        Vector3i controllerOffset = getControllerOffset(tileEntity, facing);

        int baseX = tileEntity.xCoord + controllerOffset.x;
        int baseY = tileEntity.yCoord + controllerOffset.y;
        int baseZ = tileEntity.zCoord + controllerOffset.z;
        IStructureDefinition<T> structureDefinition = getAllowHotswap() ? getStructureDefinition()
            : STRUCTURE_DEFINITION_CREATIVE;

        boolean buildDone = structureDefinition.buildOrHints(
            tileEntity,
            item,
            "main",
            tileEntity.getWorldObj(),
            facing,
            baseX,
            baseY,
            baseZ,
            0,
            0,
            0,
            hintsOnly);

        structureDefinition.check(
            tileEntity,
            "main",
            tileEntity.getWorldObj(),
            facing,
            baseX,
            baseY,
            baseZ,
            0,
            0,
            0,
            false);
        if (!hintsOnly && buildDone) {
            postConstructCheck(tileEntity, facing);
        }
    }

    @Override
    public int survivalConstruct(ItemStack item, int elementBudget, ISurvivalBuildEnvironment env, T tileEntity,
        ExtendedFacing aSide) {
        ExtendedFacing facing = getDefaultStructureFacing(aSide, tileEntity) == null ? aSide : getDefaultStructureFacing(aSide, tileEntity);
        Vector3i controllerOffset = getControllerOffset(tileEntity, facing);

        int baseX = tileEntity.xCoord + controllerOffset.x;
        int baseY = tileEntity.yCoord + controllerOffset.y;
        int baseZ = tileEntity.zCoord + controllerOffset.z;
        IStructureDefinition<T> structureDefinition = getAllowHotswap() ? getStructureDefinition(getSafeBlueprint())
            : STRUCTURE_DEFINITION_SURVIVAL;

        // Force creative construct for gui builds
        if (env.getSource() instanceof CreativeItemSource || tileEntity.getWorldObj() instanceof TrackedDummyWorld) {
            return -2;
        }

        int structureState = structureDefinition.survivalBuild(
            tileEntity,
            item,
            "main",
            tileEntity.getWorldObj(),
            facing,
            baseX,
            baseY,
            baseZ,
            0,
            0,
            0,
            getElementBudgetFromTriggerItem(item, elementBudget),
            env,
            false);

        structureDefinition.check(
            tileEntity,
            "main",
            tileEntity.getWorldObj(),
            facing,
            baseX,
            baseY,
            baseZ,
            0,
            0,
            0,
            false);
        if (structureState == -1 || structureState == 0) {
            postConstructCheck(tileEntity, facing);
        }
        return structureState;
    }

    protected String[][] getSafeBlueprint() {
        String[][] blueprint = getAllowHotswap() ? getStructureBlueprint() : STRUCTURE_BLUEPRINT;
        if (getControllerChar() == null) {
            return blueprint;
        }

        for (int i = 0; i < blueprint.length; i++) {
            if (blueprint[i] != null) {
                for (int j = 0; j < blueprint[i].length; j++) {
                    if (blueprint[i][j] != null) {
                        blueprint[i][j] = blueprint[i][j].replace(getControllerChar(), "~");
                    }
                }
            }
        }
        return blueprint;
    }

    /**
     * An optional function to run after building / adding to the structure.
     * We can not guarantee that this check is only called when the structure is fully done.
     * Intended for forcing manual checks of multiblock complete-ness.
     */
    protected void postConstructCheck(T te, ExtendedFacing facing) {};

    /**
     * Override the provided element budget with a custom one.
     */
    protected int getElementBudgetFromTriggerItem(ItemStack item, int providedBudget) {
        return 1;
    }

    /**
     * Provides the relative offset from 0, 0, 0 to the multiblock's controller.
     * Called once during construction.
     */
    protected Vector3i getControllerOffset(T te, ExtendedFacing facing) {
        return new Vector3i(0, 0, 0);
    };

    /**
     * Override the provided structure facing with a custom one.
     */
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, T te) {
        return null;
    }

    /**
     * Set the character used for the controller in the structure blueprint.
     * Will replace that character at runtime with ~.
     * This fixes an issue where the multiblock can't be constructed in survival because the controller block does
     * not resolve to the same item.
     * <p>
     * Do not use this char more than once in the blueprint.
     */
    protected String getControllerChar() {
        return null;
    }

    protected boolean getAllowHotswap() {
        return false;
    }

    public String getName() {
        return this.getClass()
            .getSimpleName();
    }

    public String getDimensions() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String[] getDescription(ItemStack item) {
        return new String[] { this.getName() + " Multiblock", "Dimensions: " + this.getDimensions(),
            "Controller: " + getControllerTileClass() };
    }

    @SuppressWarnings("unchecked")
    public Class<? extends TileEntity> getControllerTileClass() {
        Type superclass = this.getClass()
            .getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            Type typeArg = ((ParameterizedType) superclass).getActualTypeArguments()[0];
            if (typeArg instanceof Class) {
                return (Class<? extends TileEntity>) typeArg;
            }
        }
        throw new IllegalStateException("Could not determine controller tile class for " + this.getClass());
    }

    protected static <T extends TileEntity> void registerInstance(IExternalMultiblock<T> instance) {
        IMultiblockInfoContainer.registerTileClass(instance.getControllerTileClass(), instance);
    }

    /**
     * Registers this class with its default values.
     */
    public void registerSelf() {
        registerInstance(this);
    }

}
