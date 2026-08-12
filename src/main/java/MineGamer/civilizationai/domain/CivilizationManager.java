package MineGamer.civilizationai.domain;

import MineGamer.civilizationai.domain.building.Building;
import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.building.BuildingType;
import MineGamer.civilizationai.domain.construction.ConstructionJob;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.incident.Incident;
import MineGamer.civilizationai.domain.incident.IncidentType;
import MineGamer.civilizationai.domain.reputation.ReputationLedger;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.road.RoadConstructionSite;
import MineGamer.civilizationai.domain.road.RoadSegment;
import MineGamer.civilizationai.domain.technology.TechnologyLedger;
import MineGamer.civilizationai.memory.VillagerMemory;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns every {@link Civilization}, {@link VillagerProfile},
 * {@link VillagerMemory}, {@link ResourceLedger}, {@link EconomyLedger},
 * {@link Building}/{@link BuildingConstructionSite},
 * {@link RoadSegment}/{@link RoadConstructionSite},
 * {@link MineGamer.civilizationai.domain.reputation.ReputationLedger},
 * {@link MineGamer.civilizationai.domain.incident.Incident}, and
 * {@link MineGamer.civilizationai.domain.technology.TechnologyLedger} for a level,
 * and every method that mutates them.
 * <p>
 * Deliberately has no dependency on {@link net.minecraft.world.level.saveddata.SavedData}
 * or NBT — it is plain Java, constructible and testable without a running
 * game instance. {@link MineGamer.civilizationai.save.CivilizationSavedData} is
 * the only class that wires this up to Minecraft's persistence, via the
 * {@code dirtyMarker} callback supplied at construction (normally
 * {@code CivilizationSavedData::setDirty}).
 */
public final class CivilizationManager {

    private final Map<UUID, Civilization> civilizations = new HashMap<>();
    private final Map<UUID, VillagerProfile> villagerProfiles = new HashMap<>();
    private final Map<UUID, VillagerMemory> villagerMemories = new HashMap<>();
    private final Map<UUID, ResourceLedger> resourceLedgers = new HashMap<>();
    private final Map<UUID, EconomyLedger> economyLedgers = new HashMap<>();
    private final Map<UUID, Building> buildings = new HashMap<>();
    private final Map<UUID, BuildingConstructionSite> buildingSites = new HashMap<>();
    private final Map<UUID, RoadSegment> roadSegments = new HashMap<>();
    private final Map<UUID, RoadConstructionSite> roadSites = new HashMap<>();
    private final Map<UUID, ReputationLedger> reputationLedgers = new HashMap<>();
    private final Map<UUID, Incident> incidents = new HashMap<>();
    private final Map<UUID, TechnologyLedger> technologyLedgers = new HashMap<>();
    private final Runnable dirtyMarker;

    public CivilizationManager(Runnable dirtyMarker) {
        this.dirtyMarker = dirtyMarker;
    }

    /** Used by the serializer to repopulate a freshly constructed manager on load. */
    public void restore(Collection<Civilization> loadedCivilizations,
                         Collection<VillagerProfile> loadedProfiles,
                         Collection<VillagerMemory> loadedMemories,
                         Collection<ResourceLedger> loadedResourceLedgers,
                         Collection<EconomyLedger> loadedEconomyLedgers,
                         Collection<Building> loadedBuildings,
                         Collection<BuildingConstructionSite> loadedBuildingSites,
                         Collection<RoadSegment> loadedRoadSegments,
                         Collection<RoadConstructionSite> loadedRoadSites,
                         Collection<ReputationLedger> loadedReputationLedgers,
                         Collection<Incident> loadedIncidents,
                         Collection<TechnologyLedger> loadedTechnologyLedgers) {
        for (Civilization civilization : loadedCivilizations) {
            civilizations.put(civilization.getId(), civilization);
        }
        for (VillagerProfile profile : loadedProfiles) {
            villagerProfiles.put(profile.getVillagerId(), profile);
        }
        for (VillagerMemory memory : loadedMemories) {
            villagerMemories.put(memory.getVillagerId(), memory);
        }
        for (ResourceLedger ledger : loadedResourceLedgers) {
            resourceLedgers.put(ledger.getCivilizationId(), ledger);
        }
        for (EconomyLedger ledger : loadedEconomyLedgers) {
            economyLedgers.put(ledger.getCivilizationId(), ledger);
        }
        for (Building building : loadedBuildings) {
            buildings.put(building.id(), building);
        }
        for (BuildingConstructionSite site : loadedBuildingSites) {
            buildingSites.put(site.getId(), site);
        }
        for (RoadSegment segment : loadedRoadSegments) {
            roadSegments.put(segment.id(), segment);
        }
        for (RoadConstructionSite site : loadedRoadSites) {
            roadSites.put(site.getId(), site);
        }
        for (ReputationLedger ledger : loadedReputationLedgers) {
            reputationLedgers.put(ledger.getCivilizationId(), ledger);
        }
        for (Incident incident : loadedIncidents) {
            incidents.put(incident.id(), incident);
        }
        for (TechnologyLedger ledger : loadedTechnologyLedgers) {
            technologyLedgers.put(ledger.getCivilizationId(), ledger);
        }
    }

