package toughasnails.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import toughasnails.api.ITANBlock;
import toughasnails.api.item.TANItems;
import toughasnails.item.ItemTANBlock;

/**
 * Backported Rain Collector block for Forge 1.7.10.
 * Behaves like its modern equivalent but uses metadata (0-3) for water level.
 */
public class BlockRainCollector extends Block implements ITANBlock {

    public BlockRainCollector() {
        super(Material.iron);
        setHardness(2.0F);
        setStepSound(soundTypeMetal);
        setTickRandomly(true);
    }

    // -------- ITANBlock compatibility --------
    @Override
    public Class<? extends net.minecraft.item.ItemBlock> getItemClass() {
        return ItemTANBlock.class;
    }

    @Override
    public String[] getPresetProperties() {
        return new String[0];
    }

    @Override
    public String[] getNonRenderingProperties() {
        return null;
    }

    @Override
    public String getStateName(int meta) {
        return "level_" + meta;
    }

    // -------- Rendering --------
    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public boolean renderAsNormalBlock() { return false; }

    @Override
    public int getRenderType() { return 3; } // normal model render

    // -------- Interaction --------
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {

        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem == null) return true;

        int level = world.getBlockMetadata(x, y, z);
        Item item = heldItem.getItem();

        // --- Bucket ---
        if (item == Items.bucket) {
            if (level == 3 && !world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.stackSize--;
                    ItemStack waterBucket = new ItemStack(Items.water_bucket);
                    if (heldItem.stackSize <= 0) player.setCurrentItemOrArmor(0, waterBucket);
                    else if (!player.inventory.addItemStackToInventory(waterBucket))
                        player.dropPlayerItemWithRandomChoice(waterBucket, false);
                }
                setWaterLevel(world, x, y, z, 0);
            }
            return true;
        }

        // --- Glass bottle ---
        if (item == Items.glass_bottle) {
            if (level > 0 && !world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    ItemStack waterBottle = new ItemStack(Items.potionitem, 1, 0);
                    heldItem.stackSize--;
                    if (heldItem.stackSize <= 0) player.setCurrentItemOrArmor(0, waterBottle);
                    else if (!player.inventory.addItemStackToInventory(waterBottle))
                        player.dropPlayerItemWithRandomChoice(waterBottle, false);
                    else if (player instanceof EntityPlayerMP)
                        ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
                }
                setWaterLevel(world, x, y, z, level - 1);
            }
            return true;
        }

        // --- Canteen (TAN item) ---
        if (item == TANItems.canteen) {
            if (level > 0 && !world.isRemote) {
                if (!player.capabilities.isCreativeMode)
                    heldItem.setItemDamage(3); // filled canteen
                setWaterLevel(world, x, y, z, level - 1);
            }
            return true;
        }

        return false;
    }

    private void setWaterLevel(World world, int x, int y, int z, int level) {
        level = MathHelper.clamp_int(level, 0, 3);
        world.setBlockMetadataWithNotify(x, y, z, level, 2);
        world.func_147453_f(x, y, z, this); // update comparator
    }

    // -------- Rain filling --------
    @Override
    public void fillWithRain(World world, int x, int y, int z) {
        if (world.rand.nextInt(4) == 0) {
            BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
            float temp = biome.temperature;
            if (temp >= 0.15F) {
                int level = world.getBlockMetadata(x, y, z);
                if (level < 3)
                    setWaterLevel(world, x, y, z, level + 1);
            }
        }
    }

    // -------- Comparator output --------
    @Override
    public boolean hasComparatorInputOverride() { return true; }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }
}
