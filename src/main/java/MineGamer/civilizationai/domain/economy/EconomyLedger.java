package MineGamer.civilizationai.domain.economy;

import MineGamer.civilizationai.domain.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * A civilization's current price multiplier for each {@link ResourceType},
 * computed by {@link EconomyService}. A multiplier above 1.0 means the
 * resource is scarce relative to demand (prices rise); below 1.0 means it's
 * in surplus. Trading logic in a later phase is expected to multiply a base
 * price by this value; nothing in Phase 4 executes an actual trade yet.
 */
public final class EconomyLedger {

    private final UUID civilizationId;
    private final Map<ResourceType, Double> priceMultipliers = new EnumMap<>(ResourceType.class);

    public EconomyLedger(UUID civilizationId) {
        this.civilizationId = civilizationId;
    }

    public static EconomyLedger reconstruct(UUID civilizationId, Map<ResourceType, Double> priceMultipliers) {
        EconomyLedger ledger = new EconomyLedger(civilizationId);
        ledger.priceMultipliers.putAll(priceMultipliers);
        return ledger;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }

    /** Defaults to 1.0 (neutral) for any resource that hasn't been priced yet. */
    public double getPriceMultiplier(ResourceType type) {
        return priceMultipliers.getOrDefault(type, 1.0);
    }

    public void setPriceMultiplier(ResourceType type, double multiplier) {
        priceMultipliers.put(type, multiplier);
    }

    public Map<ResourceType, Double> getAllMultipliers() {
        return Map.copyOf(priceMultipliers);
    }
}
