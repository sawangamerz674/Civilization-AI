package MineGamer.civilizationai.network;

import MineGamer.civilizationai.network.packets.PingPacket;
import MineGamer.civilizationai.util.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Owns the mod's single {@link SimpleChannel} and the registration table of
 * every packet the mod sends. Later phases (civilization sync, villager
 * memory sync, UI packets for a future civilization overview screen, etc.)
 * register their packets here, in {@link #register()}, with the next free
 * index — never inline elsewhere.
 */
public final class NetworkHandler {

    private static final ResourceLocation CHANNEL_ID =
            new ResourceLocation(Constants.MOD_ID, "main");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_ID,
            () -> Constants.NETWORK_PROTOCOL_VERSION,
            Constants.NETWORK_PROTOCOL_VERSION::equals,
            Constants.NETWORK_PROTOCOL_VERSION::equals
    );

    private static int nextPacketId = 0;

    private NetworkHandler() {
    }

    private static int nextId() {
        return nextPacketId++;
    }

    /**
     * Registers every packet type. Must be called once from
     * {@code FMLCommonSetupEvent}, on the setup thread.
     */
    public static void register() {
        CHANNEL.registerMessage(
                nextId(),
                PingPacket.class,
                PingPacket::encode,
                PingPacket::decode,
                PingPacket::handle
        );
    }
}
