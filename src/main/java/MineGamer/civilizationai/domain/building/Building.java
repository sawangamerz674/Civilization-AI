package MineGamer.civilizationai.domain.building;

import net.minecraft.core.GlobalPos;

import java.util.UUID;

/**
 * A finished building. Created by {@link BuildingConstructionSite#onComplete}
 * once its block queue is exhausted — see that class for the in-progress
 * counterpart.
 */
public record Building(UUID id, UUID civilizationId, BuildingType type, GlobalPos origin, long completedGameTime) {
}
