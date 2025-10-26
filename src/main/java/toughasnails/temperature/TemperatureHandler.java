// File: toughasnails/temperature/TemperatureHandler.java
package toughasnails.temperature;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.ByteBufUtils;

import io.netty.buffer.ByteBuf;

import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.core.ToughAsNails;
import toughasnails.temperature.modifier.*;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Temperature handler with:
 *  - sustained exposure gates (must remain hot/cold for N seconds)
 *  - hysteresis (clear effects sooner than you apply them to prevent flapping)
 *  - smoother stepping toward target temperature
 *  - external modifier support
 *  - debug logging
 */
public class TemperatureHandler extends StatHandlerBase implements ITemperature {

    public static final int TEMPERATURE_SCALE_MIDPOINT = TemperatureScale.getScaleTotal() / 2;

    /* ------------------ stepping/rate knobs ------------------ */
    private static final int BASE_TEMPERATURE_CHANGE_TICKS = 80; // ~4s baseline per level
    private static final int DELTA_ACCEL_PER_LEVEL        = 6;   // faster when farther from target
    private static final int MIN_TICKS_PER_STEP           = 10;
    private static final int MAX_TICKS_PER_STEP           = 200;

    /* ------------------ effect gating knobs ------------------ */
    private static final int SUSTAIN_TICKS_HOT  = 20 * 12; // ~12s continuously in hot zone
    private static final int SUSTAIN_TICKS_COLD = 20 * 12; // ~12s continuously in cold zone
    private static final int HYSTERESIS_LEVELS  = 2;       // how far back into safe zone before clearing exposure/effects
    private static final int EFFECT_DURATION    = 20 * 10; // base 10s; refreshed while exposure continues
    private static final int EFFECT_RAMP_EXTRA  = 20 * 10; // +10s extra after long sustained exposure

    /* ------------------ state ------------------ */
    private int temperatureLevel;
    private int prevTemperatureLevel;
    private int temperatureTimer;

    // Track how long we've stayed too hot/cold (sustained exposure)
    private int heatExposureTicks = 0;
    private int coldExposureTicks = 0;

    private TemperatureModifier altitudeModifier;
    private TemperatureModifier armorModifier;
    private TemperatureModifier biomeModifier;
    private TemperatureModifier playerStateModifier;
    private TemperatureModifier objectProximityModifier;
    private TemperatureModifier weatherModifier;
    private TemperatureModifier timeModifier;
    private TemperatureModifier seasonModifier;

    private Map<String, TemperatureModifier.ExternalModifier> externalModifiers;
    public final TemperatureDebugger debugger = new TemperatureDebugger();

    // throttle logs a bit
    private int dbgTick = 0;

    public TemperatureHandler() {
        this.prevTemperatureLevel = this.temperatureLevel = TemperatureScale.getScaleTotal() / 2;
        this.altitudeModifier        = new AltitudeModifier(this.debugger);
        this.armorModifier           = new ArmorModifier(this.debugger);
        this.biomeModifier           = new BiomeModifier(this.debugger);
        this.playerStateModifier     = new PlayerStateModifier(this.debugger);
        this.objectProximityModifier = new ObjectProximityModifier(this.debugger);
        this.weatherModifier         = new WeatherModifier(this.debugger);
        this.timeModifier            = new TimeModifier(this.debugger);
        this.seasonModifier          = new SeasonModifier(this.debugger);
        this.externalModifiers       = Maps.newHashMap();
        ToughAsNails.logger.info("[TAN Temp] Handler constructed; midpoint=" + TEMPERATURE_SCALE_MIDPOINT);
    }

    public static TemperatureHandler get(EntityPlayer player) {
        if (player == null) return new TemperatureHandler();
        if (!player.getEntityData().hasKey("TAN_Temperature")) {
            TemperatureHandler h = new TemperatureHandler();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("temperature", h.temperatureLevel);
            player.getEntityData().setTag("TAN_Temperature", tag);
            ToughAsNails.logger.info("[TAN Temp] Created new tag for " + player.getCommandSenderName() + " temp=" + h.temperatureLevel);
            return h;
        }
        NBTTagCompound tag = player.getEntityData().getCompoundTag("TAN_Temperature");
        TemperatureHandler h = new TemperatureHandler();
        h.temperatureLevel = tag.getInteger("temperature");
        ToughAsNails.logger.info("[TAN Temp] Loaded tag for " + player.getCommandSenderName() + " temp=" + h.temperatureLevel);
        return h;
    }

