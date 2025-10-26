package toughasnails.block;

import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import toughasnails.api.ITANBlock;
import toughasnails.core.ToughAsNails;
import toughasnails.item.ItemTANBlock;
import toughasnails.particle.TANParticleTypes;
import toughasnails.tileentity.TileEntityTemperatureSpread;

/**
 * Back-ported temperature coil (Forge 1.7.10).
 * Behaves like the modern heater / cooler but honours the 16-meta limit.
 */
public class BlockTANTemperatureCoil extends BlockContainer implements ITANBlock {

    /* ------------------------------------------------------------ */
    /*  Enum                                                         */
    /* ------------------------------------------------------------ */
    public static enum CoilType {
        COOLING, HEATING;
        public String getName() { return name().toLowerCase(); }
        @Override public String toString() { return getName(); }
    }

    /* ------------------------------------------------------------ */
    /*  Ctor / base properties                                       */
    /* ------------------------------------------------------------ */
    public BlockTANTemperatureCoil() {
        super(Material.iron);
        setHardness(1.0F);
        setStepSound(soundTypeMetal);
        setTickRandomly(true);
    }

    /* ------------------------------------------------------------ */
    /*  ITANBlock                                                    */
    /* ------------------------------------------------------------ */
    @Override public Class<? extends ItemBlock> getItemClass()      { return ItemTANBlock.class; }
    @Override public String[] getPresetProperties()                 { return new String[] { "variant" }; }
    @Override public String[] getNonRenderingProperties()           { return null; }
    @Override public String   getStateName(int meta)                { return getType(meta).getName() + "_coil"; }

    /* ------------------------------------------------------------ */
    /*  Rendering flags                                              */
    /* ------------------------------------------------------------ */
    @Override public boolean isOpaqueCube()          { return false; }
    @Override public boolean renderAsNormalBlock()   { return false; }
    @Override public int     getRenderType()         { return 3; }      // model

    /* ------------------------------------------------------------ */
    /*  Meta helpers                                                 */
    /* ------------------------------------------------------------ */
    private static CoilType getType(int meta)     { return (meta & 1) == 1 ? CoilType.HEATING : CoilType.COOLING; }
    private static boolean  isPowered(int meta)   { return (meta & 8) != 0; }
    private static int      packMeta(CoilType t, boolean p) { return t.ordinal() | (p ? 8 : 0); }

    /* ------------------------------------------------------------ */
    /*  Red-stone updates                                            */
    /* ------------------------------------------------------------ */
    @Override
    public void onNeighborBlockChange(World w, int x, int y, int z, Block nb) {
        updatePowered(w, x, y, z);
    }

    @Override
    public void updateTick(World w, int x, int y, int z, Random r) {
        updatePowered(w, x, y, z);
    }

    private void updatePowered(World w, int x, int y, int z) {
        int  meta        = w.getBlockMetadata(x, y, z);
        boolean nowPow   = w.isBlockIndirectlyGettingPowered(x, y, z)
                        || w.isBlockIndirectlyGettingPowered(x, y + 1, z);
        boolean wasPow   = isPowered(meta);

        if (w.isRemote || nowPow == wasPow) return;

        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof TileEntityTemperatureSpread) {
            TileEntityTemperatureSpread spread = (TileEntityTemperatureSpread) te;
            invoke(spread, nowPow ? "fill" : "reset");
        }
        w.setBlockMetadataWithNotify(x, y, z, packMeta(getType(meta), nowPow), 3);
    }

    /* call private fill() / reset() safely */
    private static void invoke(TileEntityTemperatureSpread te, String name) {
        try {
            Method m = TileEntityTemperatureSpread.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(te);
        } catch (ReflectiveOperationException ignored) { /* never fatal – just skip */ }
    }

    /* ------------------------------------------------------------ */
    /*  Tile-entity factory                                          */
    /* ------------------------------------------------------------ */
    @Override
    public TileEntity createNewTileEntity(World w, int meta) {
        switch (getType(meta)) {
            case COOLING: return new TileEntityTemperatureSpread(-10);
            case HEATING: return new TileEntityTemperatureSpread( 10);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    /*  Particles                                                    */
    /* ------------------------------------------------------------ */
    @Override
    public void randomDisplayTick(World w, int x, int y, int z, Random r) {
        int meta = w.getBlockMetadata(x, y, z);
        if (!isPowered(meta)) return;

        double px = x + 0.4 + r.nextFloat() * 0.2;
        double py = y + 0.7 + r.nextFloat() * 0.3;
        double pz = z + 0.4 + r.nextFloat() * 0.2;

        if (getType(meta) == CoilType.HEATING)
            w.spawnParticle("smoke", px, py, pz, 0, 0, 0);
        else
            ToughAsNails.proxy.spawnParticle(TANParticleTypes.SNOWFLAKE, px, py, pz, 0, 0, 0, new int[0]);
    }

    /* ------------------------------------------------------------ */
    /*  Misc overrides                                               */
    /* ------------------------------------------------------------ */
    @Override public int damageDropped(int m)            { return getType(m).ordinal(); }
    @Override public int getLightValue(IBlockAccess w,int x,int y,int z){
        return isPowered(w.getBlockMetadata(x,y,z)) ? 7 : 0;
    }
}