    // --- Civilizations ---

    public Civilization createCivilization(String name, GlobalPos origin, long gameTime) {
        UUID id = UUID.randomUUID();
        Civilization civilization = new Civilization(id, name, origin, gameTime);
        civilizations.put(id, civilization);
        dirtyMarker.run();
        return civilization;
    }

    public Optional<Civilization> getCivilization(UUID civilizationId) {
        return Optional.ofNullable(civilizations.get(civilizationId));
    }

    public Collection<Civilization> getAllCivilizations() {
        return List.copyOf(civilizations.values());
    }

    public Collection<Civilization> getCivilizationsInDimension(ResourceKey<Level> dimension) {
        return civilizations.values().stream()
                .filter(c -> c.getOrigin().dimension().equals(dimension))
                .toList();
    }

    public boolean removeCivilization(UUID civilizationId) {
        Civilization removed = civilizations.remove(civilizationId);
        if (removed == null) {
            return false;
        }
        for (UUID villagerId : removed.getVillagerIds()) {
            VillagerProfile profile = villagerProfiles.get(villagerId);
            if (profile != null) {
                profile.setCivilizationId(null);
            }
        }
        resourceLedgers.remove(civilizationId);
        economyLedgers.remove(civilizationId);
        buildings.values().removeIf(b -> b.civilizationId().equals(civilizationId));
        buildingSites.values().removeIf(s -> s.getCivilizationId().equals(civilizationId));
        roadSegments.values().removeIf(r -> r.civilizationId().equals(civilizationId));
        roadSites.values().removeIf(s -> s.getCivilizationId().equals(civilizationId));
        reputationLedgers.remove(civilizationId);
        incidents.values().removeIf(i -> i.civilizationId().equals(civilizationId));
        technologyLedgers.remove(civilizationId);
        dirtyMarker.run();
        return true;
    }

    // --- Villager profiles & memory ---

    /**
     * Fetches the profile for a villager, creating one with freshly rolled
     * personality traits (and an empty memory) if this is the first time
     * this villager UUID has been seen. Never returns null.
     */
    public VillagerProfile getOrCreateProfile(UUID villagerId, long gameTime, RandomSource random) {
        VillagerProfile existing = villagerProfiles.get(villagerId);
        if (existing != null) {
            return existing;
        }
        PersonalityProfile personality = PersonalityGenerator.generate(random);
        VillagerProfile profile = new VillagerProfile(villagerId, null, personality, gameTime);
        villagerProfiles.put(villagerId, profile);
        villagerMemories.computeIfAbsent(villagerId, VillagerMemory::new);
        dirtyMarker.run();
        return profile;
    }

    public Optional<VillagerProfile> getProfile(UUID villagerId) {
        return Optional.ofNullable(villagerProfiles.get(villagerId));
    }

    /**
     * Changes a villager's job. Returns false if no profile exists for that
     * UUID (e.g. it was never registered) rather than throwing, since
     * department managers call this speculatively during needs evaluation.
     */
    public boolean assignProfession(UUID villagerId, Profession profession) {
        VillagerProfile profile = villagerProfiles.get(villagerId);
        if (profile == null) {
            return false;
        }
        profile.setProfession(profession);
        dirtyMarker.run();
        return true;
    }

    public VillagerMemory getOrCreateMemory(UUID villagerId) {
        VillagerMemory memory = villagerMemories.computeIfAbsent(villagerId, VillagerMemory::new);
        dirtyMarker.run();
        return memory;
    }

    public Optional<VillagerMemory> getMemory(UUID villagerId) {
        return Optional.ofNullable(villagerMemories.get(villagerId));
    }

