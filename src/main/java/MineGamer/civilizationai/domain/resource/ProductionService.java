package MineGamer.civilizationai.domain.resource;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.building.BuildingType;
import MineGamer.civilizationai.domain.incident.IncidentType;
import net.minecraft.util.RandomSource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts staffed jobs into resources once per evaluation cycle.
 * <p>
 * Each profession has a fixed {@link Yield} table: a guaranteed base yield
 * plus optional lower-probability secondary yields (this is how a MINER
 * produces mostly STONE but occasionally COAL, IRON, COPPER, GOLD, EMERALD,
 * or DIAMOND — rarer materials have lower {@code chance}). Professions with
 * no production role (GUARD, TEACHER, PRIEST, SCOUT) simply have no entry
 * and are skipped; BLACKSMITH is the one profession that only consumes.
 * <p>
 * This models production abstractly from staffing counts rather than
 * simulating any villager physically walking to a tree or ore vein — there
 * is no entity-level gathering yet (see the Phase 3/4 scope notes in
 * {@code docs/ARCHITECTURE.md}).
 * <p>
 * Phase 6 adds two incident-driven multipliers: an active DROUGHT halves
 * FOOD yield specifically (dry fields, everything else unaffected); an
 * active DISEASE reduces every yield (a sick workforce is less productive
 * across the board). Both read via {@link CivilizationManager#hasIncident},
 * so they rely on the same "already pruned this cycle" assumption
 * documented on {@link MineGamer.civilizationai.domain.incident.DefenseAwareNeedsEvaluator}.
 */
public final class ProductionService {

    private record Yield(ResourceType type, long amount, double chance) {
    }

    private static final Map<Profession, List<Yield>> YIELDS = buildYields();

    private static final double DROUGHT_FOOD_MULTIPLIER = 0.5;
    private static final double DISEASE_ALL_YIELD_MULTIPLIER = 0.7;

    public void produce(Civilization civilization, CivilizationManager manager, ResourceLedger stock, RandomSource random) {
        long capacity = effectiveCapacity(civilization, manager);
        Map<Profession, Integer> counts = countByProfession(civilization, manager);

        boolean drought = manager.hasIncident(civilization.getId(), IncidentType.DROUGHT);
        boolean disease = manager.hasIncident(civilization.getId(), IncidentType.DISEASE);

        for (Map.Entry<Profession, Integer> entry : counts.entrySet()) {
            List<Yield> yields = YIELDS.get(entry.getKey());
            if (yields == null) {
                continue;
            }
            int workers = entry.getValue();
            for (int i = 0; i < workers; i++) {
                for (Yield yield : yields) {
                    double chance = yield.chance();
                    if (disease) {
                        chance *= DISEASE_ALL_YIELD_MULTIPLIER;
                    }
                    if (drought && yield.type() == ResourceType.FOOD) {
                        chance *= DROUGHT_FOOD_MULTIPLIER;
                    }
                    if (random.nextDouble() <= chance) {
                        stock.deposit(yield.type(), yield.amount(), capacity);
                    }
                }
            }
        }

        applyBlacksmithConsumption(counts.getOrDefault(Profession.BLACKSMITH, 0), stock);
    }

    /**
     * Base configured capacity plus a flat bonus per completed WAREHOUSE —
     * this is what Phase 4's config comment meant by "until Phase 5
     * buildings can raise it." Applies uniformly to every resource type
     * rather than per-type warehouse specialization, which would need a
     * building subtype this phase doesn't have.
     */
    private long effectiveCapacity(Civilization civilization, CivilizationManager manager) {
        long base = ModConfig.COMMON.resourceStorageCapacity.get();
        int warehouses = manager.countBuildingsByType(civilization.getId(), BuildingType.WAREHOUSE);
        long bonus = (long) warehouses * ModConfig.COMMON.warehouseCapacityBonus.get();
        return base + bonus;
    }

    /**
     * Each blacksmith consumes 1 IRON + 1 COAL per cycle if both are
     * available, representing ongoing tool production. No tracked "tools"
     * resource exists to deposit in return — see {@link Profession}'s
     * Javadoc on the fixed-enum limitation; a future phase's registry-based
     * profession API is the natural place to add tracked crafted outputs.
     */
    private void applyBlacksmithConsumption(int blacksmithCount, ResourceLedger stock) {
        for (int i = 0; i < blacksmithCount; i++) {
            if (stock.getAvailable(ResourceType.IRON) >= 1 && stock.getAvailable(ResourceType.COAL) >= 1) {
                stock.withdrawUnreserved(ResourceType.IRON, 1);
                stock.withdrawUnreserved(ResourceType.COAL, 1);
            }
        }
    }

    private Map<Profession, Integer> countByProfession(Civilization civilization, CivilizationManager manager) {
        Map<Profession, Integer> counts = new EnumMap<>(Profession.class);
        for (UUID villagerId : civilization.getVillagerIds()) {
            manager.getProfile(villagerId)
                    .ifPresent(profile -> counts.merge(profile.getProfession(), 1, Integer::sum));
        }
        return counts;
    }

    private static Map<Profession, List<Yield>> buildYields() {
        Map<Profession, List<Yield>> map = new EnumMap<>(Profession.class);

        map.put(Profession.FARMER, List.of(
                new Yield(ResourceType.FOOD, 3, 1.0),
                new Yield(ResourceType.SEEDS, 1, 0.25)
        ));

        map.put(Profession.LUMBERJACK, List.of(
                new Yield(ResourceType.WOOD, 4, 1.0)
        ));

        map.put(Profession.MINER, List.of(
                new Yield(ResourceType.STONE, 3, 1.0),
                new Yield(ResourceType.COAL, 1, 0.35),
                new Yield(ResourceType.IRON, 1, 0.25),
                new Yield(ResourceType.COPPER, 1, 0.20),
                new Yield(ResourceType.GOLD, 1, 0.08),
                new Yield(ResourceType.EMERALD, 1, 0.03),
                new Yield(ResourceType.DIAMOND, 1, 0.015)
        ));

        return map;
    }
}
