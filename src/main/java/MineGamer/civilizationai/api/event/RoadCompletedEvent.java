package MineGamer.civilizationai.api.event;

import net.minecraft.core.GlobalPos;

import java.util.UUID;

/** Posted by {@code world.ConstructionExecutor} the tick a {@code RoadConstructionSite} completes. */
public class RoadCompletedEvent extends CivilizationEvent {

    private final GlobalPos start;
    private final GlobalPos end;

    public RoadCompletedEvent(UUID civilizationId, GlobalPos start, GlobalPos end) {
        super(civilizationId);
        this.start = start;
        this.end = end;
    }

    public GlobalPos getStart() {
        return start;
    }

    public GlobalPos getEnd() {
        return end;
    }
}