    @Override
    public void update(EntityPlayer player, World world, TickEvent.Phase phase) {
        // We get called for BOTH phases; apply logic on END (mirrors thirst/your original)
        if (phase != TickEvent.Phase.END || world.isRemote) return;

        final String pname = player.getCommandSenderName();

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            this.debugger.targetTemperature = this.temperatureLevel;
            ToughAsNails.logger.debug("[TAN Temp] DISABLED via config; cur=" + this.temperatureLevel + " player=" + pname);
            return;
        }

        /* ---------------- Build TARGET ---------------- */
        Temperature target = new Temperature(TEMPERATURE_SCALE_MIDPOINT);
        target = this.biomeModifier.modifyTarget(world, player, target);
        target = this.altitudeModifier.modifyTarget(world, player, target);
        target = this.armorModifier.modifyTarget(world, player, target);
        target = this.playerStateModifier.modifyTarget(world, player, target);
        target = this.objectProximityModifier.modifyTarget(world, player, target);
        target = this.weatherModifier.modifyTarget(world, player, target);
        target = this.timeModifier.modifyTarget(world, player, target);
        target = this.seasonModifier.modifyTarget(world, player, target);

        int targetRaw = MathHelper.clamp_int(target.getRawValue(), 0, TemperatureScale.getScaleTotal());

        // >>> Small, explicit environment push so Nether/lava visibly go HOT <<<
        targetRaw = applyEnvironmentHeatModifiers(player, world, targetRaw);

        this.debugger.targetTemperature = targetRaw;

        TemperatureTrend trend = (targetRaw == this.temperatureLevel)
                ? TemperatureTrend.STILL
                : (targetRaw > this.temperatureLevel ? TemperatureTrend.INCREASING : TemperatureTrend.DECREASING);

        /* ---------------- Rate calculation ---------------- */
        int changeTicks = BASE_TEMPERATURE_CHANGE_TICKS;
        int before = changeTicks;

