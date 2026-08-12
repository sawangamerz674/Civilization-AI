package MineGamer.civilizationai.network.packets;

import MineGamer.civilizationai.util.ModLogger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Minimal client-to-server packet carrying a single timestamp. Its only
 * purpose in Phase 1 is to prove {@link MineGamer.civilizationai.network.NetworkHandler}
 * is wired correctly end-to-end; it is not part of the simulation.
 * <p>
 * Later phases follow this exact shape (encode/decode/handle as static
 * methods, immutable record-like fields, work scheduled onto the network
 * thread's enqueued work queue) for every real gameplay packet.
 */
public class PingPacket {

    private static final Logger LOGGER = ModLogger.get("Network");

    private final long clientTimeMillis;

    public PingPacket(long clientTimeMillis) {
        this.clientTimeMillis = clientTimeMillis;
    }

    public static void encode(PingPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.clientTimeMillis);
    }

    public static PingPacket decode(FriendlyByteBuf buffer) {
        return new PingPacket(buffer.readLong());
    }

    public static void handle(PingPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            long roundTripMillis = System.currentTimeMillis() - packet.clientTimeMillis;
            LOGGER.debug("Received PingPacket, round trip {} ms.", roundTripMillis);
        });
        context.setPacketHandled(true);
    }
}
