package net.sxmaa.u25core.common.multiblocks.reactorcraft;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import Reika.ReactorCraft.Blocks.Multi.BlockSolenoidMulti;
import Reika.ReactorCraft.Registry.ReactorBlocks;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.Fusion.TileEntitySolenoidMagnet;
import blockrenderer6343.client.world.TrackedDummyWorld;

public class SolenoidMultiblock extends IExternalMultiblock<TileEntitySolenoidMagnet> {

    @Override
    protected String[][] getStructureBlueprint() {
        // spotless:off
        return StructureUtility.transpose(new String[][] {
            {
                "     LBBBBBL     ",
                "   LL       LL   ",
                "  L           L  ",
                " L             L ",
                " L             L ",
                "L               L",
                "B               B",
                "B      HHH      B",
                "B      HHH      B",
                "B      HHH      B",
                "B               B",
                "L               L",
                " L             L ",
                " L             L ",
                "  L           L  ",
                "   LL       LL   ",
                "     LBBBBBL     ",
            },
            {
                "     AMMMMMA     ",
                "   AA   R   AA   ",
                "  A     R     A  ",
                " A R    R    R A ",
                " A  R   R   R  A ",
                "A    R  R  R    A",
                "M     R R R     M",
                "M      HHH      M",
                "MRRRRRRHCHRRRRRRM",
                "M      HHH      M",
                "M     R R R     M",
                "A    R  R  R    A",
                " A  R   R   R  A ",
                " A R    R    R A ",
                "  A     R     A  ",
                "   AA   R   AA   ",
                "     AMMMMMA     ",
            },
            {
                "     LBBBBBL     ",
                "   LL       LL   ",
                "  L           L  ",
                " L             L ",
                " L             L ",
                "L               L",
                "B               B",
                "B               B",
                "B               B",
                "B               B",
                "B               B",
                "L               L",
                " L             L ",
                " L             L ",
                "  L           L  ",
                "   LL       LL   ",
                "     LBBBBBL     ",
            }
        });
        // spotless:on
    }

    @Override
    protected IStructureDefinition<TileEntitySolenoidMagnet> getStructureDefinition(String[][] blueprint) {
        return StructureDefinition.<TileEntitySolenoidMagnet>builder()
            .addShape("main", blueprint)
            .addElement(
                'C',
                StructureUtility
                    .ofBlock(ReactorTiles.SOLENOID.getBlockInstance(), ReactorTiles.SOLENOID.getBlockMetadata()))
            .addElement('B', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 0))
            .addElement('L', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 1))
            .addElement('M', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 2))
            .addElement('A', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 3))
            .addElement('R', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 4))
            .addElement('H', StructureUtility.ofBlock(ReactorBlocks.SOLENOIDMULTI.getBlockInstance(), 5))
            .build();
    }

    @Override
    public void postConstructCheck(TileEntitySolenoidMagnet te, ExtendedFacing facing) {
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
        return "C";
    }

    @Override
    protected Vector3i getControllerOffset(TileEntitySolenoidMagnet te) {
        return new Vector3i(8, 1, -8);
    }

    @Override
    protected boolean getAllowHotswap() {
        return true;
    }

    @Override
    protected ExtendedFacing getDefaultStructureFacing(ExtendedFacing aSide, TileEntitySolenoidMagnet te) {
        return ExtendedFacing.DEFAULT;
    }
}
