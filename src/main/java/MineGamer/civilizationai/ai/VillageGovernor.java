package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.needs.NeedScore;
import MineGamer.civilizationai.domain.needs.NeedsEvaluator;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * The "Village Governor" layer: evaluates all of a civilization's needs and
 * acts on the single most urgent one per evaluation cycle.
 * <p>
 * Deliberately acts on only one need per cycle rather than sweeping every
 * unmet need at once — this is what makes villages staff up gradually
 * rather than instantly snapping to their target ratios the moment
 * population crosses a threshold, matching the "no scripted progression,
 * everything emerges" design goal. Evaluation cycles run every
 * {@code simulationTicksPerEvaluation} ticks (10s by default), so a
 * civilization several jobs short of its targets fills them out over
 * roughly that many cycles, not instantly.
 */
public final class VillageGovernor {

    private VillageGovernor() {
    }

    public static void evaluate(Civilization civilization, CivilizationManager manager,
                                 NeedsEvaluator needsEvaluator, DepartmentManager departmentManager,
                                 long gameTime, RandomSource random) {
        List<NeedScore> needs = needsEvaluator.evaluate(civilization, manager);
        if (needs.isEmpty()) {
            return;
        }

        NeedScore mostUrgent = needs.get(0);
        departmentManager.fulfilNeed(mostUrgent, civilization, manager, gameTime, random);
    }
}
