package net.sxmaa.u25core.common.multiblocks;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Registry.ReactorTiles;

/**
 */
public class TestMultiblock extends IExternalMultiblock<TileEntityChest> {

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return new String[][] {
            { "CDG" },
        };
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityChest> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityChest>builder()
            .addShape("main", blueprint)
            .addElement('D', StructureUtility.ofBlock(ReactorTiles.MAGNETPIPE.getBlock(), 1))
            .addElement('C', StructureUtility.ofBlockAnyMeta(Blocks.chest))
            .addElement('G', StructureUtility.ofBlockAnyMeta(Blocks.gold_block))
            .build();
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityChest te, ExtendedFacing facing) {
        return new Vector3i(0, 0, 0);
    }

    @Override
    protected String getControllerChar() {
        return "C";
    }
}
