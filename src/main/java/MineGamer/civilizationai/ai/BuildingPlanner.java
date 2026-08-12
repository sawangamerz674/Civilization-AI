package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.VillagerProfile;
import MineGamer.civilizationai.domain.building.Building;
import MineGamer.civilizationai.domain.building.BuildingBlueprintGenerator;
import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.building.BuildingType;
import MineGamer.civilizationai.domain.building.District;
import MineGamer.civilizationai.domain.building.DistrictClusterer;
import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import MineGamer.civilizationai.world.TerrainAnalyzer;
import MineGamer.civilizationai.domain.building.TerrainSurvey;
import MineGamer.civilizationai.world.SiteSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Decides whether a civilization should start a new building, and if so,
 * sites it, reserves its resource cost, and queues its construction.
 * <p>
 * Only ever runs one construction at a time per civilization (see
 * {@link CivilizationManager#hasActiveBuildingSite}) — same "gradual, not
 * instant" pacing philosophy as {@link VillageGovernor} picking a single
 * need per cycle. Demand is checked in a fixed priority order: HOUSE
 * (population pressure) first, then WAREHOUSE (stock pressure), then the
 * five profession-triggered specialty buildings — and for now, at most one
 * of each specialty building per civilization, a deliberate simplification
 * (see {@link BuildingType}'s Javadoc).
 */
public final class BuildingPlanner {

    private static final List<BuildingType> SPECIALTY_TYPES = List.of(
            BuildingType.WORKSHOP, BuildingType.GUARD_TOWER, BuildingType.FARM,
            BuildingType.TEMPLE, BuildingType.SCHOOL
    );

    public void evaluate(Civilization civilization, CivilizationManager manager, MinecraftServer server,
                          long gameTime) {
        if (!ModConfig.COMMON.buildingEnabled.get()) {
            return;
        }
        if (manager.hasActiveBuildingSite(civilization.getId())) {
            return;
        }

        Optional<BuildingType> demand = determineDemand(civilization, manager);
        if (demand.isEmpty()) {
            return;
        }
        BuildingType type = demand.get();

        ServerLevel level = server.getLevel(civilization.getOrigin().dimension());
        if (level == null) {
            return;
        }

        if (!reserveCost(manager, civilization.getId(), type.getResourceCost())) {
            return;
        }

        BlockPos searchCenter = chooseSearchCenter(civilization, manager, type);
        int searchRadius = ModConfig.COMMON.buildingSiteSearchRadius.get();
        int maxVariance = ModConfig.COMMON.buildingSiteMaxVariance.get();
        int buffer = ModConfig.COMMON.buildingSiteBuffer.get();

        Optional<BlockPos> site = SiteSelector.findSite(level, searchCenter, type.getWidth(), type.getDepth(),
                searchRadius, maxVariance, candidate -> isUnoccupied(candidate, civilization, manager, buffer));

        if (site.isEmpty()) {
            releaseCost(manager, civilization.getId(), type.getResourceCost());
            return;
        }

        TerrainSurvey survey = TerrainAnalyzer.survey(level, site.get(), type.getWidth(), type.getDepth());
        List<BlockPlacement> plan = BuildingBlueprintGenerator.generate(type, survey);
        GlobalPos origin = GlobalPos.of(civilization.getOrigin().dimension(),
                new BlockPos(site.get().getX(), survey.flattenHeight(), site.get().getZ()));

        BuildingConstructionSite constructionSite = new BuildingConstructionSite(
                UUID.randomUUID(), civilization.getId(), type, origin, type.getResourceCost(), plan, gameTime);
        manager.addBuildingConstructionSite(constructionSite);
    }

    private Optional<BuildingType> determineDemand(Civilization civilization, CivilizationManager manager) {
        int population = civilization.getPopulation();
        int houseCount = manager.countBuildingsByType(civilization.getId(), BuildingType.HOUSE);
        int capacityPerHouse = ModConfig.COMMON.housingCapacityPerHouse.get();
        if (population > houseCount * capacityPerHouse) {
            return Optional.of(BuildingType.HOUSE);
        }

        ResourceLedger stock = manager.getOrCreateResourceLedger(civilization.getId());
        long totalStock = 0;
        for (ResourceType type : ResourceType.values()) {
            totalStock += stock.getStock(type);
        }
        int warehouseCount = manager.countBuildingsByType(civilization.getId(), BuildingType.WAREHOUSE);
        long warehouseThreshold = ModConfig.COMMON.warehouseTriggerStock.get();
        if (totalStock > warehouseThreshold * (warehouseCount + 1)) {
            return Optional.of(BuildingType.WAREHOUSE);
        }

        for (BuildingType type : SPECIALTY_TYPES) {
            Profession trigger = type.getTriggerProfession();
            if (trigger == null || manager.countBuildingsByType(civilization.getId(), type) > 0) {
                continue;
            }
            if (countByProfession(civilization, manager, trigger) >= 1) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

    private int countByProfession(Civilization civilization, CivilizationManager manager, Profession profession) {
        int count = 0;
        for (UUID villagerId : civilization.getVillagerIds()) {
            VillagerProfile profile = manager.getProfile(villagerId).orElse(null);
            if (profile != null && profile.getProfession() == profession) {
                count++;
            }
        }
        return count;
    }

    /** Bias siting toward the centroid of an existing same-district cluster, if one exists, reinforcing emergent districts. */
    private BlockPos chooseSearchCenter(Civilization civilization, CivilizationManager manager, BuildingType type) {
        List<Building> sameDistrict = manager.getBuildingsForCivilization(civilization.getId()).stream()
                .filter(b -> b.type().getDistrictType() == type.getDistrictType())
                .toList();
        if (sameDistrict.isEmpty()) {
            return civilization.getOrigin().pos();
        }
        double clusterRadius = ModConfig.COMMON.districtClusterRadius.get();
        List<District> districts = DistrictClusterer.cluster(sameDistrict, clusterRadius);
        District largest = districts.stream().max(Comparator.comparingInt(District::size)).orElse(null);
        return largest != null ? largest.center().pos() : civilization.getOrigin().pos();
    }

    private boolean isUnoccupied(BlockPos candidate, Civilization civilization, CivilizationManager manager, int buffer) {
        for (Building building : manager.getBuildingsForCivilization(civilization.getId())) {
            double minDistance = Math.max(building.type().getWidth(), building.type().getDepth()) + buffer;
            if (candidate.distSqr(building.origin().pos()) < minDistance * minDistance) {
                return false;
            }
        }
        for (BuildingConstructionSite site : manager.getAllBuildingSites().values()) {
            if (!site.getCivilizationId().equals(civilization.getId())) {
                continue;
            }
            double minDistance = Math.max(site.getBuildingType().getWidth(), site.getBuildingType().getDepth()) + buffer;
            if (candidate.distSqr(site.getOrigin().pos()) < minDistance * minDistance) {
                return false;
            }
        }
        return true;
    }

    private boolean reserveCost(CivilizationManager manager, UUID civilizationId, Map<ResourceType, Long> cost) {
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilizationId);
        for (Map.Entry<ResourceType, Long> entry : cost.entrySet()) {
            if (stock.getAvailable(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        for (Map.Entry<ResourceType, Long> entry : cost.entrySet()) {
            stock.reserve(entry.getKey(), entry.getValue());
        }
        manager.markDirty();
        return true;
    }

    private void releaseCost(CivilizationManager manager, UUID civilizationId, Map<ResourceType, Long> cost) {
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilizationId);
        for (Map.Entry<ResourceType, Long> entry : cost.entrySet()) {
            stock.releaseReservation(entry.getKey(), entry.getValue());
        }
        manager.markDirty();
    }
}
