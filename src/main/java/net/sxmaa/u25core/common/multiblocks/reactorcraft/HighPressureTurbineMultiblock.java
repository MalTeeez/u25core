package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.mixins.late.blockrenderer6343.BlockTurbineMultiAccessor;
import net.sxmaa.u25core.util.DirectionUtil;

import org.joml.Vector3i;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Blocks.Multi.BlockTurbineMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.PowerGen.TileEntityHiPTurbine;
import blockrenderer6343.client.world.TrackedDummyWorld;

public class HighPressureTurbineMultiblock extends IExternalMultiblock<TileEntityHiPTurbine> {

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return new String[][] {
            {
                "           ",
                "           ",
                "           ",
                "           ",
                "    III    ",
                "    ILI    ",
                "    III    ",
                "           ",
                "           ",
                "           ",
                "           ",
            },
            {
                "           ",
                "           ",
                "           ",
                "    HHH    ",
                "   HHHHH   ",
                "   HHTHH   ",
                "   HHHHH   ",
                "    HHH    ",
                "           ",
                "           ",
                "           ",
            },
            {
                "           ",
                "           ",
                "           ",
                "   HHHHH   ",
                "   HHBHH   ",
                "   HBRBH   ",
                "   HHBHH   ",
                "   HHHHH   ",
                "           ",
                "           ",
                "           ",
            },
            {
                "           ",
                "           ",
                "    HHH    ",
                "   HHHHH   ",
                "  HHBBBHH  ",
                "  HHBRBHH  ",
                "  HHBBBHH  ",
                "   HHHHH   ",
                "    HHH    ",
                "           ",
                "           ",
            },
            {
                "           ",
                "           ",
                "   HHHHH   ",
                "  HHBBBHH  ",
                "  HBBBBBH  ",
                "  HBBRBBH  ",
                "  HBBBBBH  ",
                "  HHBBBHH  ",
                "   HHHHH   ",
                "           ",
                "           ",
            },
            {
                "           ",
                "    HHH    ",
                "  HHHHHHH  ",
                "  HHBBBHH  ",
                " HHBBBBBHH ",
                " HHBBRBBHH ",
                " HHBBBBBHH ",
                "  HHBBBHH  ",
                "  HHHHHHH  ",
                "    HHH    ",
                "           ",
            },
            {
                "           ",
                "   HHHHH   ",
                "  HHBBBHH  ",
                " HHBBBBBHH ",
                " HBBBBBBBH ",
                " HBBBRBBBH ",
                " HBBBBBBBH ",
                " HHBBBBBHH ",
                "  HHBBBHH  ",
                "   HHHHH   ",
                "           ",
            },
            {
                "   HHHHH   ",
                "  HHHBHHH  ",
                " HHBBBBBHH ",
                "HHBBBBBBBHH",
                "HHBBBBBBBHH",
                "HBBBBRBBBBH",
                "HHBBBBBBBHH",
                "HHBBBBBBBHH",
                " HHBBBBBHH ",
                "  HHHBHHH  ",
                "   HHHHH   ",
            },
        };
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntityHiPTurbine> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntityHiPTurbine>builder()
            .addShape("main", blueprint)
            .addElement(
                'R',
                StructureUtility
                    .ofBlock(ReactorTiles.BIGTURBINE.getBlockInstance(), ReactorTiles.BIGTURBINE.getBlockMetadata()))
            .addElement(
                'T',
                StructureUtility
                    .ofBlock(ReactorTiles.BIGTURBINE.getBlockInstance(), ReactorTiles.BIGTURBINE.getBlockMetadata()))
            .addElement(
                'L',
                StructureUtility
                    .ofBlock(ReactorTiles.STEAMLINE.getBlockInstance(), ReactorTiles.STEAMLINE.getBlockMetadata()))
            .addElement('B', StructureUtility.ofBlock(ReactorBlocks.TURBINEMULTI.getBlockInstance(), 0))
            .addElement('H', StructureUtility.ofBlock(ReactorBlocks.TURBINEMULTI.getBlockInstance(), 1))
            .addElement('I', StructureUtility.ofBlock(ReactorBlocks.TURBINEMULTI.getBlockInstance(), 2))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntityHiPTurbine te, ExtendedFacing facing) {
        World world = te.getWorldObj();
        // Does not render inside GUI
        if (world instanceof TrackedDummyWorld) {
            return;
        }
        ForgeDirection trueFacing = facing.getDirection();
        BlockPos pos = new BlockPos(te.xCoord, te.yCoord, te.zCoord);
        Block block = null;
        int blockMeta = -1;
        for (int i = 1; i < 7; i++) {
            // Set correct facing for all rotors (not the main (controller) one)
            pos = DirectionUtil.offsetByBack(te.xCoord, te.yCoord, te.zCoord, trueFacing, i, 0);
            TileEntity forwardTe = world.getTileEntity(pos.x, pos.y, pos.z);
            if (forwardTe instanceof TileEntityHiPTurbine) {
                ((TileEntityHiPTurbine) forwardTe).setBlockMetadata(te.getPseudoMeta());
                // Get blade above rotor
                pos.y = pos.y + 1;
                block = world.getBlock(pos.x, pos.y, pos.z);
                blockMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);
            } else {
                // Last piece, go back one
                pos = DirectionUtil.offsetByForward(te.xCoord, te.yCoord, te.zCoord, trueFacing, i - 1, 1);
                block = world.getBlock(pos.x, pos.y, pos.z);
                blockMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);
                break;
            }
        }
        if (block == null) {
            return;
        }

        if (block instanceof BlockTurbineMulti && (blockMeta == 0 || blockMeta == 8)) {
            Boolean ret = ((BlockTurbineMulti) block)
                .checkForFullMultiBlock(world, pos.x, pos.y, pos.z, trueFacing, null);
            if (ret != null && ret) {
                ((BlockTurbineMultiAccessor) block).invokeOnCreateFullMultiBlock(world, pos.x, pos.y, pos.z, ret);
            }
        }
    }

    @Override
    protected String getControllerChar() {
        return "T";
    }

    @Override
    protected Vector3i getControllerOffset(TileEntityHiPTurbine te, ExtendedFacing facing) {
        BlockPos pos = DirectionUtil.offsetByLeft(te.xCoord, te.yCoord, te.zCoord, facing.getDirection(), 5, -5, true);
        pos = DirectionUtil.offsetByForward(pos, facing.getDirection(), 1, 0);
        return new Vector3i(pos.x, pos.y, pos.z);
    }

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntityHiPTurbine te) {
        ForgeDirection dir = switch (te.getPseudoMeta()) {
            case 0 -> ForgeDirection.WEST;
            case 1 -> ForgeDirection.EAST;
            case 3 -> ForgeDirection.SOUTH;
            default -> ForgeDirection.NORTH;
        };
        return ExtendedFacing.of(DirectionUtil.turn180(dir));
    }
}
