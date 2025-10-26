package toughasnails.api.stat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

/**
 * Backported interface for player stats (e.g., thirst, temperature) in 1.7.10.
 * Matches 1.9+ structure but uses cpw.mods.fml.* package equivalents.
 */
public interface IPlayerStat {

    /**
     * Called every player tick to update the stat.
     *
     * @param player The player whose stat is being updated.
     * @param world The world the player is in.
     * @param phase The current tick phase.
     */
    void update(EntityPlayer player, World world, TickEvent.Phase phase);

    /**
     * @return true if the stat has changed since last update and needs syncing.
     */
    boolean hasChanged();

    /**
     * Called on the client side when receiving an update from the server.
     */
    void onSendClientUpdate();

    /**
     * Creates a network packet containing stat sync data.
     * This should be sent to the client to keep stats synchronized.
     *
     * @return An IMessage object containing the stat data.
     */
    IMessage createUpdateMessage();
}
