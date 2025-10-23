package net.sxmaa.u25core.common.multiblocks;

import org.joml.Vector3i;

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
    protected IStructureDefinition<TileTelePad> getStructureDefinition() {
        return StructureDefinition.<TileTelePad>builder()
            .addShape("main", STRUCTURE_BLUEPRINT)
            .addElement('T', StructureUtility.ofBlockAnyMeta(EnderIO.blockTelePad))
            .build();
    }

    @Override
    protected Vector3i getControllerOffset() {
        return new Vector3i(-1, 0, 1);
    }

    @Override
    public String getRequiredMod() {
        return "EnderIO";
    }

    public static void registerSelf() {
        registerInstance(new TelepadMultiblock());
    }
}
