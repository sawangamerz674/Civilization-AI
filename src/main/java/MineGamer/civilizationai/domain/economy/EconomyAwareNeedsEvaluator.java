package MineGamer.civilizationai.domain.economy;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.needs.NeedScore;
import MineGamer.civilizationai.domain.needs.NeedType;
import MineGamer.civilizationai.domain.needs.NeedsEvaluator;
import MineGamer.civilizationai.domain.resource.ResourceType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Decorates a base {@link NeedsEvaluator} (Phase 3's population-ratio model)
 * with resource-scarcity awareness, implementing "Iron shortage → Mining
 * expands" from the spec:
 * <ol>
 *     <li>Every need the base evaluator already flagged gets its priority
 *         multiplied by the worst price multiplier among resources linked
 *         to it (see {@link ResourceNeedLinkage}) — this is "sharpening":
 *         an understaffed, resource-starved job gets acted on before an
 *         understaffed but resource-comfortable one.</li>
 *     <li>A severe shortage (multiplier ≥ 1.5) can inject a need for a job
 *         the base evaluator considers already at its population-ratio
 *         quota — mining can expand beyond the "normal" ratio specifically
 *         because iron ran out, not just because the population grew.</li>
 * </ol>
 * If {@code economyEnabled} is off, or no {@link EconomyLedger} exists yet
 * for this civilization, this simply delegates unchanged — the economy
 * layer is additive, never required for the base needs system to function.
 */
public final class EconomyAwareNeedsEvaluator implements NeedsEvaluator {

    private static final double SHORTAGE_INJECTION_THRESHOLD = 1.5;
    private static final double SYNTHETIC_NEED_WEIGHT = 0.5;
    private static final int MIN_POPULATION_FOR_INJECTION = 2;

    private final NeedsEvaluator delegate;

    public EconomyAwareNeedsEvaluator(NeedsEvaluator delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<NeedScore> evaluate(Civilization civilization, CivilizationManager manager) {
        List<NeedScore> base = delegate.evaluate(civilization, manager);

        if (!ModConfig.COMMON.economyEnabled.get()) {
            return base;
        }

        Optional<EconomyLedger> economyOpt = manager.getEconomyLedger(civilization.getId());
        if (economyOpt.isEmpty()) {
            return base;
        }

        Map<NeedType, Double> shortageFactor = computeShortageFactors(economyOpt.get());

        Map<NeedType, Double> adjusted = new EnumMap<>(NeedType.class);
        for (NeedScore score : base) {
            double factor = shortageFactor.getOrDefault(score.type(), 1.0);
            adjusted.put(score.type(), score.priority() * factor);
        }

        if (civilization.getPopulation() >= MIN_POPULATION_FOR_INJECTION) {
            for (Map.Entry<NeedType, Double> entry : shortageFactor.entrySet()) {
                if (entry.getValue() >= SHORTAGE_INJECTION_THRESHOLD && !adjusted.containsKey(entry.getKey())) {
                    adjusted.put(entry.getKey(), (entry.getValue() - 1.0) * SYNTHETIC_NEED_WEIGHT);
                }
            }
        }

        List<NeedScore> result = new ArrayList<>();
        for (Map.Entry<NeedType, Double> entry : adjusted.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(new NeedScore(entry.getKey(), entry.getValue()));
            }
        }
        result.sort(Comparator.comparingDouble(NeedScore::priority).reversed());
        return result;
    }

    private Map<NeedType, Double> computeShortageFactors(EconomyLedger economy) {
        Map<NeedType, Double> factors = new EnumMap<>(NeedType.class);
        for (ResourceType type : ResourceType.values()) {
            ResourceNeedLinkage.getLinkedNeed(type).ifPresent(needType -> {
                double multiplier = economy.getPriceMultiplier(type);
                factors.merge(needType, multiplier, Math::max);
            });
        }
        return factors;
    }
}
