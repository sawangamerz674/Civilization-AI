package MineGamer.civilizationai.api.event;

import MineGamer.civilizationai.domain.incident.IncidentType;

import java.util.UUID;

/** Posted by {@code notification.IncidentNotifier} for every incident newly triggered this cycle. */
public class IncidentTriggeredEvent extends CivilizationEvent {

    private final IncidentType incidentType;

    public IncidentTriggeredEvent(UUID civilizationId, IncidentType incidentType) {
        super(civilizationId);
        this.incidentType = incidentType;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }
}
