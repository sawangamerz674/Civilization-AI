package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.VillagerProfile;
import MineGamer.civilizationai.domain.building.Building;
import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.incident.Incident;
import MineGamer.civilizationai.domain.reputation.ReputationLedger;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.road.RoadConstructionSite;
import MineGamer.civilizationai.domain.road.RoadSegment;
import MineGamer.civilizationai.domain.technology.TechnologyLedger;
import MineGamer.civilizationai.memory.VillagerMemory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level (de)serializer for a whole {@link CivilizationManager}. Writes
 * twelve parallel lists into a parent tag; {@link MineGamer.civilizationai.save.CivilizationSavedData}
 * owns that parent tag and calls this once per save/load.
 */
public final class CivilizationManagerSerializer {

    private static final String KEY_CIVILIZATIONS = "Civilizations";
    private static final String KEY_PROFILES = "VillagerProfiles";
    private static final String KEY_MEMORIES = "VillagerMemories";
    private static final String KEY_RESOURCE_LEDGERS = "ResourceLedgers";
    private static final String KEY_ECONOMY_LEDGERS = "EconomyLedgers";
    private static final String KEY_BUILDINGS = "Buildings";
    private static final String KEY_BUILDING_SITES = "BuildingConstructionSites";
    private static final String KEY_ROAD_SEGMENTS = "RoadSegments";
    private static final String KEY_ROAD_SITES = "RoadConstructionSites";
    private static final String KEY_REPUTATION_LEDGERS = "ReputationLedgers";
    private static final String KEY_INCIDENTS = "Incidents";
    private static final String KEY_TECHNOLOGY_LEDGERS = "TechnologyLedgers";

    private CivilizationManagerSerializer() {
    }

    public static void write(CivilizationManager manager, CompoundTag tag) {
        ListTag civilizationsList = new ListTag();
        for (Civilization civilization : manager.getAllCivilizations()) {
            civilizationsList.add(CivilizationSerializer.write(civilization));
        }
        tag.put(KEY_CIVILIZATIONS, civilizationsList);

        ListTag profilesList = new ListTag();
        for (VillagerProfile profile : manager.getAllProfiles().values()) {
            profilesList.add(VillagerProfileSerializer.write(profile));
        }
        tag.put(KEY_PROFILES, profilesList);

        ListTag memoriesList = new ListTag();
        for (VillagerMemory memory : manager.getAllMemories().values()) {
            memoriesList.add(VillagerMemorySerializer.write(memory));
        }
        tag.put(KEY_MEMORIES, memoriesList);

        ListTag resourceLedgersList = new ListTag();
        for (ResourceLedger ledger : manager.getAllResourceLedgers().values()) {
            resourceLedgersList.add(ResourceLedgerSerializer.write(ledger));
        }
        tag.put(KEY_RESOURCE_LEDGERS, resourceLedgersList);

        ListTag economyLedgersList = new ListTag();
        for (EconomyLedger ledger : manager.getAllEconomyLedgers().values()) {
            economyLedgersList.add(EconomyLedgerSerializer.write(ledger));
        }
        tag.put(KEY_ECONOMY_LEDGERS, economyLedgersList);

        ListTag buildingsList = new ListTag();
        for (Building building : manager.getAllBuildings().values()) {
            buildingsList.add(BuildingSerializer.write(building));
        }
        tag.put(KEY_BUILDINGS, buildingsList);

        ListTag buildingSitesList = new ListTag();
        for (BuildingConstructionSite site : manager.getAllBuildingSites().values()) {
            buildingSitesList.add(BuildingConstructionSiteSerializer.write(site));
        }
        tag.put(KEY_BUILDING_SITES, buildingSitesList);

        ListTag roadSegmentsList = new ListTag();
        for (RoadSegment segment : manager.getAllRoadSegments().values()) {
            roadSegmentsList.add(RoadSegmentSerializer.write(segment));
        }
        tag.put(KEY_ROAD_SEGMENTS, roadSegmentsList);

        ListTag roadSitesList = new ListTag();
        for (RoadConstructionSite site : manager.getAllRoadSites().values()) {
            roadSitesList.add(RoadConstructionSiteSerializer.write(site));
        }
        tag.put(KEY_ROAD_SITES, roadSitesList);

        ListTag reputationLedgersList = new ListTag();
        for (ReputationLedger ledger : manager.getAllReputationLedgers().values()) {
            reputationLedgersList.add(ReputationLedgerSerializer.write(ledger));
        }
        tag.put(KEY_REPUTATION_LEDGERS, reputationLedgersList);

        ListTag incidentsList = new ListTag();
        for (Incident incident : manager.getAllIncidents().values()) {
            incidentsList.add(IncidentSerializer.write(incident));
        }
        tag.put(KEY_INCIDENTS, incidentsList);

        ListTag technologyLedgersList = new ListTag();
        for (TechnologyLedger ledger : manager.getAllTechnologyLedgers().values()) {
            technologyLedgersList.add(TechnologyLedgerSerializer.write(ledger));
        }
        tag.put(KEY_TECHNOLOGY_LEDGERS, technologyLedgersList);
    }

