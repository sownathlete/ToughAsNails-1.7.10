package toughasnails.potion;

import net.minecraft.entity.EntityLivingBase;
import toughasnails.api.TANPotions;

public class PotionColdResistance extends TANPotion {

    public PotionColdResistance(int id) {
        // 1.7.10 potions use (boolean isBadEffect, int liquidColor) in Potion constructor.
        // Your TANPotion likely wraps this, so we only pass effect properties and icon index.
        super(id, false, 7842303); // false = beneficial effect
        this.setIconIndex(2, 1);
        this.setPotionName("potion.cold_resistance");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.isPotionActive(TANPotions.hypothermia)) {
            entity.removePotionEffect(TANPotions.hypothermia.id);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int time = 50 >> amplifier;
        return time > 0 ? duration % time == 0 : true;
    }
}
