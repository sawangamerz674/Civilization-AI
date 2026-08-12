package MineGamer.civilizationai.domain.road;

import net.minecraft.core.GlobalPos;

import java.util.UUID;

/**
 * A finished, paved road connecting two points. Created by
 * {@link RoadConstructionSite#onComplete} once its block queue is
 * exhausted. Existence of a {@code RoadSegment} between two points (checked
 * approximately by endpoint proximity) is what stops
 * {@link MineGamer.civilizationai.ai.RoadPlanner} from re-paving the same route.
 */
public record RoadSegment(UUID id, UUID civilizationId, GlobalPos start, GlobalPos end, long completedGameTime) {
}
