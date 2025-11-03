package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import Reika.ReactorCraft.Blocks.Multi.BlockSolenoidMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.TileEntityReactorFlywheel;
import blockrenderer6343.client.world.TrackedDummyWorld;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.util.DirectionUtil;
import org.joml.Vector3i;

public class TurbineFlywheelMultiblock extends IExternalMultiblock<TileEntityReactorFlywheel> {
    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return new String[][] {
            {
                " FFF ",
                "FDCDF",
                "FCWCF",
                "FDCDF",
                " FFF ",
            },
        };
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityReactorFlywheel> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityReactorFlywheel>builder()
            .addShape("main", blueprint)
            .addElement('W', StructureUtility.ofBlock(ReactorTiles.FLYWHEEL.getBlockInstance(), ReactorTiles.FLYWHEEL.getBlockMetadata()))
            .addElement('C', StructureUtility.ofBlock(ReactorBlocks.FLYWHEELMULTI.getBlockInstance(), 0))
            .addElement('D', StructureUtility.ofBlock(ReactorBlocks.FLYWHEELMULTI.getBlockInstance(), 1))
            .addElement('F', StructureUtility.ofBlock(ReactorBlocks.FLYWHEELMULTI.getBlockInstance(), 2))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntityReactorFlywheel te, ExtendedFacing facing) {
        World world = te.getWorldObj();
        // Does not render inside GUI
        if (world instanceof TrackedDummyWorld) {
            return;
        }
        Block block = world.getBlock(te.xCoord, te.yCoord + 1, te.zCoord);
        int blockMeta = world.getBlockMetadata(te.xCoord, te.yCoord + 1, te.zCoord);

        if (block instanceof BlockSolenoidMulti && (blockMeta == 5 || blockMeta == 13)) {
            Boolean ret = ((BlockSolenoidMulti) block)
                .checkForFullMultiBlock(world, te.xCoord, te.yCoord + 1, te.zCoord, ForgeDirection.NORTH, null);
            if (ret != null && ret) {
                ((BlockSolenoidMulti) block).onCreateFullMultiBlock(world, te.xCoord, te.yCoord + 1, te.zCoord, ret);
            }
        }
    }

    @Override
    protected String getControllerChar() {
        return "W";
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityReactorFlywheel te, ExtendedFacing facing) {
        BlockPos pos = DirectionUtil.offsetByLeft(te.xCoord, te.yCoord, te.zCoord, facing.getDirection(), 2, -2, true);
        return new Vector3i(pos.x, pos.y, pos.z);
    }

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityReactorFlywheel te) {
        return ExtendedFacing.of(te.getFacing());
    }
}
