package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.api.event.TechnologyUnlockedEvent;
import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.JobAssignmentService;
import MineGamer.civilizationai.domain.economy.EconomyAwareNeedsEvaluator;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.economy.EconomyService;
import MineGamer.civilizationai.domain.incident.DefenseAwareNeedsEvaluator;
import MineGamer.civilizationai.domain.incident.IncidentTriggerService;
import MineGamer.civilizationai.domain.needs.JobRatioNeedsEvaluator;
import MineGamer.civilizationai.domain.needs.NeedsEvaluator;
import MineGamer.civilizationai.domain.resource.ProductionService;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.technology.Technology;
import MineGamer.civilizationai.domain.technology.TechnologyService;
import MineGamer.civilizationai.notification.IncidentNotifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.MinecraftForge;

/**
 * "Civilization Brain" — the top of the AI hierarchy
 * ({@code CivilizationBrain → VillageGovernor → DepartmentManager → VillagerProfile},
 * with the various planners/services as siblings of the governor).
 * <p>
 * Runs, in order, once per evaluation cycle for one civilization:
 * <ol>
 *     <li><b>Production</b> — always runs (now incident-aware: DROUGHT/DISEASE reduce yields).</li>
 *     <li><b>Economy</b> — if {@code economyEnabled}.</li>
 *     <li><b>Incidents</b> — expires old ones, may trigger one new one from real state.</li>
 *     <li><b>Defense</b> — if {@code warSystemEnabled}. Real hostile-mob detection, can trigger BANDIT_RAID.</li>
 *     <li><b>Notification</b> — announces any incident triggered on this exact cycle to nearby
 *         players and posts {@link MineGamer.civilizationai.api.event.IncidentTriggeredEvent}.</li>
 *     <li><b>Technology</b> — if {@code technologyEnabled}. Advances at most one tier from prosperity;
 *         posts {@link TechnologyUnlockedEvent} if it did.</li>
 *     <li><b>Building planning</b> — if {@code buildingEnabled}. Queue-only; see {@code world.ConstructionExecutor}.</li>
 *     <li><b>Road planning</b> — if {@code roadGenerationEnabled}. Queue-only, same as building.</li>
 *     <li><b>Relationships</b> — if {@code relationshipSimulationEnabled}. One interaction per cycle.</li>
 *     <li><b>Migration</b> — if {@code migrationEnabled}. Re-checks one villager per cycle.</li>
 *     <li><b>Needs &amp; job assignment</b> — if {@code jobAssignmentEnabled}. The evaluator chain now
 *         includes {@link DefenseAwareNeedsEvaluator} wrapping {@link EconomyAwareNeedsEvaluator}
 *         wrapping {@link JobRatioNeedsEvaluator} — three phases of decorators, none aware of the others.</li>
 * </ol>
 */
public final class CivilizationBrain {

    private static final NeedsEvaluator NEEDS_EVALUATOR =
            new DefenseAwareNeedsEvaluator(new EconomyAwareNeedsEvaluator(new JobRatioNeedsEvaluator()));
    private static final DepartmentManager DEPARTMENT_MANAGER =
            new ProfessionDepartmentManager(new JobAssignmentService());
    private static final ProductionService PRODUCTION_SERVICE = new ProductionService();
    private static final EconomyService ECONOMY_SERVICE = new EconomyService();
    private static final BuildingPlanner BUILDING_PLANNER = new BuildingPlanner();
    private static final RoadPlanner ROAD_PLANNER = new RoadPlanner();
    private static final IncidentTriggerService INCIDENT_TRIGGER_SERVICE = new IncidentTriggerService();
    private static final DefenseService DEFENSE_SERVICE = new DefenseService(INCIDENT_TRIGGER_SERVICE);
    private static final TechnologyService TECHNOLOGY_SERVICE = new TechnologyService();
    private static final RelationshipSimulator RELATIONSHIP_SIMULATOR = new RelationshipSimulator();
    private static final MigrationService MIGRATION_SERVICE = new MigrationService();
    private static final IncidentNotifier INCIDENT_NOTIFIER = new IncidentNotifier();

    private CivilizationBrain() {
    }

    public static void evaluate(Civilization civilization, CivilizationManager manager, MinecraftServer server,
                                 long gameTime, RandomSource random) {
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilization.getId());
        PRODUCTION_SERVICE.produce(civilization, manager, stock, random);

        if (ModConfig.COMMON.economyEnabled.get()) {
            EconomyLedger economy = manager.getOrCreateEconomyLedger(civilization.getId());
            ECONOMY_SERVICE.updatePrices(civilization, stock, economy);
        }

        // Production/economy above mutate the ledgers returned by reference,
        // bypassing CivilizationManager's usual per-method dirty marking —
        // see the Javadoc on CivilizationManager#markDirty.
        manager.markDirty();

        INCIDENT_TRIGGER_SERVICE.evaluate(civilization, manager, gameTime, random);
        DEFENSE_SERVICE.evaluate(civilization, manager, server, gameTime);
        INCIDENT_NOTIFIER.announceNewIncidents(civilization, manager, server, gameTime);

        Technology previousTier = manager.getOrCreateTechnologyLedger(civilization.getId()).getCurrentTier();
        TECHNOLOGY_SERVICE.evaluate(civilization, manager);
        Technology currentTier = manager.getOrCreateTechnologyLedger(civilization.getId()).getCurrentTier();
        if (currentTier != previousTier) {
            MinecraftForge.EVENT_BUS.post(new TechnologyUnlockedEvent(civilization.getId(), currentTier));
        }

        BUILDING_PLANNER.evaluate(civilization, manager, server, gameTime);
        ROAD_PLANNER.evaluate(civilization, manager, server, gameTime);

        RELATIONSHIP_SIMULATOR.evaluate(civilization, manager, random);
        MIGRATION_SERVICE.evaluate(civilization, manager, server, gameTime);

        if (ModConfig.COMMON.jobAssignmentEnabled.get()) {
            VillageGovernor.evaluate(civilization, manager, NEEDS_EVALUATOR, DEPARTMENT_MANAGER, gameTime, random);
        }
    }
}
