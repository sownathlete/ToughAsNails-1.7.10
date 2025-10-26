package toughasnails.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import toughasnails.api.ITANBlock;
import toughasnails.api.TANBlocks;
import toughasnails.api.achievement.TANAchievements;
import toughasnails.item.ItemTANBlock;

/**
 * Campfire block for Forge 1.7.10.
 * Metadata layout:
 *   bits 0-2 = age (0–7)
 *   bit 3 (8) = burning flag
 */
public class BlockTANCampfire extends Block implements ITANBlock {

    public BlockTANCampfire() {
        super(Material.rock);
        setTickRandomly(true);
        setHardness(0.7F);
        setStepSound(soundTypeStone);
    }

    // ---------- ITANBlock ----------
    @Override
    public Class<? extends net.minecraft.item.ItemBlock> getItemClass() { return ItemTANBlock.class; }
    @Override
    public String[] getPresetProperties() { return new String[0]; }
    @Override
    public String[] getNonRenderingProperties() { return null; }
    @Override
    public String getStateName(int meta) { return isBurning(meta) ? "burning" : "idle"; }

    // ---------- Rendering ----------
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return 3; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return null;}
    @Override public void setBlockBoundsForItemRender(){setBlockBounds(0.1F,0F,0.1F,0.9F,0.6F,0.9F);}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){setBlockBoundsForItemRender();}

    // ---------- Light ----------
    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return isBurning(world.getBlockMetadata(x, y, z)) ? 15 : 0;
    }

    // ---------- Updates ----------
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        int age = getAge(meta);
        boolean burning = isBurning(meta);

        if (burning) {
            // rain extinguish
            if (world.isRaining() && world.canBlockSeeTheSky(x, y + 1, z)) {
                world.setBlockMetadataWithNotify(x, y, z, setBurning(setAge(meta, 7), false), 2);
                for (int i = 0; i < 8; ++i)
                    world.spawnParticle("smoke", x + 0.75 - rand.nextFloat()/2,
                        y + 0.9, z + 0.75 - rand.nextFloat()/2, 0,0,0);
                world.playSoundEffect(x+0.5, y+0.5, z+0.5,
                    "random.fizz", 0.5F, 2.6F+(rand.nextFloat()-rand.nextFloat())*0.8F);
                return;
            }
            // age up / burn out
            if (age < 7 && rand.nextInt(8)==0)
                world.setBlockMetadataWithNotify(x, y, z, setAge(meta, age+1), 2);
            if (age == 7 && rand.nextInt(8)==0)
                world.setBlockMetadataWithNotify(x, y, z, setBurning(meta, false), 2);
        }
    }

    // ---------- Collision burn ----------
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity e) {
        int meta = world.getBlockMetadata(x, y, z);
        if (isBurning(meta) && e instanceof EntityLivingBase) e.setFire(1);
    }

    // ---------- Activation ----------
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) return false;

        int meta = world.getBlockMetadata(x, y, z);
        int age = getAge(meta);
        boolean burning = isBurning(meta);

        // can ignite only if unlit, fresh, not raining
        if (age == 0 && !burning &&
            !(world.isRaining() && world.canBlockSeeTheSky(x, y + 1, z))) {
            Item item = stack.getItem();

            // stick ignition (chance)
            if (item == Items.stick) {
                if (!world.isRemote && world.rand.nextInt(12)==0) {
                    world.setBlock(x, y, z, TANBlocks.campfire, setBurning(0,true), 2);
                    player.triggerAchievement(TANAchievements.campfire_song);
                }
                if (!player.capabilities.isCreativeMode) stack.stackSize--;
                return true;
            }

            // flint & steel ignition
            if (item == Items.flint_and_steel) {
                world.playSoundEffect(x+0.5, y+0.5, z+0.5,
                        "fire.ignite", 1.0F, world.rand.nextFloat()*0.4F+0.8F);
                if (!world.isRemote) {
                    world.setBlock(x, y, z, TANBlocks.campfire, setBurning(0,true), 2);
                    player.triggerAchievement(TANAchievements.campfire_song);
                }
                if (!player.capabilities.isCreativeMode)
                    stack.damageItem(1, player);
                return true;
            }
        }
        return false;
    }

    // ---------- Particles ----------
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        if (!isBurning(meta)) return;
        if (rand.nextInt(24)==0)
            world.playSound(x+0.5, y+0.5, z+0.5, "fire.fire",
                    1.0F+rand.nextFloat(), rand.nextFloat()*0.7F+0.3F, false);
        for (int i=0;i<3;i++)
            world.spawnParticle("flame",
                x+0.75-rand.nextFloat()/2, y+0.25+rand.nextFloat()/2,
                z+0.75-rand.nextFloat()/2, 0,0,0);
        world.spawnParticle("smoke",
            x+0.75-rand.nextFloat()/2, y+0.9,
            z+0.75-rand.nextFloat()/2, 0,0,0);
        if (rand.nextInt(2)==0)
            world.spawnParticle("largesmoke",
                x+0.75-rand.nextFloat()/2, y+0.9,
                z+0.75-rand.nextFloat()/2, 0,0,0);
    }

    // ---------- Placement ----------
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        Block below = world.getBlock(x, y - 1, z);
        return below.isSideSolid(world, x, y - 1, z, net.minecraftforge.common.util.ForgeDirection.UP)
            && world.isAirBlock(x, y, z);
    }

    @Override
    public int quantityDropped(Random rand) { return 0; }

    // ---------- Metadata helpers ----------
    private static boolean isBurning(int meta){ return (meta & 8)!=0; }
    private static int setBurning(int meta, boolean flag){
        if(flag) return meta|8; else return meta&~8;
    }
    private static int getAge(int meta){ return meta & 7; }
    private static int setAge(int meta,int age){ return (meta&8)|(age&7); }
}
