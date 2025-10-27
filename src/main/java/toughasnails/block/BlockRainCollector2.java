package toughasnails.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;
import toughasnails.tileentity.TileEntityRainCollector2;

public class BlockRainCollector2 extends BlockContainer {
    public BlockRainCollector2() {
        super(Material.wood);
        setHardness(1.0F);
        setStepSound(soundTypeWood);
    }

    @Override
    public TileEntityRainCollector2 createNewTileEntity(World world, int meta) {
        return new TileEntityRainCollector2();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntityRainCollector2 te = (TileEntityRainCollector2) world.getTileEntity(x, y, z);
        if (te == null) return true;

        ItemStack held = player.getHeldItem();

        // 1) Fill a vanilla glass bottle into a WATER BOTTLE (purified)
        if (held != null && held.getItem() == Items.glass_bottle && te.getAmount() >= TileEntityRainCollector2.BOTTLE_COST) {
            if (!player.capabilities.isCreativeMode) {
                held.stackSize--;
                if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            ItemStack out = (TANItems.water_bottle != null)
                    ? new ItemStack(TANItems.water_bottle, 1, TileEntityRainCollector2.META_PURIFIED)
                    : new ItemStack(Items.potionitem, 1, 0); // vanilla water bottle
            if (!player.inventory.addItemStackToInventory(out)) player.dropPlayerItemWithRandomChoice(out, false);
            te.drain(TileEntityRainCollector2.BOTTLE_COST);
            return true;
        }

        // 2) Fill a TAN canteen (NBT-based simple level system if you don't have methods yet)
        if (held != null && TANItems.canteen != null && held.getItem() == TANItems.canteen && te.getAmount() >= 1000) {
            // simple NBT scheme: level [0..CANTEEN_MAX], Purified: true/false
            net.minecraft.nbt.NBTTagCompound tag = (held.hasTagCompound() ? held.getTagCompound() : new net.minecraft.nbt.NBTTagCompound());
            int level = tag.getInteger("level");
            final int MAX = TileEntityRainCollector2.CANTEEN_MAX;
            if (level < MAX) {
                int needed = (MAX - level);
                int fillUnits = Math.min(needed, te.getAmount() / 1000);
                if (fillUnits > 0) {
                    tag.setInteger("level", level + fillUnits);
                    tag.setBoolean("Purified", true);
                    held.setTagCompound(tag);
                    te.drain(fillUnits * 1000);
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("Filled canteen: " + (level+fillUnits) + "/" + MAX));
                    return true;
                }
            }
        }

        player.addChatMessage(new net.minecraft.util.ChatComponentText("Purified water: " + te.getAmount() + " mB"));
        return true;
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return true; }
}
