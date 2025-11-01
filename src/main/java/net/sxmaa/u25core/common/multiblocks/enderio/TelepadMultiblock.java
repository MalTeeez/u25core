package net.sxmaa.u25core.common.multiblocks.enderio;

import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.telepad.TileTelePad;

public class TelepadMultiblock extends IExternalMultiblock<TileTelePad> {

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return new String[][] {
            { "TTT", "TTT", "TTT" },
        };
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileTelePad> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileTelePad>builder()
            .addShape("main", blueprint)
            .addElement('T', StructureUtility.ofBlockAnyMeta(EnderIO.blockTelePad))
            .build();
    }

    @Override
    protected Vector3i getControllerOffset(TileTelePad te) {
        return new Vector3i(-1, 0, 1);
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileTelePad te) {
        return ExtendedFacing.UP_NORMAL_VERTICAL;
    }
}
