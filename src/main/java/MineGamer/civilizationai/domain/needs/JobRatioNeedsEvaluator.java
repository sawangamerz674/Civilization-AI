package MineGamer.civilizationai.domain.needs;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.VillagerProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates need priority from population-to-job ratios: each {@link NeedType}
 * has a target fraction of the population that should hold its mapped
 * profession, a minimum population before the need applies at all (so a
 * 3-villager hamlet doesn't need a priest), and an urgency weight (food
 * matters more than religion when both are equally understaffed).
 * <p>
 * This is a deliberately simple, population-only model. It does not know
 * about actual food stock, actual building capacity, or actual danger —
 * those inputs don't exist until later phases. Swapping in a richer
 * evaluator once they do is a matter of implementing {@link NeedsEvaluator}
 * again, not modifying this class or anything that consumes it.
 */
public final class JobRatioNeedsEvaluator implements NeedsEvaluator {

    private record NeedDefinition(NeedType type, double targetRatio, int minPopulation, double urgencyWeight) {
    }

    private static final List<NeedDefinition> DEFINITIONS = List.of(
            new NeedDefinition(NeedType.FOOD, 0.30, 1, 1.5),
            new NeedDefinition(NeedType.SAFETY, 0.10, 4, 1.4),
            new NeedDefinition(NeedType.HOUSING, 0.10, 2, 1.2),
            new NeedDefinition(NeedType.WOOD, 0.15, 1, 1.1),
            new NeedDefinition(NeedType.STONE, 0.10, 3, 1.0),
            new NeedDefinition(NeedType.TOOLS, 0.05, 6, 0.9),
            new NeedDefinition(NeedType.EDUCATION, 0.05, 8, 0.7),
            new NeedDefinition(NeedType.EXPLORATION, 0.05, 6, 0.6),
            new NeedDefinition(NeedType.RELIGION, 0.05, 10, 0.5)
    );

    @Override
    public List<NeedScore> evaluate(Civilization civilization, CivilizationManager manager) {
        int population = civilization.getPopulation();
        if (population == 0) {
            return List.of();
        }

        List<NeedScore> scores = new ArrayList<>();
        for (NeedDefinition definition : DEFINITIONS) {
            if (population < definition.minPopulation()) {
                continue;
            }

            int targetCount = (int) Math.ceil(population * definition.targetRatio());
            int currentCount = countByProfession(civilization, manager, definition.type().getProfession());
            int deficit = targetCount - currentCount;
            if (deficit <= 0) {
                continue;
            }

            double priority = (deficit / (double) population) * definition.urgencyWeight();
            scores.add(new NeedScore(definition.type(), priority));
        }

        scores.sort(Comparator.comparingDouble(NeedScore::priority).reversed());
        return scores;
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
}
