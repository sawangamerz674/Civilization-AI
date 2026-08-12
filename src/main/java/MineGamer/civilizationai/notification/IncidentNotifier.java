package MineGamer.civilizationai.notification;

import MineGamer.civilizationai.api.event.IncidentTriggeredEvent;
import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.incident.Incident;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;

/**
 * The first real player-facing output this mod produces outside of command
 * replies: when {@code ai.CivilizationBrain} finishes an evaluation cycle,
 * this checks for incidents that were triggered on exactly this cycle's
 * game time and, for each, sends a chat message to players within
 * {@code civilizationActivityRadius} of the civilization's origin, and
 * posts an {@link IncidentTriggeredEvent} for other mods to react to.
 */
public final class IncidentNotifier {

    public void announceNewIncidents(Civilization civilization, CivilizationManager manager, MinecraftServer server, long gameTime) {
        Collection<Incident> active = manager.getActiveIncidents(civilization.getId(), gameTime);
        if (active.isEmpty()) {
            return;
        }

        ServerLevel level = server.getLevel(civilization.getOrigin().dimension());
        if (level == null) {
            return;
        }

        int radius = ModConfig.COMMON.civilizationActivityRadius.get();
        BlockPos origin = civilization.getOrigin().pos();
        double radiusSq = (double) radius * radius;

        for (Incident incident : active) {
            if (incident.triggeredGameTime() != gameTime) {
                continue;
            }

            Component message = Component.translatable(
                    "civilizationai.incident." + incident.type().name().toLowerCase(),
                    civilization.getName());

            for (ServerPlayer player : level.players()) {
                if (player.blockPosition().distSqr(origin) <= radiusSq) {
                    player.sendSystemMessage(message);
                }
            }

            MinecraftForge.EVENT_BUS.post(new IncidentTriggeredEvent(civilization.getId(), incident.type()));
        }
    }
}
