package MineGamer.civilizationai.domain.incident;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.needs.NeedScore;
import MineGamer.civilizationai.domain.needs.NeedType;
import MineGamer.civilizationai.domain.needs.NeedsEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorates a base {@link NeedsEvaluator} so that a civilization under an
 * active {@link IncidentType#BANDIT_RAID} incident treats SAFETY as more
 * urgent — this is "Villages detect hostile civilizations... Train guards"
 * from the spec, expressed as a needs-priority boost rather than a
 * separate command path.
 * <p>
 * Relies on {@link IncidentTriggerService#expireIncidents} having already
 * run for this civilization earlier in the same evaluation cycle (see
 * {@code ai.CivilizationBrain}'s ordering) — this class checks presence via
 * {@link CivilizationManager#hasIncident} rather than re-checking expiry
 * itself, since it has no game-time parameter to check against.
 */
public final class DefenseAwareNeedsEvaluator implements NeedsEvaluator {

    private static final double RAID_SAFETY_MULTIPLIER = 2.5;
    private static final double SYNTHETIC_SAFETY_PRIORITY = 1.0;

    private final NeedsEvaluator delegate;

    public DefenseAwareNeedsEvaluator(NeedsEvaluator delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<NeedScore> evaluate(Civilization civilization, CivilizationManager manager) {
        List<NeedScore> base = delegate.evaluate(civilization, manager);

        if (!manager.hasIncident(civilization.getId(), IncidentType.BANDIT_RAID)) {
            return base;
        }

        List<NeedScore> adjusted = new ArrayList<>();
        boolean sawSafety = false;
        for (NeedScore score : base) {
            if (score.type() == NeedType.SAFETY) {
                adjusted.add(new NeedScore(NeedType.SAFETY, score.priority() * RAID_SAFETY_MULTIPLIER));
                sawSafety = true;
            } else {
                adjusted.add(score);
            }
        }
        if (!sawSafety) {
            adjusted.add(new NeedScore(NeedType.SAFETY, SYNTHETIC_SAFETY_PRIORITY));
        }

        adjusted.sort((a, b) -> Double.compare(b.priority(), a.priority()));
        return adjusted;
    }
}
