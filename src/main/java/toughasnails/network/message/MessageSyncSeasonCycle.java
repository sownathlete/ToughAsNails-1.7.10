package toughasnails.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import toughasnails.handler.season.SeasonHandler;

public class MessageSyncSeasonCycle implements IMessage {

    public int seasonCycleTicks;

    public MessageSyncSeasonCycle() {}

    public MessageSyncSeasonCycle(int seasonCycleTicks) {
        this.seasonCycleTicks = seasonCycleTicks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.seasonCycleTicks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.seasonCycleTicks);
    }

    public static class Handler implements IMessageHandler<MessageSyncSeasonCycle, IMessage> {

        @Override
        public IMessage onMessage(final MessageSyncSeasonCycle message, MessageContext ctx) {
            // Schedule update on the Minecraft client thread (1.7.10 method)
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    SeasonHandler.clientSeasonCycleTicks = message.seasonCycleTicks;
                }
            });
            return null;
        }
    }
}
