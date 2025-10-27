// File: toughasnails/temperature/modifier/ArmorModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Stronger armor influence so full sets are meaningful:
 *  - Wool:   +2 per piece (warmer)   -> up to +8
 *  - Slime:  -2 per piece (cooler)   -> up to -8
 *  - Leaf:   treated like slime      -> up to -8
 * Final delta clamped to [-6, +6] to avoid total dominance, but very noticeable.
 */
public class ArmorModifier extends TemperatureModifier {

    public static final int ARMOR_RATE_MODIFIER        = 25; // unchanged
    public static final int WARM_PIECE_TARGET_MODIFIER = +2; // wool per piece
    public static final int COOL_PIECE_TARGET_MODIFIER = -2; // slime/leaf per piece
    public static final int TARGET_CLAMP               = 6;  // final clamp [-6..+6]

    public ArmorModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int out = changeRate;
        int perPiece = ARMOR_RATE_MODIFIER;

        if (trend == TemperatureTrend.INCREASING) perPiece *= -1;
        else if (trend == TemperatureTrend.STILL) perPiece = 0;

        InventoryPlayer inv = player.inventory;
        debugger.start(TemperatureDebugger.Modifier.ARMOR_RATE, out);

        if (inv != null && inv.armorInventory != null) {
            for (ItemStack st : inv.armorInventory) {
                if (st != null) out += perPiece;
            }
        }

        debugger.end(out);
        return out;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();
        int delta = 0;

        InventoryPlayer inv = player.inventory;
        debugger.start(TemperatureDebugger.Modifier.ARMOR_TARGET, out);

        if (inv != null && inv.armorInventory != null) {
            ItemStack helm  = inv.armorInventory[3];
            ItemStack chest = inv.armorInventory[2];
            ItemStack legs  = inv.armorInventory[1];
            ItemStack boots = inv.armorInventory[0];

            // Wool (+2 each)
            if (helm  != null && helm .getItem() == TANItems.wool_helmet)       delta += WARM_PIECE_TARGET_MODIFIER;
            if (chest != null && chest.getItem() == TANItems.wool_chestplate)   delta += WARM_PIECE_TARGET_MODIFIER;
            if (legs  != null && legs .getItem() == TANItems.wool_leggings)     delta += WARM_PIECE_TARGET_MODIFIER;
            if (boots != null && boots.getItem() == TANItems.wool_boots)        delta += WARM_PIECE_TARGET_MODIFIER;

            // Jelled slime (-2 each)
            if (helm  != null && helm .getItem() == TANItems.jelled_slime_helmet)     delta += COOL_PIECE_TARGET_MODIFIER;
            if (chest != null && chest.getItem() == TANItems.jelled_slime_chestplate) delta += COOL_PIECE_TARGET_MODIFIER;
            if (legs  != null && legs .getItem() == TANItems.jelled_slime_leggings)   delta += COOL_PIECE_TARGET_MODIFIER;
            if (boots != null && boots.getItem() == TANItems.jelled_slime_boots)      delta += COOL_PIECE_TARGET_MODIFIER;

            // Leaf armor treated same as slime (-2 each)
            if (helm  != null && helm .getItem() == TANItems.leaf_helmet)       delta += COOL_PIECE_TARGET_MODIFIER;
            if (chest != null && chest.getItem() == TANItems.leaf_chestplate)   delta += COOL_PIECE_TARGET_MODIFIER;
            if (legs  != null && legs .getItem() == TANItems.leaf_leggings)     delta += COOL_PIECE_TARGET_MODIFIER;
            if (boots != null && boots.getItem() == TANItems.leaf_boots)        delta += COOL_PIECE_TARGET_MODIFIER;
        }

        // Clamp overall armor effect so it’s strong but not overwhelming
        if (delta >  TARGET_CLAMP) delta =  TARGET_CLAMP;
        if (delta < -TARGET_CLAMP) delta = -TARGET_CLAMP;

        out += delta;
        out = Math.max(0, Math.min(TemperatureScale.getScaleTotal(), out));

        debugger.end(out);
        return new Temperature(out);
    }
}
