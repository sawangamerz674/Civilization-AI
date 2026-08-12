package MineGamer.civilizationai.domain.needs;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;

import java.util.List;

/**
 * Computes a civilization's currently unmet needs, most urgent first. An
 * interface rather than a single concrete class so later phases (once real
 * resource stock, morale, and technology exist) can swap in a richer
 * evaluator without changing anything in the {@code ai} package that
 * consumes it.
 */
public interface NeedsEvaluator {

    /**
     * @return needs with positive priority, sorted descending by priority.
     *         Empty if every need is currently satisfied.
     */
    List<NeedScore> evaluate(Civilization civilization, CivilizationManager manager);
}
