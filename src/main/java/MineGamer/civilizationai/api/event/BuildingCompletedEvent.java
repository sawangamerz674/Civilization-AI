package MineGamer.civilizationai.api.event;

import MineGamer.civilizationai.domain.building.BuildingType;
import net.minecraft.core.GlobalPos;

import java.util.UUID;

/** Posted by {@code world.ConstructionExecutor} the tick a {@code BuildingConstructionSite} completes. */
public class BuildingCompletedEvent extends CivilizationEvent {

    private final BuildingType buildingType;
    private final GlobalPos origin;

    public BuildingCompletedEvent(UUID civilizationId, BuildingType buildingType, GlobalPos origin) {
        super(civilizationId);
        this.buildingType = buildingType;
        this.origin = origin;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public GlobalPos getOrigin() {
        return origin;
    }
}
