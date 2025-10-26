package toughasnails.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureHandler;

public class MessageToggleUI implements IMessage {

    public boolean showDebugGUI;

    public MessageToggleUI() {}

    public MessageToggleUI(boolean showDebugGUI) {
        this.showDebugGUI = showDebugGUI;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.showDebugGUI = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.showDebugGUI);
    }

    public static class Handler implements IMessageHandler<MessageToggleUI, IMessage> {

        @Override
        public IMessage onMessage(final MessageToggleUI message, MessageContext ctx) {
            // Run safely on the main client thread (Forge 1.7.10 equivalent of addScheduledTask)
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
                    if (player != null) {
                        // In 1.7.10, TemperatureHandler is typically attached manually or through player data
                        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
                        if (temperatureStats != null && temperatureStats.debugger != null) {
                            TemperatureDebugger debugger = temperatureStats.debugger;
                            debugger.setGuiVisible(message.showDebugGUI);
                        }
                    }
                }
            });
            return null;
        }
    }
}