    /**
     * Assigns a villager (creating its profile/memory if needed) to a
     * civilization, removing it from any previous civilization first.
     */
    public void registerVillager(UUID villagerId, UUID civilizationId, long gameTime, RandomSource random) {
        Civilization target = civilizations.get(civilizationId);
        if (target == null) {
            throw new IllegalArgumentException("No civilization with id " + civilizationId);
        }
        VillagerProfile profile = getOrCreateProfile(villagerId, gameTime, random);

        UUID previousCivilizationId = profile.getCivilizationId();
        if (previousCivilizationId != null && !previousCivilizationId.equals(civilizationId)) {
            Civilization previous = civilizations.get(previousCivilizationId);
            if (previous != null) {
                previous.removeVillager(villagerId);
            }
        }

        target.addVillager(villagerId);
        profile.setCivilizationId(civilizationId);
        dirtyMarker.run();
    }

    /** Removes a villager from its civilization, if any. Profile and memory are kept. */
    public void unregisterVillager(UUID villagerId) {
        VillagerProfile profile = villagerProfiles.get(villagerId);
        if (profile == null || profile.getCivilizationId() == null) {
            return;
        }
        Civilization civilization = civilizations.get(profile.getCivilizationId());
        if (civilization != null) {
            civilization.removeVillager(villagerId);
        }
        profile.setCivilizationId(null);
        dirtyMarker.run();
    }

    public Map<UUID, VillagerProfile> getAllProfiles() {
        return Map.copyOf(villagerProfiles);
    }

    public Map<UUID, VillagerMemory> getAllMemories() {
        return Map.copyOf(villagerMemories);
    }

    // --- Resources & economy ---

    /** Fetches a civilization's resource ledger, creating an empty one on first access. Never returns null. */
    public ResourceLedger getOrCreateResourceLedger(UUID civilizationId) {
        return resourceLedgers.computeIfAbsent(civilizationId, id -> {
            dirtyMarker.run();
            return new ResourceLedger(id);
        });
    }

    public Optional<ResourceLedger> getResourceLedger(UUID civilizationId) {
        return Optional.ofNullable(resourceLedgers.get(civilizationId));
    }

    /** Fetches a civilization's economy ledger, creating one (all multipliers neutral) on first access. */
    public EconomyLedger getOrCreateEconomyLedger(UUID civilizationId) {
        return economyLedgers.computeIfAbsent(civilizationId, id -> {
            dirtyMarker.run();
            return new EconomyLedger(id);
        });
    }

    public Optional<EconomyLedger> getEconomyLedger(UUID civilizationId) {
        return Optional.ofNullable(economyLedgers.get(civilizationId));
    }

    /**
     * Call after mutating a {@link ResourceLedger} or {@link EconomyLedger}
     * obtained from this manager — both are mutable value holders returned
     * by reference (unlike the defensive copies {@link #getAllProfiles()}
     * etc. return), so mutation doesn't automatically flag the save dirty
     * the way every other method here does. Cheap and idempotent to call.
     */
    public void markDirty() {
        dirtyMarker.run();
    }

    public Map<UUID, ResourceLedger> getAllResourceLedgers() {
        return Map.copyOf(resourceLedgers);
    }

    public Map<UUID, EconomyLedger> getAllEconomyLedgers() {
        return Map.copyOf(economyLedgers);
    }

    // --- Buildings ---

    public void registerBuilding(Building building) {
        buildings.put(building.id(), building);
        dirtyMarker.run();
    }

    public Collection<Building> getBuildingsForCivilization(UUID civilizationId) {
        return buildings.values().stream()
                .filter(b -> b.civilizationId().equals(civilizationId))
                .toList();
    }

    public int countBuildingsByType(UUID civilizationId, BuildingType type) {
        int count = 0;
        for (Building building : buildings.values()) {
            if (building.civilizationId().equals(civilizationId) && building.type() == type) {
                count++;
            }
        }
        return count;
    }

    public void addBuildingConstructionSite(BuildingConstructionSite site) {
        buildingSites.put(site.getId(), site);
        dirtyMarker.run();
    }

    public boolean hasActiveBuildingSite(UUID civilizationId) {
        return buildingSites.values().stream().anyMatch(s -> s.getCivilizationId().equals(civilizationId));
    }

    public Map<UUID, Building> getAllBuildings() {
        return Map.copyOf(buildings);
    }

    public Map<UUID, BuildingConstructionSite> getAllBuildingSites() {
        return Map.copyOf(buildingSites);
    }

