package toughasnails.season;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class SeasonSavedData extends WorldSavedData {

    public static final String DATA_IDENTIFIER = "seasons";
    public int seasonCycleTicks;

    public SeasonSavedData() {
        this(DATA_IDENTIFIER);
    }

    public SeasonSavedData(String identifier) {
        super(identifier);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("SeasonCycleTicks")) {
            this.seasonCycleTicks = nbt.getInteger("SeasonCycleTicks");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("SeasonCycleTicks", this.seasonCycleTicks);
    }
}
