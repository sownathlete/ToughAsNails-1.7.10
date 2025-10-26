package toughasnails.handler.thirst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

/**
 * Handles vanilla drink completions (milk & potions) to restore thirst in Forge 1.7.10.
 */
public class VanillaDrinkHandler {

    @SubscribeEvent
    public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item;
        if (player == null || stack == null) return;

        // Get TAN thirst data (backs your capability backport)
        ThirstHandler thirst = (ThirstHandler) ThirstHelper.getThirstData(player);
        if (thirst == null || !thirst.isThirsty()) return;

        boolean zeroStack = false;
        if (stack.stackSize <= 0) {
            stack.stackSize = 1; // temporarily prevent NPEs in item logic
            zeroStack = true;
        }

        // Milk bucket = bigger thirst restore
        if (stack.getItem() == Items.milk_bucket) {
            thirst.addStats(6, 0.7F);
        }
        // Vanilla potion bottle
        else if (stack.getItem() == Items.potionitem) {
            ItemPotion potion = (ItemPotion) stack.getItem();

            // Plain water bottle has no effects
            if (potion.getEffects(stack) == null || potion.getEffects(stack).isEmpty()) {
                thirst.addStats(7, 0.5F);
            } else {
                // Actual potion (has effects)
                thirst.addStats(4, 0.3F);
            }
        }

        if (zeroStack) {
            stack.stackSize = 0; // restore original zeroed state
        }
    }
}
