// File: toughasnails/temperature/modifier/TemperatureModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Base class for every temperature modifier.
 * <p>
 * 1.7.10 backport: no 1.8+ interfaces; classic read/write NBT helpers.
 */
public abstract class TemperatureModifier {

    protected final TemperatureDebugger debugger;

    protected TemperatureModifier(TemperatureDebugger debugger) {
        this.debugger = debugger;
    }

    /* --------------------------------------------------------------------
       Override in subclasses
       -------------------------------------------------------------------- */

    /** Adjusts how quickly the player’s temperature changes (ticks per step). */
    public abstract int modifyChangeRate(World world,
                                         EntityPlayer player,
                                         int changeRate,
                                         TemperatureTrend trend);

    /** Shifts the target temperature level itself. */
    public abstract Temperature modifyTarget(World world,
                                             EntityPlayer player,
                                             Temperature temperature);

    /* --------------------------------------------------------------------
       External (temporary) modifiers that can be applied by commands,
       potions, items, etc.  Stored & synced via NBT.
       -------------------------------------------------------------------- */
    public static class ExternalModifier {

        private String name   = "";
        private int    amount = 0;   // constant offset to target (levels)
        private int    rate   = 0;   // change-rate adjustment (ticks)
        private int    endTime;      // handler "timer" tick when it expires

        public ExternalModifier() {}

        public ExternalModifier(String name, int amount, int rate, int endTime) {
            this.name    = name;
            this.amount  = amount;
            this.rate    = rate;
            this.endTime = endTime;
        }

        /* --------------- Getters / setters --------------- */

        public String getName()    { return name; }
        public int    getAmount()  { return amount; }
        public int    getRate()    { return rate; }
        public int    getEndTime() { return endTime; }

        public void setAmount(int amount) { this.amount = amount; }
        public void setRate(int rate)     { this.rate   = rate; }
        public void setEndTime(int time)  { this.endTime = time; }

        /* --------------- NBT persistence --------------- */

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString ("Name",    name);
            tag.setInteger("Amount",  amount);
            tag.setInteger("Rate",    rate);
            tag.setInteger("EndTime", endTime);
            return tag;
        }

        public void readFromNBT(NBTTagCompound tag) {
            this.name    = tag.getString ("Name");
            this.amount  = tag.getInteger("Amount");
            this.rate    = tag.getInteger("Rate");
            this.endTime = tag.getInteger("EndTime");
        }
    }
}
