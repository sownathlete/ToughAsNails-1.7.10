package toughasnails.handler.health;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import toughasnails.api.HealthHelper;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;

/**
 * Tough As Nails MaxHealthHandler for Forge 1.7.10.
 * Works whether world.difficultySetting is int or EnumDifficulty.
 */
public class MaxHealthHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        World world = player.worldObj;
        if (world.isRemote) return;

        int difficultyId = 0;

        // Handle both EnumDifficulty and int versions
        try {
            Object diffObj = world.difficultySetting;
            if (diffObj instanceof EnumDifficulty) {
                difficultyId = ((EnumDifficulty) diffObj).getDifficultyId();
            } else if (diffObj instanceof Integer) {
                difficultyId = (Integer) diffObj;
            }
        } catch (Throwable t) {
            // fallback if field behaves differently
            difficultyId = 0;
        }

        updateStartingHealthModifier(difficultyId, player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // 1.7.10: use event.original and event.entityPlayer
        EntityPlayer oldPlayer = event.original;
        EntityPlayer newPlayer = event.entityPlayer;

        IAttributeInstance oldMax = oldPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        AttributeModifier lifeblood = oldMax.getModifier(HealthHelper.LIFEBLOOD_HEALTH_MODIFIER_ID);

        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_LOWERED_STARTING_HEALTH) && lifeblood != null) {
            IAttributeInstance newMax = newPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            if (newMax.getModifier(HealthHelper.LIFEBLOOD_HEALTH_MODIFIER_ID) == null) {
                newMax.applyModifier(lifeblood);
            }
        }
    }

    /** Applies starting health modifier based on numeric difficulty (0–3). */
    private void updateStartingHealthModifier(int difficultyId, EntityPlayer player) {
        IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        AttributeModifier current = maxHealth.getModifier(HealthHelper.STARTING_HEALTH_MODIFIER_ID);

        // If disabled, remove any modifier and exit
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_LOWERED_STARTING_HEALTH)) {
            if (current != null) {
                maxHealth.removeModifier(current);
            }
            return;
        }

        // Map difficultyId to decrement
        double diffDecrement;
        switch (difficultyId) {
            case 1: diffDecrement = -6.0;  break; // Easy
            case 2: diffDecrement = -10.0; break; // Normal
            case 3: diffDecrement = -14.0; break; // Hard
            default: diffDecrement = 0.0;  break; // Peaceful / fallback
        }

        // Offset by Lifeblood hearts (each = 2 health)
        double lifebloodHealth = HealthHelper.getLifebloodHearts(player) * 2.0;
        double total = diffDecrement + lifebloodHealth;
        if (total > 0.0) diffDecrement -= total;

        // Only reapply if changed
        if (current == null || current.getAmount() != diffDecrement) {
            if (current != null) maxHealth.removeModifier(current);

            AttributeModifier startMod = new AttributeModifier(
                HealthHelper.STARTING_HEALTH_MODIFIER_ID,
                "Starting Health Modifier",
                diffDecrement,
                0 // operation 0 = add number
            );
            maxHealth.applyModifier(startMod);

            // Clamp health to new max
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }
}
