package toughasnails.temperature;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import toughasnails.api.stat.capability.CapabilityProvider.Capability;
import toughasnails.api.stat.capability.CapabilityProvider.EnumFacing;
import toughasnails.api.stat.capability.CapabilityProvider.IStorage;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.modifier.TemperatureModifier;

/**
 * 1.7.10-compatible storage adapter for the Temperature capability.
 * Persists the current temperature value, change timer, and any active
 * external modifiers to NBT. Relies on the back-ported
 * TemperatureModifier.ExternalModifier#writeToNBT/readFromNBT helpers.
 */
public class TemperatureStorage implements IStorage<ITemperature> {

    /* ------------------------------------------------------------
       Save
       ------------------------------------------------------------ */
    @Override
    public NBTBase writeNBT(Capability<ITemperature> capability,
                            ITemperature instance,
                            EnumFacing side) {

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("temperatureLevel", instance.getTemperature().getRawValue());
        tag.setInteger("temperatureTimer", instance.getChangeTime());

        // External modifiers
        NBTTagList list = new NBTTagList();
        for (TemperatureModifier.ExternalModifier m : instance.getExternalModifiers().values()) {
            list.appendTag(m.writeToNBT());
        }
        tag.setTag("ExternalModifiers", list);

        return tag;
    }

    /* ------------------------------------------------------------
       Load
       ------------------------------------------------------------ */
    @Override
    public void readNBT(Capability<ITemperature> capability,
                        ITemperature instance,
                        EnumFacing side,
                        NBTBase nbt) {

        if (!(nbt instanceof NBTTagCompound)) {
            throw new IllegalArgumentException("Temperature must be read from an NBTTagCompound!");
        }

        NBTTagCompound tag = (NBTTagCompound) nbt;

        // Nothing stored yet
        if (!tag.hasKey("temperatureLevel")) return;

        instance.setTemperature(new Temperature(tag.getInteger("temperatureLevel")));
        instance.setChangeTime(tag.getInteger("temperatureTimer"));

        // External modifiers
        NBTTagList list = tag.getTagList("ExternalModifiers", 10); // 10 = COMPOUND
        Map<String, TemperatureModifier.ExternalModifier> map = Maps.newHashMap();

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound modTag = list.getCompoundTagAt(i);
            TemperatureModifier.ExternalModifier m = new TemperatureModifier.ExternalModifier();
            m.readFromNBT(modTag);
            map.put(m.getName(), m);
        }

        instance.setExternalModifiers(map);
    }
}
