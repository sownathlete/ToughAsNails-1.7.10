package toughasnails.thirst;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.api.stat.capability.IThirst;

/**
 * Backported 1.7.10-compatible ThirstHandler.
 * Movement exhaustion (START) + metabolic / passive exhaustion (END).
 * Tuned so bars visibly deplete in normal play and quickly when under the
 * Thirst potion effect.
 *
 * We rely on the central StatTickHandler to call update() on BOTH phases.
 */
public class ThirstHandler extends StatHandlerBase implements IThirst {

    private static final String KEY_ROOT = "TAN_Thirst";

    private int   thirstLevel      = 20;   // 0..20
    private int   prevThirstLevel  = 20;
    private float hydrationLevel   = 5.0F; // 0..20
    private float exhaustionLevel;         // converts to hydration/thirst at 4.0
    private int   thirstTimer;

    // movement sampling
    private double prevX, prevY, prevZ;
    private boolean hasPrevPos = false;

    // passive cadence
    private int ticksSincePassive;

    // ---------------------------------------------------------------------
    // Bootstrap: no-op now (tick is driven by StatTickHandler)
    // ---------------------------------------------------------------------
    private static boolean BOOTSTRAPPED = false;
    public static void bootstrap() { BOOTSTRAPPED = true; }

    // ---------------------------------------------------------------------
    // Tick
    // ---------------------------------------------------------------------
    @Override
    public void update(EntityPlayer player, World world, TickEvent.Phase phase) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;

        if (phase == TickEvent.Phase.START) {
            // Movement exhaustion uses position measured last END phase
            if (hasPrevPos) {
                final double dx = player.posX - prevX;
                final double dy = player.posY - prevY;
                final double dz = player.posZ - prevZ;
                final double dist = Math.sqrt(dx * dx + dy * dy + dz * dz); // blocks traveled this tick
                if (dist > 0.0D) applyMovementExhaustion(player, dist);
            }
            return;
        }

        // ===== END PHASE =====
        // save pos for next tick’s START sampling
        prevX = player.posX; prevY = player.posY; prevZ = player.posZ; hasPrevPos = true;

        // Passive / metabolic exhaustion (VISIBLE tuning)
        // Every 20 ticks (1s) we add some exhaustion so you can see bars move.
        ticksSincePassive++;
        if (ticksSincePassive >= 20) {
            ticksSincePassive = 0;

            // Base per-second drain
            float perSecond = 0.10F;             // ~40s per hydration point at rest

            // Nether is hotter → 50% more
            if (world.provider != null && world.provider.isHellWorld) perSecond *= 1.5F;

            // Burning adds a chunk
            if (player.isBurning()) perSecond += 0.20F;

            // Sprinting continuously bleeds a bit even if not moving far
            if (!player.capabilities.isCreativeMode && player.isSprinting()) perSecond += 0.05F;

            // Thirst potion massively accelerates dehydration
            try {
                if (player.isPotionActive(TANPotions.thirst)) {
                    perSecond *= 3.0F;           // multiplier
                    perSecond += 0.30F;          // and a flat add so it’s unmistakable
                }
            } catch (Throwable ignore) {}

            addExhaustion(perSecond);
        }

        // Convert exhaustion → hydration → thirst
        while (exhaustionLevel > 4.0F) {
            exhaustionLevel -= 4.0F;
            if (hydrationLevel > 0.0F) {
                hydrationLevel = Math.max(0.0F, hydrationLevel - 1.0F);
            } else if (world.difficultySetting != EnumDifficulty.PEACEFUL) {
                thirstLevel = Math.max(0, thirstLevel - 1);
            }
        }

        // Starvation-like damage when empty
        if (thirstLevel <= 0) {
            thirstTimer++;
            if (thirstTimer >= 40) { // every 2s (was 4s) so it's noticeable
                EnumDifficulty diff = world.difficultySetting;
                if (player.getHealth() > 10.0F || diff == EnumDifficulty.HARD ||
                   (player.getHealth() > 1.0F && diff == EnumDifficulty.NORMAL)) {
                    player.attackEntityFrom(DamageSource.starve, 1.0F);
                }
                thirstTimer = 0;
            }
        } else {
            thirstTimer = 0;
        }

