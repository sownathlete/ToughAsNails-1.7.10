package toughasnails.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.tileentity.TileEntityWaterPurifier;

public class BlockWaterPurifier extends BlockContainer {

    public BlockWaterPurifier() {
        super(Material.iron);
        setHardness(2.0F);
        setResistance(8.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntityWaterPurifier createNewTileEntity(World world, int meta) {
        return new TileEntityWaterPurifier();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntityWaterPurifier te = (TileEntityWaterPurifier) world.getTileEntity(x, y, z);
        if (te != null) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("Purifier: " + te.debugString()));
        }
        return true;
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return true; }
}
