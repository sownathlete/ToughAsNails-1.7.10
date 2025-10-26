package toughasnails.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageTemperatureClient implements IMessage {

    public int temperatureTimer;
    public int changeTicks;
    public int targetTemperature;
    public Map<TemperatureDebugger.Modifier, Integer>[] modifiers =
            new LinkedHashMap[TemperatureDebugger.ModifierType.values().length];

    public MessageTemperatureClient() {
        for (int i = 0; i < modifiers.length; i++)
            modifiers[i] = new LinkedHashMap<TemperatureDebugger.Modifier, Integer>();
    }

    public MessageTemperatureClient(int temperatureTimer, int changeTicks, int targetTemperature,
                                    Map<TemperatureDebugger.Modifier, Integer>[] modifiers) {
        this.temperatureTimer = temperatureTimer;
        this.changeTicks = changeTicks;
        this.targetTemperature = targetTemperature;
        this.modifiers = modifiers;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.temperatureTimer = buf.readInt();
        this.changeTicks = buf.readInt();
        this.targetTemperature = buf.readInt();

        for (int mapIdx = 0; mapIdx < this.modifiers.length; ++mapIdx) {
            int size = buf.readInt();
            for (int i = 0; i < size; ++i) {
                // Enum values encoded manually (ordinal)
                int ordinal = buf.readInt();
                TemperatureDebugger.Modifier modifier =
                        TemperatureDebugger.Modifier.values()[ordinal];
                int value = buf.readInt();
                this.modifiers[mapIdx].put(modifier, value);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.temperatureTimer);
        buf.writeInt(this.changeTicks);
        buf.writeInt(this.targetTemperature);

        for (Map<TemperatureDebugger.Modifier, Integer> modifier : this.modifiers) {
            buf.writeInt(modifier.size());
            for (Map.Entry<TemperatureDebugger.Modifier, Integer> entry : modifier.entrySet()) {
                // Encode enum as ordinal
                buf.writeInt(entry.getKey().ordinal());
                buf.writeInt(entry.getValue());
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageTemperatureClient, IMessage> {
        @Override
        public IMessage onMessage(final MessageTemperatureClient message, final MessageContext ctx) {
            final Minecraft mc = Minecraft.getMinecraft();

            // Correct 1.7.10 thread scheduling (client thread)
            mc.func_152344_a(new Runnable() {
                @Override
                public void run() {
                    EntityClientPlayerMP player = mc.thePlayer;
                    if (player != null) {
                        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
                        if (temperatureStats != null) {
                            TemperatureDebugger debugger = temperatureStats.debugger;
                            debugger.temperatureTimer = message.temperatureTimer;
                            debugger.changeTicks = message.changeTicks;
                            debugger.targetTemperature = message.targetTemperature;
                            debugger.modifiers = message.modifiers;
                        }
                    }
                }
            });
            return null;
        }
    }
}
