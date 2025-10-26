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

import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.api.stat.capability.IThirst;

/**
 * Backported 1.7.10-compatible ThirstHandler.
 * Handles thirst exhaustion, hydration, starvation damage, and sprint prevention.
 * Includes bootstrap() to register a player tick listener so thirst actually changes.
 */
public class ThirstHandler extends StatHandlerBase implements IThirst {

    private static final String KEY_ROOT = "TAN_Thirst";

    private int   thirstLevel      = 20;
    private int   prevThirstLevel  = 20;
    private float hydrationLevel   = 5.0F;
    private float exhaustionLevel;
    private int   thirstTimer;

    private double prevX, prevY, prevZ;
    private boolean hasPrevPos = false;

    /* ============================================================ */
    /* Bootstrap (register tick hooks once)                          */
    /* ============================================================ */
    private static boolean BOOTSTRAPPED = false;

    public static void bootstrap() {
        if (BOOTSTRAPPED) return;
        BOOTSTRAPPED = true;
        FMLCommonHandler.instance().bus().register(new EventHooks());
    }

    /** MUST be public for Forge’s ASM event system. */
    public static final class EventHooks {
        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent e) {
            final EntityPlayer p = e.player;
            final World w = p.worldObj;

            ThirstHandler h = ThirstHandler.getOrCreate(p);
            h.update(p, w, e.phase);

            if (e.phase == TickEvent.Phase.END) {
                ThirstHandler.save(p, h);
            }
        }
    }

    /* ============================================================ */
    /* Core update logic                                             */
    /* ============================================================ */
    @Override
    public void update(EntityPlayer player, World world, TickEvent.Phase phase) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;

        if (phase == TickEvent.Phase.START) {
            if (hasPrevPos) {
                double dx = player.posX - prevX;
                double dy = player.posY - prevY;
                double dz = player.posZ - prevZ;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0;
                if (dist > 0.0D) applyMovementExhaustion(player, (int)Math.round(dist));
            }
            return;
        }

        // END phase
        prevX = player.posX;
        prevY = player.posY;
        prevZ = player.posZ;
        hasPrevPos = true;

        EnumDifficulty diff = world.difficultySetting;

        if (exhaustionLevel > 4.0F) {
            exhaustionLevel -= 4.0F;
            if (hydrationLevel > 0.0F) {
                hydrationLevel = Math.max(hydrationLevel - 1.0F, 0.0F);
            } else if (diff != EnumDifficulty.PEACEFUL) {
                thirstLevel = Math.max(thirstLevel - 1, 0);
            }
        }

        if (thirstLevel <= 0) {
            thirstTimer++;
            if (thirstTimer >= 80) {
                if (player.getHealth() > 10.0F || diff == EnumDifficulty.HARD ||
                   (player.getHealth() > 1.0F && diff == EnumDifficulty.NORMAL)) {
                    player.attackEntityFrom(DamageSource.starve, 1.0F);
                }
                thirstTimer = 0;
            }
        } else {
            thirstTimer = 0;
        }

        if (!player.capabilities.isCreativeMode && player.isSprinting() && thirstLevel <= 6) {
            player.setSprinting(false);
        }
    }

    private void applyMovementExhaustion(EntityPlayer player, int distance) {
        if (player.isInsideOfMaterial(Material.water) || player.isInWater()) {
            addExhaustion(0.015F * distance * 0.01F);
        } else if (player.onGround) {
            addExhaustion((player.isSprinting() ? 0.1F : 0.01F) * distance * 0.01F);
        }
    }

    @Override
    public void addStats(int thirst, float hydration) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;
        thirstLevel = Math.min(thirstLevel + thirst, 20);
        hydrationLevel = Math.min(hydrationLevel + Math.max(thirst * hydration, 0F), 20F);
    }

    public void addExhaustion(float amount) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;
        exhaustionLevel = Math.min(exhaustionLevel + amount, 40.0F);
    }

    public boolean isThirsty() { return thirstLevel < 20; }

    @Override public int   getThirst()              { return thirstLevel; }
    @Override public float getHydration()           { return hydrationLevel; }
    @Override public float getExhaustion()          { return exhaustionLevel; }
    @Override public void  setThirst(int t)         { thirstLevel = t; }
    @Override public void  setHydration(float h)    { hydrationLevel = h; }
    @Override public void  setExhaustion(float e)   { exhaustionLevel = e; }
    @Override public void  setChangeTime(int ticks) { thirstTimer = ticks; }
    @Override public int   getChangeTime()          { return thirstTimer; }

    @Override public boolean hasChanged()  { return thirstLevel != prevThirstLevel; }
    @Override public void onSendClientUpdate() { prevThirstLevel = thirstLevel; }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("ThirstLevel", thirstLevel);
        nbt.setFloat  ("HydrationLevel", hydrationLevel);
        nbt.setFloat  ("ExhaustionLevel", exhaustionLevel);
        nbt.setInteger("ThirstTimer", thirstTimer);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("ThirstLevel"))     thirstLevel = nbt.getInteger("ThirstLevel");
        if (nbt.hasKey("HydrationLevel"))  hydrationLevel = nbt.getFloat("HydrationLevel");
        if (nbt.hasKey("ExhaustionLevel")) exhaustionLevel = nbt.getFloat("ExhaustionLevel");
        if (nbt.hasKey("ThirstTimer"))     thirstTimer = nbt.getInteger("ThirstTimer");
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

    public static ThirstHandler getOrCreate(EntityPlayer p) {
        NBTTagCompound root = p.getEntityData();
        ThirstHandler h = new ThirstHandler();
        if (root.hasKey(KEY_ROOT)) {
            NBTTagCompound tag = root.getCompoundTag(KEY_ROOT);
            h.readFromNBT(tag);
        }
        return h;
    }

    public static void save(EntityPlayer p, ThirstHandler h) {
        NBTTagCompound tag = new NBTTagCompound();
        h.writeToNBT(tag);
        p.getEntityData().setTag(KEY_ROOT, tag);
    }

    /** Kept for older call sites; now delegates to getOrCreate. */
    public static ThirstHandler get(EntityPlayer player) {
        return getOrCreate(player);
    }
}
