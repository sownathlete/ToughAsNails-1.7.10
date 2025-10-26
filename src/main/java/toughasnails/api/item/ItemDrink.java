package toughasnails.api.item;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.capability.IThirst;
import toughasnails.api.thirst.IDrink;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

/**
 * Backported version of Tough As Nails ItemDrink for Forge 1.7.10.
 * Removes ActionResult/EnumHand usage and restores legacy consumption behavior.
 */
public abstract class ItemDrink<T extends Enum<T>> extends Item {

    public ItemDrink() {
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    /**
     * Called when the player right-clicks with this item.
     * In 1.7.10, this returns an ItemStack directly.
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        ThirstHandler thirstHandler = (ThirstHandler) ThirstHelper.getThirstData(player);
        if (thirstHandler != null && thirstHandler.isThirsty()) {
            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        }
        return stack;
    }

    /**
     * Called when the drink is finished being consumed.
     */
    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer entity) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            IThirst thirst = ThirstHelper.getThirstData(player);

            // Determine type from metadata
            T type = this.getTypeFromMeta(stack.getItemDamage());

            // Apply thirst stats and hydration
            if (thirst != null && type instanceof IDrink) {
                thirst.addStats(((IDrink) type).getThirst(), ((IDrink) type).getHydration());
            }

            // Add effects such as poisoning/thirst
            this.addEffects(player, type);

            // Return empty glass bottle after drinking
            if (!player.capabilities.isCreativeMode) {
                return new ItemStack(Items.glass_bottle);
            }
        }
        return stack;
    }

    /**
     * Adds secondary effects like thirst debuff chance.
     */
    public void addEffects(EntityPlayer player, T type) {
        if (player.worldObj.rand.nextFloat() < ((IDrink) type).getPoisonChance()
                && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            player.addPotionEffect(new PotionEffect(TANPotions.thirst.id, 600, 0));
        }
    }

    /**
     * Return the enum drink type based on item damage.
     */
    public abstract T getTypeFromMeta(int meta);

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.drink;
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }
}
