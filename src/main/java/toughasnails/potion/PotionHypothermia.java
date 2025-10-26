package toughasnails.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class PotionHypothermia extends TANPotion {

    public PotionHypothermia(int id) {
        // Correct 1.7.10 constructor order: (id, isBadEffect, color)
        super(id, true, 11063295);
        this.setIconIndex(2, 0);
        this.setPotionName("potion.hypothermia");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.attackEntityFrom(DamageSource.generic, 0.5F);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int time = 50 >> amplifier;
        return time > 0 ? duration % time == 0 : true;
    }
}
