package toughasnails.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;

public class HealthHelper {

    public static final UUID STARTING_HEALTH_MODIFIER_ID = UUID.fromString("050F240E-868F-4164-A67E-374084DACA71");
    public static final UUID LIFEBLOOD_HEALTH_MODIFIER_ID = UUID.fromString("B04DB09D-ED8A-4B82-B1EF-ADB425174925");

    public static int getActiveHearts(EntityPlayer player) {
        return Math.min((int) (player.getMaxHealth() / 2.0F), 10);
    }

    public static int getInactiveHearts(EntityPlayer player) {
        return Math.max(10 - (int) (player.getMaxHealth() / 2.0F), 0);
    }

    public static int getLifebloodHearts(EntityPlayer player) {
        IAttributeInstance maxHealthInstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        AttributeModifier modifier = maxHealthInstance.getModifier(LIFEBLOOD_HEALTH_MODIFIER_ID);
        if (modifier != null) {
            return (int) (modifier.getAmount() / 2.0);
        }
        return 0;
    }

    public static boolean addActiveHearts(EntityPlayer player, int hearts) {
        IAttributeInstance maxHealthInstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        AttributeModifier existingModifier = maxHealthInstance.getModifier(LIFEBLOOD_HEALTH_MODIFIER_ID);

        double existingHearts = existingModifier != null ? existingModifier.getAmount() : 0.0;
        float newHealth = player.getMaxHealth() + (hearts * 2.0F);

        // Limit health to vanilla range
        if (newHealth <= 20.0F && newHealth > 0.0F) {
            // Remove previous modifier if present
            if (existingModifier != null) {
                maxHealthInstance.removeModifier(existingModifier);
            }

            AttributeModifier newModifier = new AttributeModifier(
                    LIFEBLOOD_HEALTH_MODIFIER_ID,
                    "Lifeblood Health Modifier",
                    existingHearts + (hearts * 2.0),
                    0
            );

            maxHealthInstance.applyModifier(newModifier);
            return true;
        }

        return false;
    }
}
