package toughasnails.api;

import net.minecraft.potion.Potion;

/**
 * Backported Tough As Nails potion registry for 1.7.10.
 * 
 * In 1.7.10, PotionType did not exist, so potion variants are handled manually.
 * This class preserves field structure for cross-version compatibility.
 */
public class TANPotions {

    public static Potion hypothermia;
    public static Potion hyperthermia;
    public static Potion thirst;
    public static Potion cold_resistance;
    public static Potion heat_resistance;

    // These mimic PotionTypes from newer versions — stored as references to Potion + duration
    public static PotionEntry cold_resistance_type;
    public static PotionEntry long_cold_resistance_type;
    public static PotionEntry heat_resistance_type;
    public static PotionEntry long_heat_resistance_type;

    /**
     * Represents a simplified stand-in for PotionType from newer Minecraft versions.
     * Holds a reference to the base potion and default duration for use in 1.7.10.
     */
    public static class PotionEntry {
        public final Potion potion;
        public final int duration;

        public PotionEntry(Potion potion, int duration) {
            this.potion = potion;
            this.duration = duration;
        }
    }
}
