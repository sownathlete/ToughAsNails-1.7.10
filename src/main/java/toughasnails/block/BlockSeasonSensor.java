package toughasnails.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.ITANBlock;
import toughasnails.api.TANBlocks;
import toughasnails.api.season.SeasonHelper;
import toughasnails.item.ItemTANBlock;
import toughasnails.tileentity.TileEntitySeasonSensor;

/**
 * Backported Season Sensor for Forge 1.7.10.
 * Uses metadata 0–15 to store redstone power level.
 */
public class BlockSeasonSensor extends BlockContainer implements ITANBlock {

    public final DetectorType type;

    public BlockSeasonSensor(DetectorType type) {
        super(Material.wood);
        this.type = type;
        setHardness(0.2F);
        setStepSound(soundTypeWood);
        setTickRandomly(true);
    }

    // ---------- ITANBlock ----------
    @Override
    public Class<? extends net.minecraft.item.ItemBlock> getItemClass() {
        return ItemTANBlock.class;
    }

    @Override
    public String[] getPresetProperties() { return new String[0]; }

    @Override
    public String[] getNonRenderingProperties() { return new String[] { "power" }; }

    @Override
    public String getStateName(int meta) { return type.getName(); }

    // ---------- Rendering ----------
    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public boolean renderAsNormalBlock() { return false; }

    @Override
    public int getRenderType() { return 3; } // standard model render

    // ---------- Tile entity ----------
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySeasonSensor();
    }

    // ---------- Redstone ----------
    @Override
    public boolean canProvidePower() { return true; }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }

    // ---------- Update power each tick ----------
    public void updatePower(World world, int x, int y, int z) {
        if (world.provider.dimensionId == 0) {
            int currentMeta = world.getBlockMetadata(x, y, z);
            int power = 0;

            int startTicks = this.type.ordinal() * 360000;
            int endTicks = (this.type.ordinal() + 1) * 360000;
            int currentTicks = SeasonHelper.getSeasonData(world).getSeasonCycleTicks();

            if (currentTicks >= startTicks && currentTicks <= endTicks) {
                float delta = (float) (currentTicks - startTicks) / 360000.0F;
                float peak = 2.0F * (-Math.abs(delta - 0.5F) + 0.5F);
                power = Math.min((int) (peak * 15.0F + 1.0F), 15);
            }

            if (currentMeta != power) {
                world.setBlockMetadataWithNotify(x, y, z, power, 2);
                world.func_147453_f(x, y, z, this); // update comparator/power
            }
        }
    }

    // ---------- Right-click toggles next season type ----------
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {
        if (!player.capabilities.isCreativeMode) return false;
        if (world.isRemote) return true;

        Block next = TANBlocks.season_sensors[(this.type.ordinal() + 1) % DetectorType.values().length];
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlock(x, y, z, next, meta, 2);
        ((BlockSeasonSensor) next).updatePower(world, x, y, z);
        return true;
    }

    // ---------- Drop ----------
    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return Item.getItemFromBlock(TANBlocks.season_sensors[0]);
    }

    // ---------- Bounding box ----------
    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.375F, 1.0F);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBoundsForItemRender();
    }

    // ---------- Enum ----------
    public static enum DetectorType {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;

        public String getName() {
            return name().toLowerCase();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
