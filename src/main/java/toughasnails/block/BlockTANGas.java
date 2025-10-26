package toughasnails.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import toughasnails.api.ITANBlock;
import toughasnails.item.ItemTANBlock;

public class BlockTANGas extends Block implements ITANBlock {

    public static enum GasType {
        BLACKDAMP,
        WHITEDAMP,
        FIREDAMP,
        STINKDAMP;

        public String getName() { return name().toLowerCase(); }
        @Override public String toString() { return getName(); }
    }

    public BlockTANGas() {
        super(Material.air);
        setHardness(0.0F);
        setBlockUnbreakable();
        setTickRandomly(true);
        setStepSound(soundTypeCloth);
        setBlockBounds(0F, 0F, 0F, 1F, 1F, 1F);
    }

    @Override public Class<? extends ItemBlock> getItemClass() { return ItemTANBlock.class; }
    @Override public String[] getPresetProperties() { return new String[]{"variant"}; }
    @Override public String[] getNonRenderingProperties() { return null; }
    @Override public String getStateName(int meta) { return GasType.values()[meta & 3].getName() + "_block"; }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }

    @Override
    public boolean isReplaceable(IBlockAccess world, int x, int y, int z) { return true; }

    @Override
    public boolean canDropFromExplosion(Explosion exp) { return false; }

    @Override
    public void dropBlockAsItemWithChance(World w,int x,int y,int z,int meta,float chance,int fortune){}

    @Override
    public void updateTick(World world,int x,int y,int z,Random rand){}

    @Override
    public int quantityDropped(Random rand){return 0;}

    // ✅ Correct Forge 1.7.10 method signatures
    @Override
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        GasType t = GasType.values()[world.getBlockMetadata(x, y, z) & 3];
        return (t == GasType.STINKDAMP || t == GasType.FIREDAMP) ? 2000 : 0;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        GasType t = GasType.values()[world.getBlockMetadata(x, y, z) & 3];
        return (t == GasType.STINKDAMP || t == GasType.FIREDAMP) ? 2000 : 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity e){
        int meta = world.getBlockMetadata(x,y,z);
        GasType type = GasType.values()[meta & 3];

        switch(type){
            case WHITEDAMP:
                if(e instanceof EntityLivingBase){
                    EntityLivingBase ent = (EntityLivingBase)e;
                    ent.addPotionEffect(new PotionEffect(Potion.weakness.id,500));
                    ent.addPotionEffect(new PotionEffect(Potion.hunger.id,500));
                    ent.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,500));
                    ent.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,500));
                }
                break;
            case STINKDAMP:
                if(!world.isRemote && e instanceof EntityArrow && ((EntityArrow)e).isBurning()){
                    explode(world,x,y,z,meta);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBlockDestroyedByExplosion(World world,int x,int y,int z,Explosion exp){
        int meta = world.getBlockMetadata(x,y,z);
        GasType type = GasType.values()[meta & 3];
        if(type==GasType.STINKDAMP && !world.isRemote)
            explode(world,x,y,z,meta);
    }

    private void explode(World world,int x,int y,int z,int meta){
        EntityFallingBlock dummy = new EntityFallingBlock(world,x+0.5,y,z+0.5,this,meta);
        world.spawnEntityInWorld(dummy);
        world.createExplosion(dummy,x+0.5,y,z+0.5,2.0F,true);
        world.setBlockToAir(x,y,z);
    }

    @Override
    public void randomDisplayTick(World world,int x,int y,int z,Random rand){
        if(rand.nextInt(12)==0){
            double px = x + 0.75 - rand.nextFloat()/2;
            double py = y + 0.9;
            double pz = z + 0.75 - rand.nextFloat()/2;
            world.spawnParticle("reddust", px, py, pz, 0, 0, 0);
        }
    }
}
