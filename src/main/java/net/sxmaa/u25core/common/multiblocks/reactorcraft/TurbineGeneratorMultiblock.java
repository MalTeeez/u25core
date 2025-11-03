package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import Reika.ReactorCraft.Blocks.Multi.BlockSolenoidMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.TileEntityReactorGenerator;
import blockrenderer6343.client.world.TrackedDummyWorld;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.util.DirectionUtil;
import org.joml.Vector3i;

public class TurbineGeneratorMultiblock extends IExternalMultiblock<TileEntityReactorGenerator> {
    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return new String[][] {
            { "HHHHH", "HBBBH", "HBCBH", "HBBBH", "HHHHH" },
            { "HHHHH", "HBBBH", "HBCBH", "HBBBH", "HHBHH" },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HWWWH", "HWCWH", "HWWWH", " HHH " },
            { " HHH ", "HHHHH", "HHGHH", "HHHHH", " HHH " },
        };
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityReactorGenerator> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityReactorGenerator>builder()
            .addShape("main", blueprint)
            .addElement('G', StructureUtility.ofBlock(ReactorTiles.GENERATOR.getBlockInstance(), ReactorTiles.GENERATOR.getBlockMetadata()))
            .addElement('C', StructureUtility.ofBlock(ReactorBlocks.GENERATORMULTI.getBlockInstance(), 0))
            .addElement('W', StructureUtility.ofBlock(ReactorBlocks.GENERATORMULTI.getBlockInstance(), 1))
            .addElement('H', StructureUtility.ofBlock(ReactorBlocks.GENERATORMULTI.getBlockInstance(), 2))
            .addElement('B', StructureUtility.ofBlock(ReactorBlocks.GENERATORMULTI.getBlockInstance(), 3))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntityReactorGenerator te, ExtendedFacing facing) {
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
        return "G";
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityReactorGenerator te, ExtendedFacing facing) {
        BlockPos pos = DirectionUtil.offsetByBack(te.xCoord, te.yCoord, te.zCoord, facing.getDirection(), 9, 2, true);
        pos = DirectionUtil.offsetByRight(pos, facing.getDirection(), 2, 0);
        return new Vector3i(pos.x, pos.y, pos.z);
    }

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityReactorGenerator te) {
        if (aSide.getDirection() == ForgeDirection.UP || aSide.getDirection() == ForgeDirection.DOWN) {
            aSide = aSide.with(ForgeDirection.NORTH);
        }
        ForgeDirection dir = DirectionUtil.turn180(aSide.getDirection());
        return ExtendedFacing.UP_NORMAL_VERTICAL.with(dir);
    }
}
