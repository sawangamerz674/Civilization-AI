package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.road.RoadConstructionSite;
import MineGamer.civilizationai.domain.road.RoadSegment;
import MineGamer.civilizationai.domain.technology.Technology;
import MineGamer.civilizationai.memory.TravelRoute;
import MineGamer.civilizationai.memory.VillagerMemory;
import MineGamer.civilizationai.world.RoadPathGenerator;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements "frequently traveled routes become permanent": scans every
 * villager's {@link TravelRoute} memories for one used at least
 * {@code roadPavingTrafficThreshold} times that isn't already paved or
 * being paved, and queues its construction.
 * <p>
 * Roads have no resource cost in this phase — unlike buildings, nothing
 * reserves WOOD or STONE for a road. This is a deliberate scope cut: a
 * "real" cost would need a per-block-length cost model this phase doesn't
 * have reason to invent yet, and roads are comparatively cheap/thin
 * structures compared to buildings. A later phase can add one without
 * changing anything here except {@link #evaluate}.
 */
public final class RoadPlanner {

    private static final double MATCH_TOLERANCE_BLOCKS = 4.0;

    public void evaluate(Civilization civilization, CivilizationManager manager, MinecraftServer server, long gameTime) {
        if (!ModConfig.COMMON.roadGenerationEnabled.get()) {
            return;
        }
        if (manager.hasActiveRoadSite(civilization.getId())) {
            return;
        }

        int trafficThreshold = ModConfig.COMMON.roadPavingTrafficThreshold.get();

        for (UUID villagerId : civilization.getVillagerIds()) {
            Optional<VillagerMemory> memory = manager.getMemory(villagerId);
            if (memory.isEmpty()) {
                continue;
            }

            for (TravelRoute route : memory.get().getTravelRoutes()) {
                if (route.getTimesTraveled() < trafficThreshold) {
                    continue;
                }
                if (isAlreadyHandled(civilization, manager, route)) {
                    continue;
                }
                if (tryQueueRoad(civilization, manager, server, route)) {
                    return;
                }
            }
        }
    }

    private boolean tryQueueRoad(Civilization civilization, CivilizationManager manager, MinecraftServer server,
                                  TravelRoute route) {
        ServerLevel level = server.getLevel(route.getStart().dimension());
        if (level == null) {
            return false;
        }

        Optional<List<BlockPlacement>> plan = RoadPathGenerator.generate(
                level, route.getStart(), route.getWaypoints(), route.getEnd(),
                effectiveMaxSlope(civilization, manager), ModConfig.COMMON.roadBridgeDropThreshold.get(),
                "minecraft:dirt_path", "minecraft:cobblestone_stairs", "minecraft:oak_planks");

        if (plan.isEmpty()) {
            // Too steep end-to-end — "roads avoid cliffs" via refusal, not routing around them.
            return false;
        }

        RoadConstructionSite site = new RoadConstructionSite(
                UUID.randomUUID(), civilization.getId(), route.getStart(), route.getEnd(), plan.get());
        manager.addRoadConstructionSite(site);
        return true;
    }

    private boolean isAlreadyHandled(Civilization civilization, CivilizationManager manager, TravelRoute route) {
        for (RoadSegment segment : manager.getRoadSegmentsForCivilization(civilization.getId())) {
            if (endpointsMatch(segment.start(), segment.end(), route.getStart(), route.getEnd())) {
                return true;
            }
        }
        for (RoadConstructionSite site : manager.getAllRoadSites().values()) {
            if (!site.getCivilizationId().equals(civilization.getId())) {
                continue;
            }
            if (endpointsMatch(site.getStart(), site.getEnd(), route.getStart(), route.getEnd())) {
                return true;
            }
        }
        return false;
    }

    private boolean endpointsMatch(GlobalPos a1, GlobalPos a2, GlobalPos b1, GlobalPos b2) {
        if (!a1.dimension().equals(b1.dimension())) {
            return false;
        }
        double toleranceSq = MATCH_TOLERANCE_BLOCKS * MATCH_TOLERANCE_BLOCKS;
        boolean forward = a1.pos().distSqr(b1.pos()) <= toleranceSq && a2.pos().distSqr(b2.pos()) <= toleranceSq;
        boolean reverse = a1.pos().distSqr(b2.pos()) <= toleranceSq && a2.pos().distSqr(b1.pos()) <= toleranceSq;
        return forward || reverse;
    }

    /** STONE_ROADS unlocked raises the accepted slope — "better road engineering" as prosperity grows. */
    private int effectiveMaxSlope(Civilization civilization, CivilizationManager manager) {
        int base = ModConfig.COMMON.roadMaxSlope.get();
        boolean hasStoneRoads = manager.getTechnologyLedger(civilization.getId())
                .map(ledger -> ledger.hasUnlocked(Technology.STONE_ROADS))
                .orElse(false);
        return hasStoneRoads ? base + ModConfig.COMMON.roadMaxSlopeStoneRoadsBonus.get() : base;
    }
}
