package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.util.DirectionUtil;

import org.joml.Vector3i;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Blocks.Multi.BlockHeaterMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.Fusion.TileEntityFusionHeater;
import Reika.RotaryCraft.Registry.BlockRegistry;

public class PreheaterMultiblock extends IExternalMultiblock<TileEntityFusionHeater> {

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return StructureUtility.transpose(new String[][] {
            {
                "     ",
                "     ",
                "  M  ",
                "     ",
                "     ",
            },            {
                "     ",
                " CEC ",
                " EME ",
                " CEC ",
                "     ",
            },
            {
                "CEEEC",
                "EIIIE",
                "EIMIE",
                "EIIIE",
                "CEEEC",
            },
            {
                "EFFFE",
                "FIIIF",
                "FIMIF",
                "FIIIF",
                "EFFFE",
            },
            {
                "EFFFE",
                "FIIIF",
                "PPHPP",
                "FILIF",
                "EFLFE",
            },
            {
                "EFFFE",
                "FIIIF",
                "FIIIF",
                "FIIIF",
                "EFFFE",
            },
            {
                "CEEEC",
                "EFFFE",
                "EFFFE",
                "EFFFE",
                "CEEEC",
            },
        });
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityFusionHeater> getStructureDefinition(String[][] blueprint) {
        Map<Block, Integer> pipes = new HashMap<>() {

            {
                put(BlockRegistry.PIPING.getBlockInstance(), 1);
                put(BlockRegistry.PIPING.getBlockInstance(), 8);
            }
        };
        return StructureDefinition.<TileEntityFusionHeater>builder()
            .addShape("main", blueprint)
            .addElement(
                'H',
                StructureUtility
                    .ofBlock(ReactorTiles.HEATER.getBlockInstance(), ReactorTiles.HEATER.getBlockMetadata()))
            .addElement('L', StructureUtility.ofBlock(ReactorBlocks.HEATERMULTI.getBlockInstance(), 0))
            .addElement('I', StructureUtility.ofBlock(ReactorBlocks.HEATERMULTI.getBlockInstance(), 1))
            .addElement('C', StructureUtility.ofBlock(ReactorBlocks.HEATERMULTI.getBlockInstance(), 2))
            .addElement('E', StructureUtility.ofBlock(ReactorBlocks.HEATERMULTI.getBlockInstance(), 3))
            .addElement('F', StructureUtility.ofBlock(ReactorBlocks.HEATERMULTI.getBlockInstance(), 4))
            .addElement(
                'M',
                StructureUtility
                    .ofBlock(ReactorTiles.MAGNETPIPE.getBlockInstance(), ReactorTiles.MAGNETPIPE.getBlockMetadata()))
            .addElement('P', StructureUtility.ofBlocksFlat(pipes, BlockRegistry.PIPING.getBlockInstance(), 1))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntityFusionHeater te, ExtendedFacing facing) {
        World world = te.getWorldObj();
        BlockPos insulation = DirectionUtil
            .offsetByForward(te.xCoord, te.yCoord, te.zCoord, ForgeDirection.NORTH, 1, 1);
        Block block = world.getBlock(insulation.x, insulation.y, insulation.z);
        int blockMeta = world.getBlockMetadata(insulation.x, insulation.y, insulation.z);

        if (block instanceof BlockHeaterMulti && (blockMeta == 1 || blockMeta == 9)) {
            Boolean ret = ((BlockHeaterMulti) block)
                .checkForFullMultiBlock(world, insulation.x, insulation.y, insulation.z, ForgeDirection.NORTH, null);
            if (ret != null && ret) {
                ((BlockHeaterMulti) block).onCreateFullMultiBlock(world, insulation.x, insulation.y, insulation.z, ret);
            }
        }
    }

    @Override
    protected String getControllerChar() {
        return "H";
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityFusionHeater te) {
        return new Vector3i(2, 4, -2);
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityFusionHeater te) {
        return ExtendedFacing.DEFAULT;
    }
}
