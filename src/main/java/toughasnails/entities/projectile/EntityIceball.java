package toughasnails.entities.projectile;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toughasnails.core.ToughAsNails;
import toughasnails.particle.TANParticleTypes;

public class EntityIceball extends Entity implements IProjectile {

    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private boolean inGround;
    public EntityLivingBase shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    public EntityIceball(World world) {
        super(world);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntityIceball(World world, double x, double y, double z, double accelX, double accelY, double accelZ) {
        super(world);
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.setPosition(x, y, z);
        double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.1D;
        this.accelerationY = accelY / d0 * 0.1D;
        this.accelerationZ = accelZ / d0 * 0.1D;
    }

    public EntityIceball(World world, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
        super(world);
        this.shootingEntity = shooter;
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionX = this.motionY = this.motionZ = 0.0D;

        accelX += this.rand.nextGaussian() * 0.4D;
        accelY += this.rand.nextGaussian() * 0.4D;
        accelZ += this.rand.nextGaussian() * 0.4D;

        double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.1D;
        this.accelerationY = accelY / d0 * 0.1D;
        this.accelerationZ = accelZ / d0 * 0.1D;
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        if (this.worldObj.isRemote || (this.shootingEntity == null || !this.shootingEntity.isDead)
                && this.worldObj.blockExists((int) this.posX, (int) this.posY, (int) this.posZ)) {

            super.onUpdate();

            if (this.inGround) {
                Block block = this.worldObj.getBlock(this.xTile, this.yTile, this.zTile);
                if (block == this.inTile) {
                    ++this.ticksAlive;
                    if (this.ticksAlive == 600) {
                        this.setDead();
                    }
                    return;
                }

                this.inGround = false;
                this.motionX *= this.rand.nextFloat() * 0.2F;
                this.motionY *= this.rand.nextFloat() * 0.2F;
                this.motionZ *= this.rand.nextFloat() * 0.2F;
                this.ticksAlive = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInAir;
            }

            Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY,
                    this.posZ + this.motionZ);
            MovingObjectPosition mop = this.worldObj.rayTraceBlocks(vec3, vec31);

            vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY,
                    this.posZ + this.motionZ);
            if (mop != null) {
                vec31 = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
            }

            Entity hitEntity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                    this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i) {
                Entity entity1 = (Entity) list.get(i);
                if (!entity1.canBeCollidedWith()
                        || (entity1 == this.shootingEntity && this.ticksInAir < 25)) {
                    continue;
                }

                float f = 0.3F;
                AxisAlignedBB aabb = entity1.boundingBox.expand(f, f, f);
                MovingObjectPosition mop1 = aabb.calculateIntercept(vec3, vec31);
                if (mop1 == null)
                    continue;

                double d1 = vec3.squareDistanceTo(mop1.hitVec);
                if (d1 < d0 || d0 == 0.0D) {
                    hitEntity = entity1;
                    d0 = d1;
                }
            }

            if (hitEntity != null) {
                mop = new MovingObjectPosition(hitEntity);
            }

            if (mop != null) {
                this.onImpact(mop);
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;
            this.rotationPitch = (float) (Math.atan2(f1, this.motionY) * 180.0D / Math.PI) - 90.0F;

            while (this.rotationPitch - this.prevRotationPitch < -180.0F)
                this.prevRotationPitch -= 360.0F;
            while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
                this.prevRotationPitch += 360.0F;
            while (this.rotationYaw - this.prevRotationYaw < -180.0F)
                this.prevRotationYaw -= 360.0F;
            while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
                this.prevRotationYaw += 360.0F;

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

            float f2 = this.getMotionFactor();
            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    float f3 = 0.25F;
                    this.worldObj.spawnParticle("bubble",
                            this.posX - this.motionX * f3,
                            this.posY - this.motionY * f3,
                            this.posZ - this.motionZ * f3,
                            this.motionX, this.motionY, this.motionZ);
                }
                f2 = 0.8F;
            }

            this.motionX += this.accelerationX;
            this.motionY += this.accelerationY;
            this.motionZ += this.accelerationZ;
            this.motionX *= f2;
            this.motionY *= f2;
            this.motionZ *= f2;

            ToughAsNails.proxy.spawnParticle(TANParticleTypes.SNOWFLAKE, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);

            this.setPosition(this.posX, this.posY, this.posZ);
        } else {
            this.setDead();
        }
    }

    protected float getMotionFactor() {
        return 0.95F;
    }

    protected void onImpact(MovingObjectPosition mop) {
        if (!this.worldObj.isRemote) {
            if (mop.entityHit != null) {
                mop.entityHit.attackEntityFrom(DamageSource.generic, 5.0F);
            }
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setShort("xTile", (short) this.xTile);
        tag.setShort("yTile", (short) this.yTile);
        tag.setShort("zTile", (short) this.zTile);
        tag.setString("inTile", Block.blockRegistry.getNameForObject(this.inTile));
        tag.setByte("inGround", (byte) (this.inGround ? 1 : 0));
        tag.setTag("direction", this.newDoubleNBTList(new double[] { this.motionX, this.motionY, this.motionZ }));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        this.xTile = tag.getShort("xTile");
        this.yTile = tag.getShort("yTile");
        this.zTile = tag.getShort("zTile");
        this.inTile = Block.getBlockFromName(tag.getString("inTile"));
        this.inGround = tag.getByte("inGround") == 1;
        if (tag.hasKey("direction", 9)) {
            NBTTagList list = tag.getTagList("direction", 6);
            this.motionX = list.func_150309_d(0);
            this.motionY = list.func_150309_d(1);
            this.motionZ = list.func_150309_d(2);
        } else {
            this.setDead();
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public float getCollisionBorderSize() {
        return 0.5F;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable())
            return false;
        this.setBeenAttacked();
        if (source.getEntity() != null) {
            Vec3 vec3 = source.getEntity().getLookVec();
            if (vec3 != null) {
                this.motionX = vec3.xCoord;
                this.motionY = vec3.yCoord;
                this.motionZ = vec3.zCoord;
                this.accelerationX = this.motionX * 0.1D;
                this.accelerationY = this.motionY * 0.1D;
                this.accelerationZ = this.motionZ * 0.1D;
            }
            if (source.getEntity() instanceof EntityLivingBase) {
                this.shootingEntity = (EntityLivingBase) source.getEntity();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {}
}
