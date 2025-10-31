package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import net.minecraft.init.Blocks;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.TileEntityFusionMarker;

public class ToroidMagnetsMultiblock extends IExternalMultiblock<TileEntityFusionMarker> {

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return StructureUtility.transpose(new String[][] {
            {
                "          T T I T T          ",
                "        T           T        ",
                "      T               T      ",
                "                             ",
                "    T                   T    ",
                "                             ",
                "  T                       T  ",
                "                             ",
                " T                         T ",
                "                             ",
                "T                           T",
                "                             ",
                "T                           T",
                "                             ",
                "I                           I",
                "                             ",
                "T                           T",
                "                             ",
                "T                           T",
                "                             ",
                " T                         T ",
                "                             ",
                "  T                       T  ",
                "                             ",
                "    T                   T    ",
                "                             ",
                "      T               T      ",
                "        T           T        ",
                "          T T I T T          ",
            },
            {
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "              M              ", // middle
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
            },
            {
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
                "                             ",
            },
            {
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
                "RWRWRWRWRWRWRWRWRWRWRWRWRWRWR",
                "WRWRWRWRWRWRWRWRWRWRWRWRWRWRW",
            }
        });
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityFusionMarker> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityFusionMarker>builder()
            .addShape("main", getAllowHotswap() ? getStructureBlueprint() : STRUCTURE_BLUEPRINT)
            .addElement('W', StructureUtility.ofBlock(Blocks.stained_hardened_clay, 0))
            .addElement('R', StructureUtility.ofBlock(Blocks.stained_hardened_clay, 14))
            .addElement('I', StructureUtility.ofBlock(ReactorTiles.INJECTOR.getBlock(), 7))
            .addElement(
                'M',
                StructureUtility.ofBlock(ReactorTiles.MARKER.getBlock(), ReactorTiles.MARKER.getBlockMetadata()))
            .addElement(
                'T',
                StructureUtility.ofBlock(ReactorTiles.MAGNET.getBlock(), ReactorTiles.MAGNET.getBlockMetadata()))
            .build();
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityFusionMarker te) {
        return new Vector3i(14, 1, -14);
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityFusionMarker te) {
        return ExtendedFacing.DEFAULT;
    }

    @Override
    protected String getControllerChar() {
        return "M";
    }

    public static void registerSelf() {
        registerInstance(new ToroidMagnetsMultiblock());
    }
}
