package toughasnails.api.stat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Base class for all Tough As Nails player stat handlers.
 * 
 * 1.7.10 version — adds support for persistence and player context
 * that were handled automatically by Forge capabilities in newer builds.
 */
public abstract class StatHandlerBase implements IPlayerStat {

    protected EntityPlayer player;
    protected boolean changed = false;

    /** Assigns this stat to a player instance. */
    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    /** @return The player associated with this stat. */
    public EntityPlayer getPlayer() {
        return player;
    }

    /** Marks the stat as dirty so it can be synced or saved. */
    public void markDirty() {
        this.changed = true;
    }

    /** Clears the dirty flag after syncing or saving. */
    public void clearChanged() {
        this.changed = false;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void onSendClientUpdate() {
        // Default no-op
    }

    /**
     * Writes this stat's data to NBT. Override in subclasses.
     */
    public void writeToNBT(NBTTagCompound tag) {
        // Implement in subclasses (e.g. thirst, temperature)
    }

    /**
     * Reads this stat's data from NBT. Override in subclasses.
     */
    public void readFromNBT(NBTTagCompound tag) {
        // Implement in subclasses (e.g. thirst, temperature)
    }
}
