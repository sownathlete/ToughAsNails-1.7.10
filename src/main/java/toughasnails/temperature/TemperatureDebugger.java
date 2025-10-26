package toughasnails.temperature;

import java.util.LinkedHashMap;
import java.util.Map;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import toughasnails.handler.PacketHandler;
import toughasnails.network.message.MessageTemperatureClient;
import toughasnails.network.message.MessageToggleUI;
import toughasnails.util.MapUtils;

@SuppressWarnings("unchecked")
public class TemperatureDebugger {

    /** Array of modifier maps for RATE/TARGET categories */
    public Map<Modifier, Integer>[] modifiers = new LinkedHashMap[ModifierType.values().length];

    private boolean showGui = false;
    public int debugTimer;
    public int temperatureTimer;
    public int changeTicks;
    public int targetTemperature;
    private boolean currentlyMeasuring = false;
    private Modifier currentModifier;
    private int currentLevel = -1;

    public TemperatureDebugger() {
        for (int i = 0; i < ModifierType.values().length; i++) {
            this.modifiers[i] = new LinkedHashMap<Modifier, Integer>();
        }
    }

    public void start(Modifier modifier, int startLevel) {
        if (this.currentlyMeasuring) {
            throw new RuntimeException("Already measuring!");
        }
        this.currentModifier = modifier;
        this.currentLevel = startLevel;
        this.currentlyMeasuring = true;
    }

    public void end(int endLevel) {
        if (!this.currentlyMeasuring) {
            throw new RuntimeException("No measurement has been started!");
        }
        int difference = endLevel - this.currentLevel;
        if (difference != 0) {
            this.modifiers[this.currentModifier.modifierType.ordinal()].put(this.currentModifier, difference);
        }
        this.currentlyMeasuring = false;
    }

    public void finalize(EntityPlayerMP player) {
        this.debugTimer = 0;
        if (this.showGui) {
            this.sortModifiers();
        }
        PacketHandler.instance.sendTo(
            (IMessage)new MessageTemperatureClient(this.temperatureTimer, this.changeTicks, this.targetTemperature, this.modifiers),
            player
        );
        this.clearModifiers();
    }

    private void sortModifiers() {
        for (int i = 0; i < this.modifiers.length; i++) {
            this.modifiers[i] = MapUtils.sortMapByValue(this.modifiers[i]);
        }
    }

    public void clearModifiers() {
        for (int i = 0; i < this.modifiers.length; i++) {
            this.modifiers[i].clear();
        }
    }

    public void setGuiVisible(boolean state, EntityPlayerMP updatePlayer) {
        this.showGui = state;
        this.debugTimer = 0;
        if (updatePlayer != null) {
            PacketHandler.instance.sendTo((IMessage)new MessageToggleUI(state), updatePlayer);
        }
    }

    public void setGuiVisible(boolean state) {
        this.setGuiVisible(state, null);
    }

    public boolean isGuiVisible() {
        return this.showGui;
    }

    public static enum ModifierType {
        RATE,
        TARGET;
    }

    public static enum Modifier {
        EQUILIBRIUM_TARGET("Equilibrium", ModifierType.TARGET),
        BIOME_HUMIDITY_RATE("Biome Humidity", ModifierType.RATE),
        BIOME_TEMPERATURE_TARGET("Biome Temperature", ModifierType.TARGET),
        NEARBY_BLOCKS_RATE("Nearby Blocks", ModifierType.RATE),
        NEARBY_BLOCKS_TARGET("Nearby Blocks", ModifierType.TARGET),
        SPRINTING_RATE("Sprinting", ModifierType.RATE),
        HEALTH_RATE("Health", ModifierType.RATE),
        ALTITUDE_TARGET("Altitude", ModifierType.TARGET),
        ARMOR_TARGET("Armor", ModifierType.TARGET),
        ARMOR_RATE("Armor", ModifierType.RATE),
        SPRINTING_TARGET("Sprinting", ModifierType.TARGET),
        TIME_TARGET("Time", ModifierType.TARGET),
        WET_RATE("Wet", ModifierType.RATE),
        WET_TARGET("Wet", ModifierType.TARGET),
        SNOW_TARGET("Snow", ModifierType.TARGET),
        CLIMATISATION_TARGET("Climatisation", ModifierType.TARGET),
        CLIMATISATION_RATE("Climatisation", ModifierType.RATE),
        SEASON_TARGET("Season", ModifierType.TARGET);

        public final String name;
        public final ModifierType modifierType;

        private Modifier(String name, ModifierType modifierType) {
            this.name = name;
            this.modifierType = modifierType;
        }
    }
}
