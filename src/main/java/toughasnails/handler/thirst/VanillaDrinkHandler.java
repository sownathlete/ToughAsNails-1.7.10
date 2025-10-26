package toughasnails.handler.thirst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import toughasnails.handler.PacketHandler;
import toughasnails.network.message.MessageUpdateStat;
import toughasnails.thirst.ThirstHandler;

public class VanillaDrinkHandler {

    @SubscribeEvent
    public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item;
        if (player == null || stack == null) return;
        if (player.worldObj.isRemote) return; // server only

        ThirstHandler thirst = ThirstHandler.getOrCreate(player);
        if (thirst == null) return;

        boolean zeroStack = false;
        if (stack.stackSize <= 0) { stack.stackSize = 1; zeroStack = true; }

        if (stack.getItem() == Items.milk_bucket) {
            thirst.addStats(6, 0.7F);
        } else if (stack.getItem() == Items.potionitem) {
            ItemPotion potion = (ItemPotion) stack.getItem();
            if (potion.getEffects(stack) == null || potion.getEffects(stack).isEmpty()) {
                thirst.addStats(7, 0.5F); // water bottle
            } else {
                thirst.addStats(4, 0.3F); // actual potions
            }
        }

        // Persist + sync now so HUD flips immediately
        ThirstHandler.save(player, thirst);
        if (player instanceof EntityPlayerMP) {
            NBTTagCompound tag = new NBTTagCompound();
            thirst.writeToNBT(tag);
            thirst.onSendClientUpdate();
            PacketHandler.instance.sendTo(new MessageUpdateStat("thirst", tag), (EntityPlayerMP) player);
        }

        if (zeroStack) stack.stackSize = 0;
    }
}