        // No sprinting when very thirsty
        if (!player.capabilities.isCreativeMode && player.isSprinting() && thirstLevel <= 6) {
            player.setSprinting(false);
        }
    }

    private void applyMovementExhaustion(EntityPlayer player, double distanceBlocks) {
        // Base per-block costs; sprinting costs far more
        float perBlock;
        if (player.isInsideOfMaterial(Material.water) || player.isInWater()) {
            perBlock = 0.015F;
        } else if (player.onGround) {
            perBlock = player.isSprinting() ? 0.10F : 0.01F;
        } else {
            perBlock = 0.02F;
        }
        addExhaustion(perBlock * (float)distanceBlocks);
    }

    // ---------------------------------------------------------------------
    // API
    // ---------------------------------------------------------------------
    @Override
    public void addStats(int thirst, float hydration) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;
        thirstLevel    = Math.min(20, thirstLevel + Math.max(thirst, 0));
        hydrationLevel = Math.min(20F, hydrationLevel + Math.max(thirst * hydration, 0F));
    }

    public void addExhaustion(float amount) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;
        exhaustionLevel = Math.min(40.0F, exhaustionLevel + Math.max(0F, amount));
    }

    public boolean isThirsty() { return thirstLevel < 20; }

    @Override public int   getThirst()              { return thirstLevel; }
    @Override public float getHydration()           { return hydrationLevel; }
    @Override public float getExhaustion()          { return exhaustionLevel; }
    @Override public void  setThirst(int t)         { thirstLevel = Math.max(0, Math.min(20, t)); }
    @Override public void  setHydration(float h)    { hydrationLevel = Math.max(0F, Math.min(20F, h)); }
    @Override public void  setExhaustion(float e)   { exhaustionLevel = Math.max(0F, Math.min(40F, e)); }
    @Override public void  setChangeTime(int ticks) { thirstTimer = Math.max(0, ticks); }
    @Override public int   getChangeTime()          { return thirstTimer; }

    @Override public boolean hasChanged()           { return thirstLevel != prevThirstLevel; }
    @Override public void onSendClientUpdate()      { prevThirstLevel = thirstLevel; }

    // ---------------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------------
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("ThirstLevel", thirstLevel);
        nbt.setFloat  ("HydrationLevel", hydrationLevel);
        nbt.setFloat  ("ExhaustionLevel", exhaustionLevel);
        nbt.setInteger("ThirstTimer", thirstTimer);
        nbt.setInteger("TicksSincePassive", ticksSincePassive);
        nbt.setBoolean("HasPrevPos", hasPrevPos);
        if (hasPrevPos) {
            nbt.setDouble("PrevX", prevX);
            nbt.setDouble("PrevY", prevY);
            nbt.setDouble("PrevZ", prevZ);
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("ThirstLevel"))       thirstLevel = nbt.getInteger("ThirstLevel");
        if (nbt.hasKey("HydrationLevel"))    hydrationLevel = nbt.getFloat("HydrationLevel");
        if (nbt.hasKey("ExhaustionLevel"))   exhaustionLevel = nbt.getFloat("ExhaustionLevel");
        if (nbt.hasKey("ThirstTimer"))       thirstTimer = nbt.getInteger("ThirstTimer");
        if (nbt.hasKey("TicksSincePassive")) ticksSincePassive = nbt.getInteger("TicksSincePassive");
        hasPrevPos = nbt.getBoolean("HasPrevPos");
        if (hasPrevPos) {
            prevX = nbt.getDouble("PrevX");
            prevY = nbt.getDouble("PrevY");
            prevZ = nbt.getDouble("PrevZ");
        }
    }

    @Override
    public IMessage createUpdateMessage() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("ThirstLevel", this.thirstLevel);
        data.setFloat  ("HydrationLevel", this.hydrationLevel);
        data.setFloat  ("ExhaustionLevel", this.exhaustionLevel);
        return new BackportMessageUpdateStat("Thirst", data);
    }

    private static final class BackportMessageUpdateStat implements IMessage {
        private String key;
        private NBTTagCompound data;
        public BackportMessageUpdateStat() {}
        public BackportMessageUpdateStat(String key, NBTTagCompound data) {
            this.key = key;
            this.data = (data == null ? new NBTTagCompound() : data);
        }
        @Override public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, key == null ? "" : key);
            ByteBufUtils.writeTag(buf, data == null ? new NBTTagCompound() : data);
        }
        @Override public void fromBytes(ByteBuf buf) {
            this.key = ByteBufUtils.readUTF8String(buf);
            this.data = ByteBufUtils.readTag(buf);
            if (this.data == null) this.data = new NBTTagCompound();
        }
    }

    // Static helpers
    public static ThirstHandler getOrCreate(EntityPlayer p) {
        NBTTagCompound root = p.getEntityData();
        ThirstHandler h = new ThirstHandler();
        if (root.hasKey(KEY_ROOT)) h.readFromNBT(root.getCompoundTag(KEY_ROOT));
        return h;
    }

    public static void save(EntityPlayer p, ThirstHandler h) {
        NBTTagCompound tag = new NBTTagCompound();
        h.writeToNBT(tag);
        p.getEntityData().setTag(KEY_ROOT, tag);
    }

    /** Kept for older call sites. */
    public static ThirstHandler get(EntityPlayer player) { return getOrCreate(player); }
}
