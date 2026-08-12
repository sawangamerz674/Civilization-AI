package MineGamer.civilizationai.api.event;

import net.minecraft.core.GlobalPos;

import java.util.UUID;

/** Posted by {@code entity.VillagerRegistrationService} the moment a civilization is auto-founded. */
public class CivilizationCreatedEvent extends CivilizationEvent {

    private final String name;
    private final GlobalPos origin;

    public CivilizationCreatedEvent(UUID civilizationId, String name, GlobalPos origin) {
        super(civilizationId);
        this.name = name;
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public GlobalPos getOrigin() {
        return origin;
    }
}