    // --- Roads ---

    public void registerRoadSegment(RoadSegment segment) {
        roadSegments.put(segment.id(), segment);
        dirtyMarker.run();
    }

    public Collection<RoadSegment> getRoadSegmentsForCivilization(UUID civilizationId) {
        return roadSegments.values().stream()
                .filter(r -> r.civilizationId().equals(civilizationId))
                .toList();
    }

    public void addRoadConstructionSite(RoadConstructionSite site) {
        roadSites.put(site.getId(), site);
        dirtyMarker.run();
    }

    public boolean hasActiveRoadSite(UUID civilizationId) {
        return roadSites.values().stream().anyMatch(s -> s.getCivilizationId().equals(civilizationId));
    }

    public Map<UUID, RoadSegment> getAllRoadSegments() {
        return Map.copyOf(roadSegments);
    }

    public Map<UUID, RoadConstructionSite> getAllRoadSites() {
        return Map.copyOf(roadSites);
    }

    // --- Construction (buildings + roads unified) ---

    /** Every in-progress building and road across every civilization, for {@link MineGamer.civilizationai.world.ConstructionExecutor}. */
    public List<ConstructionJob> getAllActiveConstructionJobs() {
        List<ConstructionJob> jobs = new ArrayList<>(buildingSites.size() + roadSites.size());
        jobs.addAll(buildingSites.values());
        jobs.addAll(roadSites.values());
        return jobs;
    }

    /** Moves a finished job out of whichever active map it was in. Its completed form was already registered by {@code onComplete}. */
    public void completeConstructionJob(ConstructionJob job) {
        buildingSites.remove(job.getId());
        roadSites.remove(job.getId());
    }

    // --- Reputation ---

    public ReputationLedger getOrCreateReputationLedger(UUID civilizationId) {
        return reputationLedgers.computeIfAbsent(civilizationId, id -> {
            dirtyMarker.run();
            return new ReputationLedger(id);
        });
    }

    public Optional<ReputationLedger> getReputationLedger(UUID civilizationId) {
        return Optional.ofNullable(reputationLedgers.get(civilizationId));
    }

    public Map<UUID, ReputationLedger> getAllReputationLedgers() {
        return Map.copyOf(reputationLedgers);
    }

    // --- Incidents ---

    public void addIncident(Incident incident) {
        incidents.put(incident.id(), incident);
        dirtyMarker.run();
    }

    /** Active (not-yet-expired) incidents for a civilization, authoritative against {@code gameTime}. */
    public Collection<Incident> getActiveIncidents(UUID civilizationId, long gameTime) {
        return incidents.values().stream()
                .filter(i -> i.civilizationId().equals(civilizationId) && i.isActive(gameTime))
                .toList();
    }

    public boolean hasActiveIncident(UUID civilizationId, IncidentType type, long gameTime) {
        return incidents.values().stream()
                .anyMatch(i -> i.civilizationId().equals(civilizationId) && i.type() == type && i.isActive(gameTime));
    }

    /**
     * Presence check that trusts whatever pruning already happened this
     * cycle rather than taking a game-time parameter — see the Javadoc on
     * {@link MineGamer.civilizationai.domain.incident.DefenseAwareNeedsEvaluator}
     * for the one place that relies on this, and why.
     */
    public boolean hasIncident(UUID civilizationId, IncidentType type) {
        return incidents.values().stream()
                .anyMatch(i -> i.civilizationId().equals(civilizationId) && i.type() == type);
    }

    public void pruneExpiredIncidents(UUID civilizationId, long gameTime) {
        boolean removedAny = incidents.values().removeIf(
                i -> i.civilizationId().equals(civilizationId) && !i.isActive(gameTime));
        if (removedAny) {
            dirtyMarker.run();
        }
    }

    public Map<UUID, Incident> getAllIncidents() {
        return Map.copyOf(incidents);
    }

    // --- Technology ---

    public TechnologyLedger getOrCreateTechnologyLedger(UUID civilizationId) {
        return technologyLedgers.computeIfAbsent(civilizationId, id -> {
            dirtyMarker.run();
            return new TechnologyLedger(id);
        });
    }

    public Optional<TechnologyLedger> getTechnologyLedger(UUID civilizationId) {
        return Optional.ofNullable(technologyLedgers.get(civilizationId));
    }

    public Map<UUID, TechnologyLedger> getAllTechnologyLedgers() {
        return Map.copyOf(technologyLedgers);
    }
}
