package toughasnails.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import toughasnails.api.ITANBlock;
import toughasnails.api.TANBlocks;
import toughasnails.item.ItemTANBlock;

/**
 * Backport of TAN’s “new torch” to Forge 1.7.10.
 * Keeps age, burning state, rain-extinguish, and relight behavior.
 */
public class BlockTANTorchNew extends Block implements ITANBlock {

    // bits: 0–2 = facing, bit3 = burning, bits4–7 = age (0–15)
    public BlockTANTorchNew() {
        super(Material.circuits);
        setTickRandomly(true);
        setHardness(0F);
        setStepSound(soundTypeWood);
        setLightLevel(0.9375F);
    }

    // ---------- ITANBlock ----------
    @Override public Class<? extends ItemBlock> getItemClass() { return ItemTANBlock.class; }
    @Override public String[] getPresetProperties() { return new String[0]; }
    @Override public String[] getNonRenderingProperties() { return null; }
    @Override public String getStateName(int meta) { return "torch_new"; }

    // ---------- Placement ----------
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return canPlaceOn(world, x, y - 1, z)
            || world.isSideSolid(x - 1, y, z, net.minecraftforge.common.util.ForgeDirection.EAST)
            || world.isSideSolid(x + 1, y, z, net.minecraftforge.common.util.ForgeDirection.WEST)
            || world.isSideSolid(x, y, z - 1, net.minecraftforge.common.util.ForgeDirection.SOUTH)
            || world.isSideSolid(x, y, z + 1, net.minecraftforge.common.util.ForgeDirection.NORTH);
    }

    private boolean canPlaceOn(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block.isOpaqueCube() && block.renderAsNormalBlock();
    }

    // ---------- Ticking ----------
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean burning = (meta & 8) != 0;
        int age = (meta >> 4) & 15;

        if (burning) {
            if (world.isRaining() && world.canBlockSeeTheSky(x, y, z)) {
                world.setBlockMetadataWithNotify(x, y, z, (meta & 7) | (15 << 4), 3);
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5,
                        "random.fizz", 0.5F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
                return;
            }
            if (age < 15 && rand.nextInt(8) == 0) {
                world.setBlockMetadataWithNotify(x, y, z, (meta & 15) | ((age + 1) << 4) | 8, 3);
            }
            if (age == 15 && rand.nextInt(8) == 0) {
                world.setBlockMetadataWithNotify(x, y, z, (meta & 7), 3); // extinguish
            }
        }
    }

    // ---------- Lighting ----------
    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & 8) != 0 ? 14 : 0;
    }

    // ---------- Interaction ----------
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem();
        if (held == null) return false;

        Item item = held.getItem();
        int meta = world.getBlockMetadata(x, y, z);
        boolean burning = (meta & 8) != 0;
        int age = (meta >> 4) & 15;

        if (!burning && age == 0 && item == Items.flint_and_steel && !world.isRaining() && world.canBlockSeeTheSky(x, y, z) == false) {
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "fire.ignite", 1.0F,
                    world.rand.nextFloat() * 0.4F + 0.8F);
            world.setBlock(x, y, z, TANBlocks.torch_new, (meta & 7) | 8, 3);
            held.damageItem(1, player);
            return true;
        }
        return false;
    }

    // ---------- Rendering ----------
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return 2; } // same as vanilla torch

    // ---------- Particles ----------
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & 8) == 0) return;
        double px = x + 0.5, py = y + 0.7, pz = z + 0.5;
        world.spawnParticle("smoke", px, py, pz, 0, 0, 0);
        world.spawnParticle("flame", px, py, pz, 0, 0, 0);
    }

    // ---------- Metadata conversion (optional helpers) ----------
    @Override
    public int damageDropped(int meta) { return 0; }
}
