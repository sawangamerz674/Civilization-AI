package MineGamer.civilizationai.api.event;

import java.util.UUID;

/** Posted by {@code entity.VillagerRegistrationService} on join and by {@code ai.MigrationService} on migration. */
public class VillagerRegisteredEvent extends CivilizationEvent {

    private final UUID villagerId;

    public VillagerRegisteredEvent(UUID civilizationId, UUID villagerId) {
        super(civilizationId);
        this.villagerId = villagerId;
    }

    public UUID getVillagerId() {
        return villagerId;
    }
}
