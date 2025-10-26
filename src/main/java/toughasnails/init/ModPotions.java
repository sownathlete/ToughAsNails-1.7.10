package toughasnails.init;

import net.minecraft.potion.Potion;
import toughasnails.api.TANPotions;
import toughasnails.potion.PotionColdResistance;
import toughasnails.potion.PotionHeatResistance;
import toughasnails.potion.PotionHyperthermia;
import toughasnails.potion.PotionHypothermia;
import toughasnails.potion.PotionThirst;

public class ModPotions {

    public static void init() {
        // Use IDs that are free in your pack. 24–28 are inside vanilla array bounds for 1.7.10.
        TANPotions.hypothermia    = register(new PotionHypothermia(24).setPotionName("potion.hypothermia"));
        TANPotions.hyperthermia   = register(new PotionHyperthermia(25).setPotionName("potion.hyperthermia"));
        TANPotions.thirst         = register(new PotionThirst(26).setPotionName("potion.thirst"));
        TANPotions.cold_resistance= register(new PotionColdResistance(27).setPotionName("potion.cold_resistance"));
        TANPotions.heat_resistance= register(new PotionHeatResistance(28).setPotionName("potion.heat_resistance"));

        // 1.7.10 has no PotionType system; create longer/shorter variants by applying different
        // PotionEffect durations in code (e.g., on item use or brewing handler).
    }

    private static Potion register(Potion p) {
        // 1.7.10: no Forge registry for potions — returning the instance is enough.
        // If you ever use IDs >= 32, you’ll need to expand Potion.potionTypes via reflection.
        return p;
    }
}
