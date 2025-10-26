package toughasnails.thirst;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import toughasnails.api.stat.capability.CapabilityProvider.Capability;
import toughasnails.api.stat.capability.CapabilityProvider.EnumFacing;
import toughasnails.api.stat.capability.CapabilityProvider.IStorage;
import toughasnails.api.stat.capability.IThirst;

public class ThirstStorage implements IStorage<IThirst> {

    @Override
    public NBTBase writeNBT(Capability<IThirst> capability, IThirst instance, EnumFacing side) {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("thirstLevel", instance.getThirst());
        compound.setInteger("thirstTimer", instance.getChangeTime());
        compound.setFloat("thirstHydrationLevel", instance.getHydration());
        compound.setFloat("thirstExhaustionLevel", instance.getExhaustion());

        return compound;
    }

    @Override
    public void readNBT(Capability<IThirst> capability, IThirst instance, EnumFacing side, NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            throw new IllegalArgumentException("Thirst must be read from an NBTTagCompound!");
        }

        NBTTagCompound compound = (NBTTagCompound) nbt;

        if (compound.hasKey("thirstLevel")) {
            instance.setThirst(compound.getInteger("thirstLevel"));
            instance.setHydration(compound.getFloat("thirstHydrationLevel"));
            instance.setExhaustion(compound.getFloat("thirstExhaustionLevel"));
            instance.setChangeTime(compound.getInteger("thirstTimer"));
        }
    }
}
