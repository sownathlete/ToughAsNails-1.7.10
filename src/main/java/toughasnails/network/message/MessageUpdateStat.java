package toughasnails.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import toughasnails.api.stat.PlayerStatRegistryCompat;
import toughasnails.api.stat.StatHandlerBase;

/**
 * Syncs player stat data from server to client (Forge 1.7.10 compatible).
 * Uses PlayerStatRegistryCompat to resolve the correct per-player stat handler.
 */
public class MessageUpdateStat implements IMessage {

    private String identifier;
    private NBTTagCompound data;

    public MessageUpdateStat() {}

    /**
     * @param identifier e.g., "temperature"
     * @param data NBT payload for the stat
     */
    public MessageUpdateStat(String identifier, NBTTagCompound data) {
        if (data == null) throw new IllegalArgumentException("Data cannot be null!");
        this.identifier = identifier;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.identifier = ByteBufUtils.readUTF8String(buf);
        this.data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.identifier);
        ByteBufUtils.writeTag(buf, this.data);
    }

    public static class Handler implements IMessageHandler<MessageUpdateStat, IMessage> {
        @Override
        public IMessage onMessage(final MessageUpdateStat message, final MessageContext ctx) {
            // In 1.7.10 this is the proper way to schedule work on the main client thread
            final Minecraft mc = Minecraft.getMinecraft();
            mc.func_152344_a(new Runnable() {
                @Override
                public void run() {
                    EntityClientPlayerMP player = mc.thePlayer;
                    if (player == null) return;

                    StatHandlerBase stat = PlayerStatRegistryCompat.getStat(player, message.identifier);
                    if (stat != null && message.data != null) {
                        // 1.7.10 stats expose direct NBT IO instead of Capability storage
                        stat.readFromNBT(message.data);
                    }
                }
            });
            return null;
        }
    }
}
