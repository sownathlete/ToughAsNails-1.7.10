/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.temperature.modifier.TemperatureModifier;

public class ArmorModifier
extends TemperatureModifier {
    public static final int ARMOR_RATE_MODIFIER = 25;
    public static final int JELLED_SLIME_TARGET_MODIFIER = -1;
    public static final int WOOL_TARGET_MODIFIER = 1;

    public ArmorModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int newChangeRate = changeRate;
        int armorRateModifier = 25;
        switch (trend) {
            case INCREASING: {
                armorRateModifier *= -1;
                break;
            }
            case STILL: {
                armorRateModifier = 0;
            }
        }
        this.debugger.start(TemperatureDebugger.Modifier.ARMOR_RATE, newChangeRate);
        InventoryPlayer inventory = player.inventory;
        if (inventory.armorInventory[3] != null) {
            newChangeRate += armorRateModifier;
        }
        if (inventory.armorInventory[2] != null) {
            newChangeRate += armorRateModifier;
        }
        if (inventory.armorInventory[1] != null) {
            newChangeRate += armorRateModifier;
        }
        if (inventory.armorInventory[0] != null) {
            newChangeRate += armorRateModifier;
        }
        this.debugger.end(newChangeRate);
        return newChangeRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int temperatureLevel;
        int newTemperatureLevel = temperatureLevel = temperature.getRawValue();
        this.debugger.start(TemperatureDebugger.Modifier.ARMOR_TARGET, newTemperatureLevel);
        InventoryPlayer inventory = player.inventory;
        if (inventory.armorInventory[3] != null) {
            if (inventory.armorInventory[3].getItem() == TANItems.wool_helmet) {
                ++newTemperatureLevel;
            }
            if (inventory.armorInventory[3].getItem() == TANItems.jelled_slime_helmet) {
                --newTemperatureLevel;
            }
        }
        if (inventory.armorInventory[2] != null) {
            if (inventory.armorInventory[2].getItem() == TANItems.wool_chestplate) {
                ++newTemperatureLevel;
            }
            if (inventory.armorInventory[2].getItem() == TANItems.jelled_slime_chestplate) {
                --newTemperatureLevel;
            }
        }
        if (inventory.armorInventory[1] != null) {
            if (inventory.armorInventory[1].getItem() == TANItems.wool_leggings) {
                ++newTemperatureLevel;
            }
            if (inventory.armorInventory[1].getItem() == TANItems.jelled_slime_leggings) {
                --newTemperatureLevel;
            }
        }
        if (inventory.armorInventory[0] != null) {
            if (inventory.armorInventory[0].getItem() == TANItems.wool_boots) {
                ++newTemperatureLevel;
            }
            if (inventory.armorInventory[0].getItem() == TANItems.jelled_slime_boots) {
                --newTemperatureLevel;
            }
        }
        this.debugger.end(newTemperatureLevel);
        return new Temperature(newTemperatureLevel);
    }

}

