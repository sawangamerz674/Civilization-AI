package MineGamer.civilizationai.domain.building;

import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.construction.ConstructionJob;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import net.minecraft.core.GlobalPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A building under construction: a queue of {@link BlockPlacement}s left to
 * place, and the resource cost that was reserved (via
 * {@link ResourceLedger#reserve}) when this site was created by
 * {@link MineGamer.civilizationai.ai.BuildingPlanner}.
 * <p>
 * On completion, the reservation is committed (actually consumed from
 * stock) and a {@link Building} is registered — see {@link #onComplete}.
 */
public final class BuildingConstructionSite implements ConstructionJob {

    private final UUID id;
    private final UUID civilizationId;
    private final BuildingType buildingType;
    private final GlobalPos origin;
    private final Map<ResourceType, Long> reservedCost;
    private final Deque<BlockPlacement> queue;
    private final long startedGameTime;

    public BuildingConstructionSite(UUID id, UUID civilizationId, BuildingType buildingType, GlobalPos origin,
                                     Map<ResourceType, Long> reservedCost, List<BlockPlacement> plan,
                                     long startedGameTime) {
        this.id = id;
        this.civilizationId = civilizationId;
        this.buildingType = buildingType;
        this.origin = origin;
        this.reservedCost = reservedCost;
        this.queue = new ArrayDeque<>(plan);
        this.startedGameTime = startedGameTime;
    }

    /** Reconstruction used by the serializer — {@code remainingPlan} is whatever hadn't been placed yet. */
    public static BuildingConstructionSite reconstruct(UUID id, UUID civilizationId, BuildingType buildingType,
                                                         GlobalPos origin, Map<ResourceType, Long> reservedCost,
                                                         List<BlockPlacement> remainingPlan, long startedGameTime) {
        return new BuildingConstructionSite(id, civilizationId, buildingType, origin, reservedCost, remainingPlan, startedGameTime);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getCivilizationId() {
        return civilizationId;
    }

    @Override
    public GlobalPos getOrigin() {
        return origin;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public long getStartedGameTime() {
        return startedGameTime;
    }

    /** The plan as it currently stands — whatever hasn't been placed yet, in order. Used by the serializer. */
    public List<BlockPlacement> getRemainingPlan() {
        return List.copyOf(queue);
    }

    public Map<ResourceType, Long> getReservedCost() {
        return reservedCost;
    }

    @Override
    public boolean isComplete() {
        return queue.isEmpty();
    }

    @Override
    public List<BlockPlacement> takeNextBatch(int maxCount) {
        List<BlockPlacement> batch = new ArrayList<>(Math.min(maxCount, queue.size()));
        for (int i = 0; i < maxCount && !queue.isEmpty(); i++) {
            batch.add(queue.pollFirst());
        }
        return batch;
    }

    @Override
    public void onComplete(CivilizationManager manager, long gameTime) {
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilizationId);
        for (Map.Entry<ResourceType, Long> entry : reservedCost.entrySet()) {
            stock.commitReservation(entry.getKey(), entry.getValue());
        }
        manager.registerBuilding(new Building(UUID.randomUUID(), civilizationId, buildingType, origin, gameTime));
    }
}
