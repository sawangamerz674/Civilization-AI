package MineGamer.civilizationai.domain.building;

import net.minecraft.core.GlobalPos;

import java.util.List;
import java.util.UUID;

/**
 * A cluster of nearby same-{@link DistrictType} buildings, as computed by
 * {@link DistrictClusterer}. Deliberately not persisted — a district is a
 * derived view over {@link Building}s, not independent state, so there is
 * nothing to save/load and no schema entry for it. Recomputing this from
 * scratch (a handful of buildings per civilization, at most) is cheap
 * enough to do on demand whenever something needs it (a future command, or
 * {@link MineGamer.civilizationai.ai.BuildingPlanner} biasing new same-type sites
 * toward an existing cluster).
 */
public record District(DistrictType districtType, GlobalPos center, List<UUID> buildingIds) {

    public int size() {
        return buildingIds.size();
    }
}
