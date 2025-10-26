package toughasnails.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Syncs player stat data from server to client (Forge 1.7.10 compatible).
 * CLIENT: writes directly into the player's EntityData so client-side readers
 * (like ThirstOverlayHandler using ThirstHandler.get(player)) see updates.
 */
public class MessageUpdateStat implements IMessage {

    private String identifier;     // "thirst", "temperature", etc.
    public  NBTTagCompound data;   // payload

    public MessageUpdateStat() {}

    public MessageUpdateStat(String identifier, NBTTagCompound data) {
        this.identifier = identifier;
        this.data = (data == null ? new NBTTagCompound() : data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.identifier = ByteBufUtils.readUTF8String(buf);
        this.data       = ByteBufUtils.readTag(buf);
        if (this.data == null) this.data = new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.identifier == null ? "" : this.identifier);
        ByteBufUtils.writeTag(buf, this.data == null ? new NBTTagCompound() : this.data);
    }

    public static class Handler implements IMessageHandler<MessageUpdateStat, IMessage> {
        @Override
        public IMessage onMessage(final MessageUpdateStat message, final MessageContext ctx) {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.func_152344_a(new Runnable() {
                @Override
                public void run() {
                    EntityClientPlayerMP player = mc.thePlayer;
                    if (player == null) return;

                    // Mirror into client-side EntityData so any local reader can see it
                    if ("thirst".equalsIgnoreCase(message.identifier)) {
                        player.getEntityData().setTag("TAN_Thirst", message.data);
                    } else if ("temperature".equalsIgnoreCase(message.identifier)) {
                        // Only needed if you choose to sync temperature the same way.
                        player.getEntityData().setTag("TAN_Temperature", message.data);
                    }
                }
            });
            return null;
        }
    }
}
