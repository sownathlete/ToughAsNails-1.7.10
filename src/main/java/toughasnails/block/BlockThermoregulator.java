package toughasnails.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import toughasnails.tileentity.TileEntityThermoregulator;

public class BlockThermoregulator extends BlockContainer {

    public BlockThermoregulator() {
        super(Material.iron);
        this.setHardness(3.0F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntityThermoregulator createNewTileEntity(World world, int meta) {
        return new TileEntityThermoregulator();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        if (world.isRemote) return true;

        TileEntityThermoregulator te = (TileEntityThermoregulator) world.getTileEntity(x, y, z);
        if (te == null) return true;

        if (player.isSneaking()) {
            boolean disabled = te.toggleRedstoneOverride();
            player.addChatComponentMessage(new ChatComponentText("Thermoregulator override: " + (disabled ? "DISABLED" : "ENABLED")));
            return true;
        }

        // TODO: open your GUI here once you wire a GuiHandler
        // player.openGui(ToughAsNails.instance, GUI_ID_THERMOREGULATOR, world, x, y, z);

        player.addChatComponentMessage(new ChatComponentText("Thermoregulator: Cooling=" + te.getCoolingTicksRemaining()
                + ", Heating=" + te.getHeatingTicksRemaining() + ", Mode=" + te.getModeString()));
        return true;
    }

    @Override
    public int quantityDropped(Random rand) {
        return 1;
    }

    @Override
    public boolean isOpaqueCube() { return false; }
    @Override
    public boolean renderAsNormalBlock() { return true; }
}
