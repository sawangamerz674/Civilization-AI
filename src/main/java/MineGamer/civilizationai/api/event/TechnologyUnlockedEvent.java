package MineGamer.civilizationai.api.event;

import MineGamer.civilizationai.domain.technology.Technology;

import java.util.UUID;

/** Posted by {@code ai.CivilizationBrain} the cycle a civilization's technology tier advances. */
public class TechnologyUnlockedEvent extends CivilizationEvent {

    private final Technology tier;

    public TechnologyUnlockedEvent(UUID civilizationId, Technology tier) {
        super(civilizationId);
        this.tier = tier;
    }

    public Technology getTier() {
        return tier;
    }
}
