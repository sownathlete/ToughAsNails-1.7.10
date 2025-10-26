package toughasnails.temperature;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.modifier.AltitudeModifier;
import toughasnails.temperature.modifier.ArmorModifier;
import toughasnails.temperature.modifier.BiomeModifier;
import toughasnails.temperature.modifier.ObjectProximityModifier;
import toughasnails.temperature.modifier.PlayerStateModifier;
import toughasnails.temperature.modifier.SeasonModifier;
import toughasnails.temperature.modifier.TemperatureModifier;
import toughasnails.temperature.modifier.TimeModifier;
import toughasnails.temperature.modifier.WeatherModifier;

/**
 * Tough As Nails – **TemperatureHandler** (Forge 1.7.10 back-port).  
 * <p>
 * Implements the original 1.10-series temperature logic while relying only on
 * the APIs that exist in Minecraft 1.7.10.</p>
 */
public class TemperatureHandler
        extends StatHandlerBase
        implements ITemperature {

    /* --------------------------------------------------------------------- */
    /*  Constants                                                             */
    /* --------------------------------------------------------------------- */

    public static final int BASE_TEMPERATURE_CHANGE_TICKS = 1200;
    private static final int SCALE_TOTAL        = TemperatureScale.getScaleTotal();
    private static final int SCALE_MIDPOINT     = SCALE_TOTAL / 2;

    /* --------------------------------------------------------------------- */
    /*  Live state                                                            */
    /* --------------------------------------------------------------------- */

    private int temperatureLevel   = SCALE_MIDPOINT;
    private int prevTemperatureLvl = SCALE_MIDPOINT;
    private int temperatureTimer   = 0;

    /*  All modifier instances share one debugger so we can send a single
        consolidated packet to the client.                                   */
    public final TemperatureDebugger debugger = new TemperatureDebugger();

    /*  Built-in modifiers (order = original TAN 1.10 order).                */
    private final TemperatureModifier altitudeModifier        = new AltitudeModifier(debugger);
    private final TemperatureModifier armorModifier           = new ArmorModifier(debugger);
    private final TemperatureModifier biomeModifier           = new BiomeModifier(debugger);
    private final TemperatureModifier playerStateModifier     = new PlayerStateModifier(debugger);
    private final TemperatureModifier objectProximityModifier = new ObjectProximityModifier(debugger);
    private final TemperatureModifier weatherModifier         = new WeatherModifier(debugger);
    private final TemperatureModifier timeModifier            = new TimeModifier(debugger);
    private final TemperatureModifier seasonModifier          = new SeasonModifier(debugger);

    /*  External (command / item) modifiers                                   */
    private Map<String, TemperatureModifier.ExternalModifier> externalModifiers = Maps.newHashMap();

    /* --------------------------------------------------------------------- */
    /*  Convenience accessor                                                 */
    /* --------------------------------------------------------------------- */
    public static TemperatureHandler get(EntityPlayer player) {
        /* On 1.7.10 we persist the handler data directly on the player’s
           EntityData NBT – this avoids having to recreate Forge’s capability
           system.                                                            */
        if (player == null) return new TemperatureHandler();

        NBTTagCompound tag = player.getEntityData().getCompoundTag("TAN_Temperature");
        TemperatureHandler h = new TemperatureHandler();
        h.temperatureLevel = tag.getInteger("temperature");
        return h;
    }

    /* --------------------------------------------------------------------- */
    /*  Main tick entry-point (called by proxy hook every player tick)        */
    /* --------------------------------------------------------------------- */
    @Override
    public void update(EntityPlayer player, World world, TickEvent.Phase phase) {

        if (phase != TickEvent.Phase.END || world.isRemote) return;

        /* ---------------- determine change-rate ------------------------- */
        TemperatureTrend trend = debugger.targetTemperature == temperatureLevel
                                 ? TemperatureTrend.STILL
                                 : (debugger.targetTemperature > temperatureLevel
                                      ? TemperatureTrend.INCREASING
                                      : TemperatureTrend.DECREASING);

        int changeTicks = BASE_TEMPERATURE_CHANGE_TICKS;

        changeTicks = altitudeModifier.modifyChangeRate(world, player, changeTicks, trend);
        changeTicks = armorModifier.modifyChangeRate(world,  player, changeTicks, trend);
        changeTicks = biomeModifier.modifyChangeRate(world,   player, changeTicks, trend);
        changeTicks = playerStateModifier.modifyChangeRate(world, player, changeTicks, trend);
        changeTicks = objectProximityModifier.modifyChangeRate(world, player, changeTicks, trend);
        changeTicks = weatherModifier.modifyChangeRate(world,  player, changeTicks, trend);
        changeTicks = timeModifier.modifyChangeRate(world,     player, changeTicks, trend);
        changeTicks = seasonModifier.modifyChangeRate(world,   player, changeTicks, trend);

        /* external (“climatisation”) modifiers */
        debugger.start(TemperatureDebugger.Modifier.CLIMATISATION_RATE, changeTicks);
        Iterator<TemperatureModifier.ExternalModifier> it = externalModifiers.values().iterator();
        while (it.hasNext()) {
            TemperatureModifier.ExternalModifier m = it.next();
            if (temperatureTimer > m.getEndTime()) { it.remove(); continue; }
            if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE))
                changeTicks += m.getRate();
        }
        debugger.end(changeTicks);

        changeTicks = Math.max(20, changeTicks);

        /* ---------------- countdown / increment ------------------------- */
        boolean tickReached = ++temperatureTimer >= changeTicks;
        boolean sendDebug   = (++debugger.debugTimer % 5) == 0;

        debugger.temperatureTimer = temperatureTimer;
        debugger.changeTicks      = changeTicks;

        if ((tickReached || sendDebug) &&
            SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {

            /* ----------- recalc target on every debug/step ------------- */
            Temperature target = new Temperature(SCALE_MIDPOINT);
            target = biomeModifier.modifyTarget(world, player, target);
            target = altitudeModifier.modifyTarget(world, player, target);
            target = armorModifier.modifyTarget(world,  player, target);
            target = playerStateModifier.modifyTarget(world, player, target);
            target = objectProximityModifier.modifyTarget(world, player, target);
            target = weatherModifier.modifyTarget(world,  player, target);
            target = timeModifier.modifyTarget(world,     player, target);
            target = seasonModifier.modifyTarget(world,   player, target);

            debugger.targetTemperature = MathHelper.clamp_int(target.getRawValue(), 0, SCALE_TOTAL);

            if (tickReached) {
                /* move 1 step towards target */
                int step = (int) Math.signum(debugger.targetTemperature - temperatureLevel);
                addTemperature(new Temperature(step));
                temperatureTimer = 0;
            }
        }

        addPotionEffects(player);

        if (sendDebug && player instanceof EntityPlayerMP)
            debugger.finalize((EntityPlayerMP) player);
    }

    /* --------------------------------------------------------------------- */
    /*  Potion side-effects                                                  */
    /* --------------------------------------------------------------------- */
    private void addPotionEffects(EntityPlayer player) {

        if (player.capabilities.isCreativeMode ||
            !SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE))
            return;

        int icyLimit  = (int) (SCALE_TOTAL * 0.25f);
        int heatLimit = (int) (SCALE_TOTAL * 0.75f);

        /* hypothermia */
        if (temperatureLevel < icyLimit &&
            !player.isPotionActive(TANPotions.cold_resistance) &&
            (!player.isPotionActive(TANPotions.hypothermia) || temperatureLevel < prevTemperatureLvl)) {

            player.removePotionEffect(TANPotions.hypothermia.id);
            player.addPotionEffect(new PotionEffect(TANPotions.hypothermia.id, 200, 0));
        }

        /* hyperthermia */
        if (temperatureLevel > heatLimit &&
            !player.isPotionActive(TANPotions.heat_resistance) &&
            (!player.isPotionActive(TANPotions.hyperthermia) || temperatureLevel > prevTemperatureLvl)) {

            player.removePotionEffect(TANPotions.hyperthermia.id);
            player.addPotionEffect(new PotionEffect(TANPotions.hyperthermia.id, 200, 0));
        }
    }

    /* --------------------------------------------------------------------- */
    /*  StatHandlerBase                                                      */
    /* --------------------------------------------------------------------- */
    @Override public boolean hasChanged()               { return prevTemperatureLvl != temperatureLevel; }
    @Override public void    onSendClientUpdate()       { prevTemperatureLvl = temperatureLevel; }

    /* --------------------------------------------------------------------- */
    /*  Network sync (single-stat packet)                                    */
    /* --------------------------------------------------------------------- */
    @Override
    public IMessage createUpdateMessage() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Temperature", temperatureLevel);
        return new BackportMessageUpdateStat("Temperature", tag);
    }

    /* --------------------------------------------------------------------- */
    /*  ITemperature implementation                                          */
    /* --------------------------------------------------------------------- */
    @Override public void setChangeTime(int t)           { temperatureTimer = t; }
    @Override public int  getChangeTime()                { return temperatureTimer; }
    @Override public void setTemperature(Temperature t)  { temperatureLevel = t.getRawValue(); }
    @Override public Temperature getTemperature()        { return new Temperature(temperatureLevel); }

    @Override
    public void addTemperature(Temperature delta) {
        temperatureLevel = MathHelper.clamp_int(
                temperatureLevel + delta.getRawValue(), 0, SCALE_TOTAL);
    }

    @Override
    public void applyModifier(String name, int amount, int rate, int duration) {
        TemperatureModifier.ExternalModifier m = externalModifiers.get(name);
        if (m == null) {
            m = new TemperatureModifier.ExternalModifier(name, amount, rate,
                                                         temperatureTimer + duration);
            externalModifiers.put(name, m);
        } else {
            m.setAmount(amount);
            m.setRate(rate);
            m.setEndTime(temperatureTimer + duration);
        }
    }

    @Override public boolean hasModifier(String name) { return externalModifiers.containsKey(name); }

    @Override
    public ImmutableMap<String, TemperatureModifier.ExternalModifier> getExternalModifiers() {
        return ImmutableMap.copyOf(externalModifiers);
    }
    @Override
    public void setExternalModifiers(Map<String, TemperatureModifier.ExternalModifier> map) {
        externalModifiers = map == null ? Maps.<String,TemperatureModifier.ExternalModifier>newHashMap() : map;
    }

    /* --------------------------------------------------------------------- */
    /*  Lightweight back-port packet                                         */
    /* --------------------------------------------------------------------- */
    private static final class BackportMessageUpdateStat implements IMessage {
        private String         key = "";
        private NBTTagCompound data = new NBTTagCompound();

        BackportMessageUpdateStat() {}
        BackportMessageUpdateStat(String k, NBTTagCompound d){ key=k; data=d; }

        @Override public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, key == null ? "" : key);
            ByteBufUtils.writeTag(buf, data == null ? new NBTTagCompound() : data);
        }

        @Override public void fromBytes(ByteBuf buf) {
            key  = ByteBufUtils.readUTF8String(buf);
            data = ByteBufUtils.readTag(buf);
            if (data == null) data = new NBTTagCompound();
        }
    }
}