    public static CivilizationManager read(CompoundTag tag, Runnable dirtyMarker) {
        CivilizationManager manager = new CivilizationManager(dirtyMarker);

        List<Civilization> civilizations = new ArrayList<>();
        ListTag civilizationsList = tag.getList(KEY_CIVILIZATIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < civilizationsList.size(); i++) {
            civilizations.add(CivilizationSerializer.read(civilizationsList.getCompound(i)));
        }

        List<VillagerProfile> profiles = new ArrayList<>();
        ListTag profilesList = tag.getList(KEY_PROFILES, Tag.TAG_COMPOUND);
        for (int i = 0; i < profilesList.size(); i++) {
            profiles.add(VillagerProfileSerializer.read(profilesList.getCompound(i)));
        }

        List<VillagerMemory> memories = new ArrayList<>();
        ListTag memoriesList = tag.getList(KEY_MEMORIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < memoriesList.size(); i++) {
            memories.add(VillagerMemorySerializer.read(memoriesList.getCompound(i)));
        }

        List<ResourceLedger> resourceLedgers = new ArrayList<>();
        ListTag resourceLedgersList = tag.getList(KEY_RESOURCE_LEDGERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < resourceLedgersList.size(); i++) {
            resourceLedgers.add(ResourceLedgerSerializer.read(resourceLedgersList.getCompound(i)));
        }

        List<EconomyLedger> economyLedgers = new ArrayList<>();
        ListTag economyLedgersList = tag.getList(KEY_ECONOMY_LEDGERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < economyLedgersList.size(); i++) {
            economyLedgers.add(EconomyLedgerSerializer.read(economyLedgersList.getCompound(i)));
        }

        List<Building> buildings = new ArrayList<>();
        ListTag buildingsList = tag.getList(KEY_BUILDINGS, Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingsList.size(); i++) {
            buildings.add(BuildingSerializer.read(buildingsList.getCompound(i)));
        }

        List<BuildingConstructionSite> buildingSites = new ArrayList<>();
        ListTag buildingSitesList = tag.getList(KEY_BUILDING_SITES, Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingSitesList.size(); i++) {
            buildingSites.add(BuildingConstructionSiteSerializer.read(buildingSitesList.getCompound(i)));
        }

        List<RoadSegment> roadSegments = new ArrayList<>();
        ListTag roadSegmentsList = tag.getList(KEY_ROAD_SEGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < roadSegmentsList.size(); i++) {
            roadSegments.add(RoadSegmentSerializer.read(roadSegmentsList.getCompound(i)));
        }

        List<RoadConstructionSite> roadSites = new ArrayList<>();
        ListTag roadSitesList = tag.getList(KEY_ROAD_SITES, Tag.TAG_COMPOUND);
        for (int i = 0; i < roadSitesList.size(); i++) {
            roadSites.add(RoadConstructionSiteSerializer.read(roadSitesList.getCompound(i)));
        }

        List<ReputationLedger> reputationLedgers = new ArrayList<>();
        ListTag reputationLedgersList = tag.getList(KEY_REPUTATION_LEDGERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < reputationLedgersList.size(); i++) {
            reputationLedgers.add(ReputationLedgerSerializer.read(reputationLedgersList.getCompound(i)));
        }

        List<Incident> incidents = new ArrayList<>();
        ListTag incidentsList = tag.getList(KEY_INCIDENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < incidentsList.size(); i++) {
            incidents.add(IncidentSerializer.read(incidentsList.getCompound(i)));
        }

        List<TechnologyLedger> technologyLedgers = new ArrayList<>();
        ListTag technologyLedgersList = tag.getList(KEY_TECHNOLOGY_LEDGERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < technologyLedgersList.size(); i++) {
            technologyLedgers.add(TechnologyLedgerSerializer.read(technologyLedgersList.getCompound(i)));
        }

        manager.restore(civilizations, profiles, memories, resourceLedgers, economyLedgers,
                buildings, buildingSites, roadSegments, roadSites,
                reputationLedgers, incidents, technologyLedgers);
        return manager;
    }
}
