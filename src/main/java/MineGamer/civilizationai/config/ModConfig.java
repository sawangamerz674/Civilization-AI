package MineGamer.civilizationai.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * All user-tunable values for the mod, split into logical sections.
 * <p>
 * This is registered as a single COMMON config (server-authoritative values
 * that must be identical for every player, since the simulation itself is
 * server-side only — see the architecture doc). Later phases will read these
 * values through the {@code COMMON} holder rather than hardcoding numbers,
 * so every subsystem stays configurable without new config classes.
 */
public final class ModConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    private ModConfig() {
    }

    public static final class Common {

        // --- Simulation ---
        public final ForgeConfigSpec.IntValue simulationTicksPerEvaluation;
        public final ForgeConfigSpec.IntValue populationCapPerVillage;
        public final ForgeConfigSpec.DoubleValue constructionSpeedMultiplier;
        public final ForgeConfigSpec.IntValue civilizationActivityRadius;

        // --- Systems toggles ---
        public final ForgeConfigSpec.BooleanValue economyEnabled;
        public final ForgeConfigSpec.BooleanValue warSystemEnabled;
        public final ForgeConfigSpec.BooleanValue roadGenerationEnabled;
        public final ForgeConfigSpec.BooleanValue technologyEnabled;
        public final ForgeConfigSpec.BooleanValue jobAssignmentEnabled;
        public final ForgeConfigSpec.BooleanValue buildingEnabled;
        public final ForgeConfigSpec.BooleanValue reputationEnabled;
        public final ForgeConfigSpec.BooleanValue relationshipSimulationEnabled;
        public final ForgeConfigSpec.BooleanValue migrationEnabled;
        public final ForgeConfigSpec.BooleanValue incidentsEnabled;

        // --- Difficulty ---
        public final ForgeConfigSpec.DoubleValue difficultyScalar;

        // --- Performance ---
        public final ForgeConfigSpec.IntValue maxVillagersProcessedPerTick;
        public final ForgeConfigSpec.IntValue inactiveVillageSimulationInterval;
        public final ForgeConfigSpec.IntValue maxVillagesPerServer;
        public final ForgeConfigSpec.IntValue maxBlockPlacementsPerTick;

        // --- Economy ---
        public final ForgeConfigSpec.IntValue resourceStorageCapacity;
        public final ForgeConfigSpec.IntValue warehouseCapacityBonus;
        public final ForgeConfigSpec.IntValue warehouseTriggerStock;

        // --- Building ---
        public final ForgeConfigSpec.IntValue housingCapacityPerHouse;
        public final ForgeConfigSpec.IntValue buildingSiteSearchRadius;
        public final ForgeConfigSpec.IntValue buildingSiteMaxVariance;
        public final ForgeConfigSpec.IntValue buildingSiteBuffer;
        public final ForgeConfigSpec.IntValue districtClusterRadius;

        // --- Roads ---
        public final ForgeConfigSpec.IntValue roadPavingTrafficThreshold;
        public final ForgeConfigSpec.IntValue roadMaxSlope;
        public final ForgeConfigSpec.IntValue roadBridgeDropThreshold;
        public final ForgeConfigSpec.IntValue roadMaxSlopeStoneRoadsBonus;

        // --- Population ---
        public final ForgeConfigSpec.IntValue civilizationClaimRadius;
        public final ForgeConfigSpec.IntValue migrationMinImprovementBlocks;

        // --- Reputation ---
        public final ForgeConfigSpec.IntValue reputationTradeDelta;
        public final ForgeConfigSpec.IntValue reputationAttackDelta;
        public final ForgeConfigSpec.IntValue reputationDefendDelta;

        // --- Defense ---
        public final ForgeConfigSpec.IntValue defenseThreatRadius;
        public final ForgeConfigSpec.IntValue defenseThreatThreshold;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Civilization AI - Simulation Settings").push("simulation");

            simulationTicksPerEvaluation = builder
                    .comment("How many server ticks between full needs-evaluation passes for an active village.",
                            "Lower = more responsive civilizations, higher server cost. Default: 200 (10s).")
                    .defineInRange("simulationTicksPerEvaluation", 200, 20, 6000);

            populationCapPerVillage = builder
                    .comment("Maximum villagers a single civilization will grow to before births stop.")
                    .defineInRange("populationCapPerVillage", 64, 1, 1000);

            constructionSpeedMultiplier = builder
                    .comment("Multiplier applied to how fast builders place blocks. 1.0 = normal.")
                    .defineInRange("constructionSpeedMultiplier", 1.0, 0.1, 10.0);

            civilizationActivityRadius = builder
                    .comment("Radius in blocks around a civilization's origin within which a nearby player",
                            "counts as making it 'active' for full-rate simulation, instead of the reduced",
                            "LOD rate used for unobserved villages.")
                    .defineInRange("civilizationActivityRadius", 128, 16, 1024);

            builder.pop();

            builder.comment("Civilization AI - System Toggles").push("systems");

            economyEnabled = builder
                    .comment("If false, supply/demand pricing is disabled and trades use static prices.")
                    .define("economyEnabled", true);

            warSystemEnabled = builder
                    .comment("If false, civilizations never build defenses or train guards in response to threats.")
                    .define("warSystemEnabled", true);

            roadGenerationEnabled = builder
                    .comment("If false, villagers will not pave or build roads between frequently traveled points.")
                    .define("roadGenerationEnabled", true);

            technologyEnabled = builder
                    .comment("If false, civilizations stay at the primitive technology tier permanently.")
                    .define("technologyEnabled", true);

            jobAssignmentEnabled = builder
                    .comment("If false, villagers never receive dynamic job assignments based on civilization needs.")
                    .define("jobAssignmentEnabled", true);

            buildingEnabled = builder
                    .comment("If false, civilizations never start new building construction.")
                    .define("buildingEnabled", true);

            reputationEnabled = builder
                    .comment("If false, player actions (trading, attacking villagers, defending a village) never",
                            "affect civilization reputation.")
                    .define("reputationEnabled", true);

            relationshipSimulationEnabled = builder
                    .comment("If false, villagers never form or update relationships with each other.")
                    .define("relationshipSimulationEnabled", true);

            migrationEnabled = builder
                    .comment("If false, villagers never migrate to a closer civilization.")
                    .define("migrationEnabled", true);

            incidentsEnabled = builder
                    .comment("If false, dynamic events (famine, drought, disease, bandit raids, festivals, ...) never trigger.")
                    .define("incidentsEnabled", true);

            builder.pop();

            builder.comment("Civilization AI - Difficulty").push("difficulty");

            difficultyScalar = builder
                    .comment("Global scalar applied to raid frequency/strength and resource scarcity.",
                            "1.0 = balanced. Lower = easier, higher = harder.")
                    .defineInRange("difficultyScalar", 1.0, 0.1, 5.0);

            builder.pop();

            builder.comment("Civilization AI - Performance").push("performance");

            maxVillagersProcessedPerTick = builder
                    .comment("Hard cap on how many villagers can run AI/task logic in a single server tick.",
                            "Protects TPS on large servers at the cost of AI responsiveness under heavy load.")
                    .defineInRange("maxVillagersProcessedPerTick", 200, 10, 5000);

            inactiveVillageSimulationInterval = builder
                    .comment("Tick interval used for villages with no players nearby (LOD simulation).",
                            "Higher = cheaper but less accurate simulation while unobserved.")
                    .defineInRange("inactiveVillageSimulationInterval", 1200, 100, 24000);

            maxVillagesPerServer = builder
                    .comment("Upper bound on simultaneously tracked civilizations. Prevents unbounded growth.")
                    .defineInRange("maxVillagesPerServer", 100, 1, 10000);

            maxBlockPlacementsPerTick = builder
                    .comment("Hard cap on total blocks placed across ALL active buildings and roads, in ALL",
                            "civilizations, in a single server tick. Split fairly across active construction",
                            "jobs. This is the primary knob protecting TPS during heavy construction.")
                    .defineInRange("maxBlockPlacementsPerTick", 64, 1, 100_000);

            builder.pop();

            builder.comment("Civilization AI - Economy").push("economy");

            resourceStorageCapacity = builder
                    .comment("Maximum amount of any single resource type a civilization can stockpile.",
                            "Production beyond this cap is lost, simulating limited storage until a WAREHOUSE",
                            "is built, which raises it via warehouseCapacityBonus.")
                    .defineInRange("resourceStorageCapacity", 10000, 100, 10_000_000);

            warehouseCapacityBonus = builder
                    .comment("Flat storage capacity bonus (per resource type) granted by each completed WAREHOUSE.")
                    .defineInRange("warehouseCapacityBonus", 5000, 0, 10_000_000);

            warehouseTriggerStock = builder
                    .comment("Total stock (summed across all resource types) that triggers wanting another",
                            "WAREHOUSE built, scaled by (existing warehouse count + 1).")
                    .defineInRange("warehouseTriggerStock", 500, 10, 1_000_000);

            builder.pop();

            builder.comment("Civilization AI - Building").push("building");

            housingCapacityPerHouse = builder
                    .comment("How many villagers one HOUSE is assumed to shelter, for deciding when another is needed.")
                    .defineInRange("housingCapacityPerHouse", 4, 1, 64);

            buildingSiteSearchRadius = builder
                    .comment("Maximum radius in blocks SiteSelector will search outward from a civilization's",
                            "origin (or existing district center) for a buildable site.")
                    .defineInRange("buildingSiteSearchRadius", 64, 8, 512);

            buildingSiteMaxVariance = builder
                    .comment("Maximum height difference, in blocks, allowed across a candidate building",
                            "footprint before it's rejected as too uneven.")
                    .defineInRange("buildingSiteMaxVariance", 4, 1, 32);

            buildingSiteBuffer = builder
                    .comment("Minimum extra spacing, in blocks, kept between a new building and any existing one.")
                    .defineInRange("buildingSiteBuffer", 2, 0, 32);

            districtClusterRadius = builder
                    .comment("Buildings of the same district type within this many blocks of each other are",
                            "considered part of the same emergent district.")
                    .defineInRange("districtClusterRadius", 24, 4, 256);

            builder.pop();

            builder.comment("Civilization AI - Roads").push("roads");

            roadPavingTrafficThreshold = builder
                    .comment("How many times a TravelRoute must have been walked before it's paved into a road.")
                    .defineInRange("roadPavingTrafficThreshold", 8, 1, 10_000);

            roadMaxSlope = builder
                    .comment("Maximum height difference, in blocks, a road segment will attempt to pave.",
                            "Steeper terrain is left unpaved entirely rather than routed around.")
                    .defineInRange("roadMaxSlope", 4, 1, 32);

            roadBridgeDropThreshold = builder
                    .comment("A local height drop (or fluid) at least this large along a road triggers a",
                            "flat bridge instead of following the terrain down and back up.")
                    .defineInRange("roadBridgeDropThreshold", 3, 1, 32);

            roadMaxSlopeStoneRoadsBonus = builder
                    .comment("Extra slope tolerance added to roadMaxSlope once a civilization has unlocked",
                            "the STONE_ROADS technology tier.")
                    .defineInRange("roadMaxSlopeStoneRoadsBonus", 2, 0, 32);

            builder.pop();

            builder.comment("Civilization AI - Population").push("population");

            civilizationClaimRadius = builder
                    .comment("Radius in blocks within which a newly detected villager is claimed by an existing",
                            "civilization. Beyond this radius from every civilization, a new one is founded.")
                    .defineInRange("civilizationClaimRadius", 96, 16, 2048);

            migrationMinImprovementBlocks = builder
                    .comment("A villager only migrates to a different civilization if it is at least this many",
                            "blocks closer than its current one, preventing back-and-forth flapping near borders.")
                    .defineInRange("migrationMinImprovementBlocks", 16, 0, 512);

            builder.pop();

            builder.comment("Civilization AI - Reputation").push("reputation");

            reputationTradeDelta = builder
                    .comment("Reputation change per completed trade with a tracked villager.")
                    .defineInRange("reputationTradeDelta", 1, -100, 100);

            reputationAttackDelta = builder
                    .comment("Reputation change per hit landed on a tracked villager.")
                    .defineInRange("reputationAttackDelta", -5, -100, 100);

            reputationDefendDelta = builder
                    .comment("Reputation change per hostile mob killed near a civilization.")
                    .defineInRange("reputationDefendDelta", 3, -100, 100);

            builder.pop();

            builder.comment("Civilization AI - Defense").push("defense");

            defenseThreatRadius = builder
                    .comment("Radius in blocks around a civilization's origin scanned for hostile mobs.")
                    .defineInRange("defenseThreatRadius", 32, 8, 256);

            defenseThreatThreshold = builder
                    .comment("Number of hostile mobs within defenseThreatRadius that triggers a BANDIT_RAID incident.")
                    .defineInRange("defenseThreatThreshold", 3, 1, 100);

            builder.pop();
        }
    }
}
