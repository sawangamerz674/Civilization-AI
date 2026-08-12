package MineGamer.civilizationai.domain.economy;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;

import java.util.Map;

/**
 * Implements the spec's "real supply and demand": each resource has a
 * per-capita target buffer (how much a population this size should
 * reasonably have banked). Available stock below that buffer raises the
 * price multiplier ("Food shortage → Food prices rise"); stock above it
 * lowers it ("Wood surplus" — cheaper, and via
 * {@link EconomyAwareNeedsEvaluator}, a lower-priority need).
 * <p>
 * Precious resources (GOLD, EMERALD, DIAMOND) intentionally have very small
 * per-capita targets — a village isn't expected to bank meaningful
 * quantities of them, so their multiplier responds mostly to actually
 * having zero rather than "not enough for everyone."
 */
public final class EconomyService {

    private static final double MIN_MULTIPLIER = 0.5;
    private static final double MAX_MULTIPLIER = 3.0;
    private static final double DEFAULT_PER_CAPITA_TARGET = 1.0;

    private static final Map<ResourceType, Double> PER_CAPITA_TARGET = Map.ofEntries(
            Map.entry(ResourceType.FOOD, 5.0),
            Map.entry(ResourceType.WOOD, 5.0),
            Map.entry(ResourceType.STONE, 3.0),
            Map.entry(ResourceType.IRON, 2.0),
            Map.entry(ResourceType.COAL, 2.0),
            Map.entry(ResourceType.SEEDS, 2.0),
            Map.entry(ResourceType.LEATHER, 1.0),
            Map.entry(ResourceType.GLASS, 1.0),
            Map.entry(ResourceType.CLAY, 1.0),
            Map.entry(ResourceType.COPPER, 1.0),
            Map.entry(ResourceType.GOLD, 0.5),
            Map.entry(ResourceType.EMERALD, 0.2),
            Map.entry(ResourceType.DIAMOND, 0.1)
    );

    public void updatePrices(Civilization civilization, ResourceLedger stock, EconomyLedger economy) {
        int population = Math.max(1, civilization.getPopulation());

        for (ResourceType type : ResourceType.values()) {
            double perCapitaTarget = PER_CAPITA_TARGET.getOrDefault(type, DEFAULT_PER_CAPITA_TARGET);
            double targetBuffer = perCapitaTarget * population;
            double available = stock.getAvailable(type);

            double ratio = targetBuffer <= 0 ? 1.0 : available / targetBuffer;
            double multiplier = clamp(2.0 - ratio, MIN_MULTIPLIER, MAX_MULTIPLIER);

            economy.setPriceMultiplier(type, multiplier);
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
