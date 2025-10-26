package toughasnails.potion;

import net.minecraft.entity.EntityLivingBase;
import toughasnails.api.TANPotions;

public class PotionHeatResistance extends TANPotion {

    public PotionHeatResistance(int id) {
        // 1.7.10 potion constructor order: (id, isBadEffect, color)
        super(id, false, 15025952);
        this.setIconIndex(1, 1);
        this.setPotionName("potion.heat_resistance");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.isPotionActive(TANPotions.hyperthermia)) {
            entity.removePotionEffect(TANPotions.hyperthermia.id);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int time = 50 >> amplifier;
        return time > 0 ? duration % time == 0 : true;
    }
}
