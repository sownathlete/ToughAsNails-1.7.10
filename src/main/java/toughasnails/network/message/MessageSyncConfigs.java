package toughasnails.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import toughasnails.api.config.SyncedConfig;
import toughasnails.core.ToughAsNails;

import java.util.Set;

public class MessageSyncConfigs implements IMessage {

    public NBTTagCompound nbtOptions;

    public MessageSyncConfigs() {}

    public MessageSyncConfigs(NBTTagCompound nbtOptions) {
        this.nbtOptions = nbtOptions;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtOptions = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbtOptions);
    }

    public static class Handler implements IMessageHandler<MessageSyncConfigs, IMessage> {
        @Override
        public IMessage onMessage(final MessageSyncConfigs message, MessageContext ctx) {
            // Must always run client-side
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    Set<String> keys = message.nbtOptions.func_150296_c(); // getKeySet() in 1.7.10
                    for (String key : keys) {
                        SyncedConfig.SyncedConfigEntry entry = SyncedConfig.optionsToSync.get(key);
                        if (entry == null) {
                            ToughAsNails.logger.error("Option " + key + " does not exist locally!");
                            continue;
                        }
                        entry.value = message.nbtOptions.getString(key);
                    }
                    ToughAsNails.logger.info("TAN configuration synchronized with the server");
                }
            });
            return null;
        }
    }
}
