package MineGamer.civilizationai.domain.incident;

import java.util.UUID;

/**
 * One occurrence of an {@link IncidentType} affecting a civilization, with
 * a game-time expiry. {@link IncidentTriggerService#expireIncidents} removes
 * these once {@code expiresGameTime} has passed.
 */
public record Incident(UUID id, UUID civilizationId, IncidentType type, long triggeredGameTime, long expiresGameTime) {

    public boolean isActive(long gameTime) {
        return gameTime < expiresGameTime;
    }
}
