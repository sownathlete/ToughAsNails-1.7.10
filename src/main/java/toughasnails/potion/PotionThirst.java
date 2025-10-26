package toughasnails.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

public class PotionThirst extends TANPotion {

    public PotionThirst(int id) {
        // 1.7.10 constructor format: (id, isBadEffect, color)
        super(id, true, 6411546);
        this.setIconIndex(0, 0);
        this.setPotionName("potion.thirst");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ThirstHandler handler = (ThirstHandler) ThirstHelper.getThirstData(player);
            if (handler != null) {
                handler.addExhaustion(0.025F * (amplifier + 1));
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int time = 50 >> amplifier;
        return time > 0 ? duration % time == 0 : true;
    }
}