        changeTicks = this.altitudeModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Altitude: " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.armorModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Armor   : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.biomeModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Biome   : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.playerStateModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after State   : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.objectProximityModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Objects : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.weatherModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Weather : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.timeModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Time    : " + before + " -> " + changeTicks); before = changeTicks;

        changeTicks = this.seasonModifier.modifyChangeRate(world, player, changeTicks, trend);
        ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after Season  : " + before + " -> " + changeTicks);

        // External modifiers (climatisation, etc.)
        Iterator<TemperatureModifier.ExternalModifier> it = this.externalModifiers.values().iterator();
        int extStart = changeTicks;
        int extAdded = 0;
        this.debugger.start(TemperatureDebugger.Modifier.CLIMATISATION_RATE, changeTicks);
        while (it.hasNext()) {
            TemperatureModifier.ExternalModifier m = it.next();
            if (this.temperatureTimer > m.getEndTime()) { it.remove(); continue; }
            changeTicks += m.getRate();
            extAdded += m.getRate();
        }
        this.debugger.end(changeTicks);
        if (extAdded != 0) {
            ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Rate after EXTERNAL: " + extStart + " +(" + extAdded + ") -> " + changeTicks);
        }

        // Acceleration & step cooldown
        int delta  = Math.abs(targetRaw - this.temperatureLevel);
        int accel  = DELTA_ACCEL_PER_LEVEL * delta;
        int stepCd = Math.max(MIN_TICKS_PER_STEP, Math.min(MAX_TICKS_PER_STEP, changeTicks - accel));

        this.debugger.temperatureTimer = this.temperatureTimer;
        this.debugger.changeTicks      = stepCd;

        // periodic summary log
        if ((++dbgTick % 20) == 0) {
            ToughAsNails.logger.info(
                "[TAN Temp]["+pname+"] cur=" + this.temperatureLevel +
                " target=" + targetRaw +
                " trend=" + trend +
                " baseRate=" + BASE_TEMPERATURE_CHANGE_TICKS +
                " finalRate=" + changeTicks +
                " delta=" + delta +
                " accel=" + accel +
                " stepCd=" + stepCd +
                " timer=" + this.temperatureTimer
            );
        }

        /* ---------------- step toward target ---------------- */
        this.temperatureTimer++;
        if (this.temperatureTimer >= stepCd && targetRaw != this.temperatureLevel) {
            int step = (targetRaw > this.temperatureLevel) ? 1 : -1;
            ToughAsNails.logger.info("[TAN Temp]["+pname+"] APPLY STEP " + step + " (cur="+this.temperatureLevel+" -> "+(this.temperatureLevel+step)+")");
            this.addTemperature(new Temperature(step));
            this.temperatureTimer = 0;
        } else if (targetRaw == this.temperatureLevel) {
            ToughAsNails.logger.debug("[TAN Temp]["+pname+"] Target equals current; no step (cur="+this.temperatureLevel+")");
        }

        /* ---------------- exposure tracking + effects ---------------- */
        this.updateExposureTimers();
        this.applySustainedEffects(player);

        // client debug packet throttle
        if (player instanceof EntityPlayerMP && ++this.debugger.debugTimer % 5 == 0) {
            this.debugger.finalize((EntityPlayerMP) player);
        }
    }

    /** Push target hotter in the Nether and near local heat sources so the icon/vignette show. */
    private int applyEnvironmentHeatModifiers(EntityPlayer player, World world, int target) {
        int t = target;

        // Strong baseline heat in the Nether
        if (world.provider != null && world.provider.isHellWorld) {
            t += 7; // enough to push into warm/hot ranges
        }

        // Local heat sources: scan a 3x3x3 around the player
        int px = (int)Math.floor(player.posX);
        int py = (int)Math.floor(player.posY);
        int pz = (int)Math.floor(player.posZ);
        int localHeat = 0;

        for (int dx = -1; dx <= 1; dx++)
        for (int dy = -1; dy <= 1; dy++)
        for (int dz = -1; dz <= 1; dz++) {
            net.minecraft.block.Block b = world.getBlock(px + dx, py + dy, pz + dz);
            if (b == Blocks.lava || b == Blocks.flowing_lava) localHeat += 2;
            else if (b == Blocks.fire) localHeat += 1;
        }

        if (localHeat > 0) {
            t += Math.min(6, localHeat);
        }

        int max = TemperatureScale.getScaleTotal();
        if (t < 0)  t = 0;
        if (t > max) t = max;
        return t;
    }

    /* -------------------------------------------------------------
       Sustained exposure + hysteresis
       ------------------------------------------------------------- */
    private void updateExposureTimers() {
        final int total     = TemperatureScale.getScaleTotal();
        final int hotEnter  = (int)(total * 0.75f);
        final int coldEnter = (int)(total * 0.25f);
        final int hotClear  = Math.max(0, hotEnter  - HYSTERESIS_LEVELS);
        final int coldClear = Math.min(total, coldEnter + HYSTERESIS_LEVELS);

        // Heat exposure
        if (this.temperatureLevel > hotEnter) {
            heatExposureTicks = Math.min(heatExposureTicks + 1, 60 * 20);
        } else if (this.temperatureLevel <= hotClear) {
            heatExposureTicks = Math.max(0, heatExposureTicks - 4);   // decay faster when safe
        } else {
            heatExposureTicks = Math.max(0, heatExposureTicks - 1);   // slow decay in hysteresis band
        }

        // Cold exposure
        if (this.temperatureLevel < coldEnter) {
            coldExposureTicks = Math.min(coldExposureTicks + 1, 60 * 20);
        } else if (this.temperatureLevel >= coldClear) {
            coldExposureTicks = Math.max(0, coldExposureTicks - 4);
        } else {
            coldExposureTicks = Math.max(0, coldExposureTicks - 1);
        }
    }

    private void applySustainedEffects(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) return;

        final int total     = TemperatureScale.getScaleTotal();
        final int hotEnter  = (int)(total * 0.75f);
        final int coldEnter = (int)(total * 0.25f);

        // ---------- Hyperthermia ----------
        if (this.temperatureLevel > hotEnter
                && heatExposureTicks >= SUSTAIN_TICKS_HOT
                && !player.isPotionActive(TANPotions.heat_resistance)) {

            int dur = EFFECT_DURATION;
            if (heatExposureTicks >= SUSTAIN_TICKS_HOT + 20 * 20) {
                dur += EFFECT_RAMP_EXTRA; // longer if exposure continues well beyond gate
            }

            PotionEffect cur = player.getActivePotionEffect(TANPotions.hyperthermia);
            if (cur == null || cur.getDuration() < dur / 2) {
                if (cur != null) player.removePotionEffect(TANPotions.hyperthermia.id);
                player.addPotionEffect(new PotionEffect(TANPotions.hyperthermia.id, dur, 0));
                ToughAsNails.logger.info("[TAN Temp]["+player.getCommandSenderName()+"] Hyperthermia (dur="+dur+")");
            }
        }

        // Clear hyperthermia once you cool sufficiently and exposure is gone
        if (this.temperatureLevel <= hotEnter - HYSTERESIS_LEVELS && heatExposureTicks == 0) {
            if (player.isPotionActive(TANPotions.hyperthermia)) {
                player.removePotionEffect(TANPotions.hyperthermia.id);
                ToughAsNails.logger.info("[TAN Temp]["+player.getCommandSenderName()+"] Hyperthermia cleared");
            }
        }

        // ---------- Hypothermia ----------
        if (this.temperatureLevel < coldEnter
                && coldExposureTicks >= SUSTAIN_TICKS_COLD
                && !player.isPotionActive(TANPotions.cold_resistance)) {

            int dur = EFFECT_DURATION;
            if (coldExposureTicks >= SUSTAIN_TICKS_COLD + 20 * 20) {
                dur += EFFECT_RAMP_EXTRA;
            }

            PotionEffect cur = player.getActivePotionEffect(TANPotions.hypothermia);
            if (cur == null || cur.getDuration() < dur / 2) {
                if (cur != null) player.removePotionEffect(TANPotions.hypothermia.id);
                player.addPotionEffect(new PotionEffect(TANPotions.hypothermia.id, dur, 0));
                ToughAsNails.logger.info("[TAN Temp]["+player.getCommandSenderName()+"] Hypothermia (dur="+dur+")");
            }
        }

        if (this.temperatureLevel >= coldEnter + HYSTERESIS_LEVELS && coldExposureTicks == 0) {
            if (player.isPotionActive(TANPotions.hypothermia)) {
                player.removePotionEffect(TANPotions.hypothermia.id);
                ToughAsNails.logger.info("[TAN Temp]["+player.getCommandSenderName()+"] Hypothermia cleared");
            }
        }
    }

    /* -------------------------------------------------------------
       ITemperature
       ------------------------------------------------------------- */
    @Override public boolean hasChanged() { return this.prevTemperatureLevel != this.temperatureLevel; }
    @Override public void onSendClientUpdate() { this.prevTemperatureLevel = this.temperatureLevel; }

    @Override
    public IMessage createUpdateMessage() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("Temperature", this.temperatureLevel);
        return new BackportMessageUpdateStat("Temperature", data);
    }

    @Override public void setChangeTime(int ticks) { this.temperatureTimer = ticks; }
    @Override public int  getChangeTime()          { return this.temperatureTimer; }

    @Override public void setTemperature(Temperature t) { this.temperatureLevel = t.getRawValue(); }
    @Override public Temperature getTemperature() { return new Temperature(this.temperatureLevel); }

    @Override
    public void addTemperature(Temperature d) {
        int before = this.temperatureLevel;
        this.temperatureLevel = Math.max(0, Math.min(TemperatureScale.getScaleTotal(), this.temperatureLevel + d.getRawValue()));
        ToughAsNails.logger.debug("[TAN Temp] addTemperature " + d.getRawValue() + " : " + before + " -> " + this.temperatureLevel);
    }

    @Override
    public void applyModifier(String name, int amount, int rate, int duration) {
        if (this.externalModifiers.containsKey(name)) {
            TemperatureModifier.ExternalModifier m = this.externalModifiers.get(name);
            m.setAmount(amount);
            m.setRate(rate);
            m.setEndTime(this.temperatureTimer + duration);
        } else {
            TemperatureModifier.ExternalModifier m =
                    new TemperatureModifier.ExternalModifier(name, amount, rate, this.temperatureTimer + duration);
            this.externalModifiers.put(name, m);
        }
        ToughAsNails.logger.info("[TAN Temp] ExternalModifier apply name="+name+" amount="+amount+" rate="+rate+" duration="+duration);
    }

    @Override public boolean hasModifier(String name) { return this.externalModifiers.containsKey(name); }

    @Override
    public ImmutableMap<String, TemperatureModifier.ExternalModifier> getExternalModifiers() {
        return ImmutableMap.copyOf(this.externalModifiers);
    }

    @Override
    public void setExternalModifiers(Map<String, TemperatureModifier.ExternalModifier> m) {
        this.externalModifiers = m;
    }

    /* -------------------------------------------------------------
       Optional exposure getters/setters (for NBT persistence helpers)
       ------------------------------------------------------------- */
    public int  getHeatExposureTicks() { return heatExposureTicks; }
    public int  getColdExposureTicks() { return coldExposureTicks; }
    public void setHeatExposureTicks(int v) { heatExposureTicks = Math.max(0, v); }
    public void setColdExposureTicks(int v) { coldExposureTicks = Math.max(0, v); }

    /* -------------------------------------------------------------
       Small backport update packet
       ------------------------------------------------------------- */
    private static final class BackportMessageUpdateStat implements IMessage {
        private String key;
        private NBTTagCompound data;
        public BackportMessageUpdateStat() {}
        public BackportMessageUpdateStat(String key, NBTTagCompound data) {
            this.key = key; this.data = (data == null ? new NBTTagCompound() : data);
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
}
