package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.util.DirectionUtil;

import org.joml.Vector3i;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Blocks.Multi.BlockInjectorMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.Fusion.TileEntityFusionInjector;

public class PlasmaInjectorMultiblock extends IExternalMultiblock<TileEntityFusionInjector> {

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return StructureUtility.transpose(new String[][] {
            { "LBL", "CHC", "CHC", "CHC", "UTU" },
            { "LBL", "SYS", "SYS", "SYS", "UTU" },
            { "LBL", "-I-", "SYS", "SYS", "UTU" },
            { "LBL", "SMS", "SYS", "SYS", "UTU" },
            { "LBL", "SMS", "SYS", "SYS", "UTU" },
            { "LBL", "SMS", "SYS", "UTU", "   " },
            { "LBL", "SMS", "SYS", "UTU", "   " },
            { "LBL", "SMS", "UTU", "   ", "   " },
            { "LBL", "CMC", "UTU", "   ", "   " },
        });
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityFusionInjector> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityFusionInjector>builder()
            .addShape("main", blueprint)
            .addElement('B', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 0))
            .addElement('L', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 1))
            .addElement('H', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 5))
            .addElement('S', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 2))
            .addElement('I', StructureUtility.ofBlock(ReactorTiles.INJECTOR.getBlock(), 7))
            .addElement('C', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 6))
            .addElement('Y', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 7))
            .addElement('U', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 4))
            .addElement('T', StructureUtility.ofBlock(ReactorBlocks.INJECTORMULTI.getBlockInstance(), 3))
            .addElement('M', StructureUtility.ofBlock(ReactorTiles.MAGNETPIPE.getBlock(), 1))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntityFusionInjector te, ExtendedFacing facing) {
        World world = te.getWorldObj();
        BlockPos coil = DirectionUtil.offsetByRight(te.xCoord, te.yCoord, te.zCoord, te.getFacing(), 2, 1);
        Block block = world.getBlock(coil.x, coil.y, coil.z);
        int blockMeta = world.getBlockMetadata(coil.x, coil.y, coil.z);

        if (block instanceof BlockInjectorMulti && blockMeta == 5) {
            Boolean ret = ((BlockInjectorMulti) block)
                .checkForFullMultiBlock(world, coil.x, coil.y, coil.z, DirectionUtil.turnLeft90(te.getFacing()), null);
            if (ret != null && ret) {
                ((BlockInjectorMulti) block).onCreateFullMultiBlock(world, coil.x, coil.y, coil.z, ret);
            }
        }
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityFusionInjector te, ExtendedFacing facing) {
        return switch (te.getFacing()) {
            case NORTH -> new Vector3i(2, -1, 1);
            case EAST -> new Vector3i(-1, -1, 2);
            case SOUTH -> new Vector3i(-2, -1, -1);
            default -> new Vector3i(1, -1, -2);
        };
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityFusionInjector te) {
        Rotation rot;
        switch (te.getFacing()) {
            case NORTH -> rot = Rotation.COUNTER_CLOCKWISE;
            case EAST -> rot = Rotation.UPSIDE_DOWN;
            case SOUTH -> rot = Rotation.CLOCKWISE;
            default -> rot = Rotation.NORMAL;
        }
        return ExtendedFacing.DOWN_NORMAL_NONE.with(rot);
    }

    @Override
    protected String getControllerChar() {
        return "I";
    }
}
