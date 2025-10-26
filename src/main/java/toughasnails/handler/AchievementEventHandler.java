package toughasnails.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import toughasnails.api.achievement.TANAchievements;
import toughasnails.api.item.TANItems;

public class AchievementEventHandler {

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        EntityItem entityItem = event.pickedUp;
        if (entityItem == null || entityItem.getEntityItem() == null)
            return;

        ItemStack stack = entityItem.getEntityItem();
        Item item = stack.getItem();
        EntityPlayer player = event.player;

        if (item == TANItems.freeze_rod) {
            player.triggerAchievement(TANAchievements.into_ice);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Item item = event.crafting.getItem();
        EntityPlayer player = event.player;

        if (item == TANItems.canteen) {
            player.triggerAchievement(TANAchievements.thirst_quencher);
        }
        if (item == TANItems.fruit_juice) {
            player.triggerAchievement(TANAchievements.thirst_ender);
        }
        if (item == TANItems.thermometer) {
            player.triggerAchievement(TANAchievements.hot_or_cold);
        }
        if (item == TANItems.season_clock) {
            player.triggerAchievement(TANAchievements.that_time_of_year);
        }
    }

    // Correct 1.7.10 replacement for LivingEntityUseItemEvent.Finish
    @SubscribeEvent
    public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item;
        if (stack == null)
            return;

        Item item = stack.getItem();
        if (item == TANItems.lifeblood_crystal) {
            player.triggerAchievement(TANAchievements.life_or_death);
        }
    }

    @SubscribeEvent
    public void onBlockPlaced(net.minecraftforge.event.world.BlockEvent.PlaceEvent event) {
        ItemStack stack = event.itemInHand;
        if (stack == null)
            return;

        Block block = event.placedBlock;
        // You can add block-based achievements here if needed
    }
}
