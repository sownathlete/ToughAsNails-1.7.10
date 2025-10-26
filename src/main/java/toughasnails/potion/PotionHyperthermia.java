package toughasnails.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class PotionHyperthermia extends TANPotion {

    public PotionHyperthermia(int id) {
        // 1.7.10 constructor order: (id, isBadEffect, color)
        super(id, true, 16753408);
        this.setIconIndex(3, 0);
        this.setPotionName("potion.hyperthermia");
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
