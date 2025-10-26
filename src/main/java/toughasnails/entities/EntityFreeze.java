package toughasnails.entities;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;
import toughasnails.core.ToughAsNails;
import toughasnails.entities.projectile.EntityIceball;
import toughasnails.particle.TANParticleTypes;

public class EntityFreeze extends EntityMob implements IMob {

    private float heightOffset = 0.5F;
    private int heightOffsetUpdateTime;

    public EntityFreeze(World world) {
        super(world);
        this.experienceValue = 10;
        this.tasks.addTask(4, new AIFireballAttack(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(48.0D);
    }

    @Override
    public void onLivingUpdate() {
        if (!this.onGround && this.motionY < 0.0D)
            this.motionY *= 0.6D;

        if (this.worldObj.isRemote) {
            for (int i = 0; i < 2; ++i) {
                ToughAsNails.proxy.spawnParticle(
                        TANParticleTypes.SNOWFLAKE,
                        this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
                        this.posY + this.rand.nextDouble() * this.height,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
                        0.0D, 0.0D, 0.0D);
            }
        }

        super.onLivingUpdate();
    }

    @Override
    protected void updateAITasks() {
        if (this.isBurning())
            this.attackEntityFrom(DamageSource.inFire, 1.0F);

        --this.heightOffsetUpdateTime;
        if (this.heightOffsetUpdateTime <= 0) {
            this.heightOffsetUpdateTime = 100;
            this.heightOffset = 0.5F + (float) this.rand.nextGaussian() * 3.0F;
        }

        EntityLivingBase target = this.getAttackTarget();
        if (target != null && target.posY + target.getEyeHeight() >
                this.posY + this.getEyeHeight() + this.heightOffset) {
            this.motionY += (0.3D - this.motionY) * 0.3D;
            this.isAirBorne = true;
        }

        super.updateAITasks();
    }

    @Override
    protected Item getDropItem() {
        return TANItems.freeze_rod;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int looting) {
        if (wasRecentlyHit) {
            int count = this.rand.nextInt(2 + looting);
            for (int i = 0; i < count; ++i)
                this.dropItem(TANItems.freeze_rod, 1);
        }
    }

    @Override
    protected boolean isValidLightLevel() {
        return true;
    }

    /** AI that handles shooting iceballs */
    static class AIFireballAttack extends EntityAIBase {
        private final EntityFreeze freeze;
        private int attackStep;
        private int attackTime;

        public AIFireballAttack(EntityFreeze freeze) {
            this.freeze = freeze;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.freeze.getAttackTarget();
            return target != null && target.isEntityAlive();
        }

        @Override
        public void startExecuting() {
            this.attackStep = 0;
        }

        @Override
        public void updateTask() {
            --this.attackTime;
            EntityLivingBase target = this.freeze.getAttackTarget();
            double distSq = this.freeze.getDistanceSqToEntity(target);

            if (distSq < 4.0D) {
                if (this.attackTime <= 0) {
                    this.attackTime = 20;
                    this.freeze.attackEntityAsMob(target);
                }
                this.freeze.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            } else if (distSq < 256.0D) {
                double dx = target.posX - this.freeze.posX;
                double dy = target.posY + target.getEyeHeight() - (this.freeze.posY + this.freeze.height / 2.0D);
                double dz = target.posZ - this.freeze.posZ;

                if (this.attackTime <= 0) {
                    ++this.attackStep;
                    if (this.attackStep == 1) this.attackTime = 60;
                    else if (this.attackStep <= 4) this.attackTime = 6;
                    else {
                        this.attackTime = 100;
                        this.attackStep = 0;
                    }

                    if (this.attackStep > 1) {
                        float f = MathHelper.sqrt_float((float)MathHelper.sqrt_double(distSq)) * 0.5F;
                        this.freeze.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1009,
                                (int)this.freeze.posX, (int)this.freeze.posY, (int)this.freeze.posZ, 0);

                        EntityIceball iceball = new EntityIceball(
                                this.freeze.worldObj, this.freeze,
                                dx + this.freeze.rand.nextGaussian() * f,
                                dy,
                                dz + this.freeze.rand.nextGaussian() * f);
                        iceball.posY = this.freeze.posY + this.freeze.height / 2.0F + 0.5D;
                        this.freeze.worldObj.spawnEntityInWorld(iceball);
                    }
                }

                this.freeze.getLookHelper().setLookPositionWithEntity(target, 10.0F, 10.0F);
            } else {
                this.freeze.getNavigator().clearPathEntity();
            }
        }
    }
}
