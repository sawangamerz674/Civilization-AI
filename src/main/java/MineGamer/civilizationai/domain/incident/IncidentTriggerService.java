package MineGamer.civilizationai.domain.incident;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import MineGamer.civilizationai.memory.VillagerMemory;
import net.minecraft.util.RandomSource;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Checks a civilization's real state each evaluation cycle and, at most,
 * triggers one new {@link Incident} — same "gradual, one thing per cycle"
 * pacing as every other planner in this mod. Checked in priority order
 * (urgent first); the first matching condition wins. The three
 * chance-based triggers (DROUGHT, DISEASE, FIRE) scale with
 * {@code difficultyScalar} — a higher difficulty makes them more frequent.
 * <p>
 * BANDIT_RAID is the one type never triggered from here — it's triggered
 * externally by {@code ai.DefenseService} via {@link #triggerIncident} once
 * real hostile mobs are detected, since that requires world access this
 * (domain-only) class deliberately doesn't have.
 */
public final class IncidentTriggerService {

    private static final Map<IncidentType, Long> DURATION_TICKS = buildDurations();

    private static final double DROUGHT_CHANCE = 0.005;
    private static final double DISEASE_CHANCE = 0.003;
    private static final double FIRE_CHANCE = 0.002;

    public void evaluate(Civilization civilization, CivilizationManager manager, long gameTime, RandomSource random) {
        if (!ModConfig.COMMON.incidentsEnabled.get()) {
            return;
        }

        expireIncidents(manager, civilization.getId(), gameTime);

        if (!manager.getActiveIncidents(civilization.getId(), gameTime).isEmpty()) {
            return;
        }

        int population = civilization.getPopulation();
        if (population == 0) {
            return;
        }

        ResourceLedger stock = manager.getOrCreateResourceLedger(civilization.getId());

        if (stock.getAvailable(ResourceType.FOOD) <= 0) {
            triggerIncident(manager, civilization.getId(), IncidentType.FAMINE, gameTime);
            return;
        }

        if (stock.getAvailable(ResourceType.FOOD) >= population * 10L) {
            triggerIncident(manager, civilization.getId(), IncidentType.HARVEST_FESTIVAL, gameTime);
            return;
        }

        Optional<EconomyLedger> economy = manager.getEconomyLedger(civilization.getId());
        if (economy.isPresent() && averageMultiplier(economy.get()) < 0.8) {
            triggerIncident(manager, civilization.getId(), IncidentType.MARKET_BOOM, gameTime);
            return;
        }

        if (population >= 4 && averageRelationship(civilization, manager) < -2.0) {
            triggerIncident(manager, civilization.getId(), IncidentType.CIVIL_UNREST, gameTime);
            return;
        }

        double difficulty = ModConfig.COMMON.difficultyScalar.get();

        if (population >= 3 && random.nextDouble() < DROUGHT_CHANCE * difficulty) {
            triggerIncident(manager, civilization.getId(), IncidentType.DROUGHT, gameTime);
            return;
        }

        if (population >= 5 && random.nextDouble() < DISEASE_CHANCE * difficulty) {
            triggerIncident(manager, civilization.getId(), IncidentType.DISEASE, gameTime);
            return;
        }

        if (!manager.getBuildingsForCivilization(civilization.getId()).isEmpty() && random.nextDouble() < FIRE_CHANCE * difficulty) {
            triggerIncident(manager, civilization.getId(), IncidentType.FIRE, gameTime);
        }
    }

    /** Public so external detectors (e.g. {@code ai.DefenseService} for BANDIT_RAID) can trigger without duplicating duration/dedup logic. */
    public void triggerIncident(CivilizationManager manager, UUID civilizationId, IncidentType type, long gameTime) {
        if (manager.hasActiveIncident(civilizationId, type, gameTime)) {
            return;
        }
        long duration = DURATION_TICKS.getOrDefault(type, 6000L);
        Incident incident = new Incident(UUID.randomUUID(), civilizationId, type, gameTime, gameTime + duration);
        manager.addIncident(incident);
    }

    public void expireIncidents(CivilizationManager manager, UUID civilizationId, long gameTime) {
        manager.pruneExpiredIncidents(civilizationId, gameTime);
    }

    private double averageMultiplier(EconomyLedger economy) {
        Map<ResourceType, Double> multipliers = economy.getAllMultipliers();
        if (multipliers.isEmpty()) {
            return 1.0;
        }
        double sum = 0;
        for (double value : multipliers.values()) {
            sum += value;
        }
        return sum / multipliers.size();
    }

    private double averageRelationship(Civilization civilization, CivilizationManager manager) {
        long sum = 0;
        int count = 0;
        for (UUID villagerId : civilization.getVillagerIds()) {
            VillagerMemory memory = manager.getMemory(villagerId).orElse(null);
            if (memory == null) {
                continue;
            }
            for (int value : memory.getRelationships().values()) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / (double) count;
    }

    private static Map<IncidentType, Long> buildDurations() {
        Map<IncidentType, Long> map = new EnumMap<>(IncidentType.class);
        map.put(IncidentType.FAMINE, 6000L);
        map.put(IncidentType.DROUGHT, 12000L);
        map.put(IncidentType.DISEASE, 12000L);
        map.put(IncidentType.FIRE, 2000L);
        map.put(IncidentType.BANDIT_RAID, 6000L);
        map.put(IncidentType.HARVEST_FESTIVAL, 4000L);
        map.put(IncidentType.MARKET_BOOM, 6000L);
        map.put(IncidentType.CIVIL_UNREST, 8000L);
        return map;
    }
}
