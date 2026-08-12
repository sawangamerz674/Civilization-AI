package MineGamer.civilizationai.domain.reputation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A civilization's collective opinion of each player, separate from any one
 * villager's personal opinion ({@code VillagerMemory.playerReputation} from
 * Phase 2, which represents an individual's memory). This is the aggregate
 * that would drive civilization-wide effects — trade pricing, quest/access
 * gating in a future phase — hence "collective," not "average of
 * villagers."
 */
public final class ReputationLedger {

    private final UUID civilizationId;
    private final Map<UUID, Integer> reputation = new HashMap<>();

    public ReputationLedger(UUID civilizationId) {
        this.civilizationId = civilizationId;
    }

    public static ReputationLedger reconstruct(UUID civilizationId, Map<UUID, Integer> reputation) {
        ReputationLedger ledger = new ReputationLedger(civilizationId);
        ledger.reputation.putAll(reputation);
        return ledger;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }

    public int getReputation(UUID playerId) {
        return reputation.getOrDefault(playerId, 0);
    }

    public void adjustReputation(UUID playerId, int delta) {
        reputation.merge(playerId, delta, Integer::sum);
    }

    public Map<UUID, Integer> getAllReputation() {
        return Map.copyOf(reputation);
    }
}
