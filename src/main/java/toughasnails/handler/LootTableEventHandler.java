package toughasnails.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.event.world.WorldEvent;
import toughasnails.api.item.TANItems;

/**
 * Backported LootTableEventHandler for Forge 1.7.10.
 * Replaces LootTables with ChestGenHooks-based loot injection.
 */
public class LootTableEventHandler {

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        // Desert pyramid chest — substitute for Nether fortress rarity
        ChestGenHooks.addItem(
                ChestGenHooks.PYRAMID_DESERT_CHEST,
                new WeightedRandomChestContent(new ItemStack(TANItems.lifeblood_crystal), 1, 1, 1)
        );

        // Bonus chest (spawn chest)
        ChestGenHooks.addItem(
                ChestGenHooks.BONUS_CHEST,
                new WeightedRandomChestContent(new ItemStack(TANItems.canteen), 1, 1, 10)
        );

        // Use dungeon chests to simulate igloo-style cold loot
        ChestGenHooks.addItem(
                ChestGenHooks.DUNGEON_CHEST,
                new WeightedRandomChestContent(new ItemStack(TANItems.ice_cube), 1, 3, 8)
        );

        ChestGenHooks.addItem(
                ChestGenHooks.DUNGEON_CHEST,
                new WeightedRandomChestContent(new ItemStack(TANItems.freeze_powder), 1, 2, 4)
        );

        ChestGenHooks.addItem(
                ChestGenHooks.DUNGEON_CHEST,
                new WeightedRandomChestContent(new ItemStack(TANItems.freeze_rod), 1, 1, 2)
        );
    }
}
