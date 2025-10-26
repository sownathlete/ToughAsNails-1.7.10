package toughasnails.handler;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import toughasnails.network.message.MessageSyncConfigs;
import toughasnails.network.message.MessageSyncSeasonCycle;
import toughasnails.network.message.MessageTemperatureClient;
import toughasnails.network.message.MessageToggleUI;
import toughasnails.network.message.MessageUpdateStat;

/**
 * Handles all Tough As Nails network packet registration (Forge 1.7.10 version).
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper instance =
            NetworkRegistry.INSTANCE.newSimpleChannel("ToughAsNails");

    public static void init() {
        instance.registerMessage(MessageUpdateStat.Handler.class, MessageUpdateStat.class, 0, Side.CLIENT);
        instance.registerMessage(MessageTemperatureClient.Handler.class, MessageTemperatureClient.class, 1, Side.CLIENT);
        instance.registerMessage(MessageToggleUI.Handler.class, MessageToggleUI.class, 2, Side.CLIENT);
        instance.registerMessage(MessageSyncSeasonCycle.Handler.class, MessageSyncSeasonCycle.class, 3, Side.CLIENT);
        instance.registerMessage(MessageSyncConfigs.Handler.class, MessageSyncConfigs.class, 4, Side.CLIENT);
    }
}
