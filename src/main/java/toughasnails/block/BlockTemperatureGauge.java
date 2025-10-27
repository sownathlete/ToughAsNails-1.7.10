// File: toughasnails/block/BlockTemperatureGauge.java
package toughasnails.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import toughasnails.tileentity.TileEntityTemperatureGauge;

import java.util.List;

public class BlockTemperatureGauge extends BlockContainer {

    // meta bit 0 -> 0=cold, 1=warm
    private static final int META_WARM_BIT = 0x1;

    @SideOnly(Side.CLIENT) private IIcon topColdIcon;
    @SideOnly(Side.CLIENT) private IIcon topWarmIcon;
    @SideOnly(Side.CLIENT) private IIcon sideIcon;

    public BlockTemperatureGauge() {
        super(Material.rock);
        setHardness(0.2F);
        setStepSound(soundTypeStone);
        setLightOpacity(0);
        setBlockBounds(0F, 0F, 0F, 1F, 0.375F, 1F);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1.0D, y + 0.375D, z + 1.0D);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTemperatureGauge();
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        topColdIcon = reg.registerIcon("toughasnails:temperature_gauge_top");
        topWarmIcon = reg.registerIcon("toughasnails:temperature_gauge_inverted_top");
        sideIcon    = reg.registerIcon("toughasnails:temperature_gauge_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) {
            boolean warm = (meta & META_WARM_BIT) != 0;
            return warm ? topWarmIcon : topColdIcon;
        }
        return sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        if (side == 1) {
            int meta = access.getBlockMetadata(x, y, z);
            boolean warm = (meta & META_WARM_BIT) != 0;
            return warm ? topWarmIcon : topColdIcon;
        }
        return sideIcon;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z) & META_WARM_BIT;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override public boolean canProvidePower() { return true; }

    @Override
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side) {
        TileEntity te = access.getTileEntity(x, y, z);
        return (te instanceof TileEntityTemperatureGauge)
                ? ((TileEntityTemperatureGauge) te).getRedstoneLevelClient()
                : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side) {
        return isProvidingWeakPower(access, x, y, z, side);
    }

    /** Right-click toggles warm/cold and explicitly clears any prior output so it never latches. */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        int meta = world.getBlockMetadata(x, y, z);
        boolean warm = (meta & META_WARM_BIT) != 0;
        int newMeta = (meta & ~META_WARM_BIT) | (!warm ? META_WARM_BIT : 0);
        world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityTemperatureGauge) {
            ((TileEntityTemperatureGauge) te).onModeChanged(); // clear output immediately
        }

        world.markBlockForUpdate(x, y, z);
        world.notifyBlocksOfNeighborChange(x, y, z, this);

        player.addChatMessage(new ChatComponentText(
                "Gauge mode: " + (((newMeta & META_WARM_BIT) != 0) ? "WARM (hot detector)" : "COLD (cold detector)")));
        return true;
    }

    @Override public int damageDropped(int meta) { return (meta & META_WARM_BIT); }

    // If you want two creative variants, uncomment:
    /*
    @Override
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));              // cold
        list.add(new ItemStack(item, 1, META_WARM_BIT));  // warm
    }
    */
}
