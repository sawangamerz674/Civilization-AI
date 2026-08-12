package MineGamer.civilizationai.domain.reputation;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.memory.VillagerMemory;

import java.util.UUID;

/**
 * Applies a {@link ReputationEvent} to a civilization's {@link ReputationLedger}
 * and, for flavor and consistency with Phase 2's per-villager memory,
 * nudges every member's individual {@code playerReputation} by the same
 * event (civilization sizes stay small enough that this is cheap).
 */
public final class ReputationService {

    public void recordEvent(CivilizationManager manager, UUID civilizationId, UUID playerId, ReputationEvent event) {
        int delta = deltaFor(event);

        ReputationLedger ledger = manager.getOrCreateReputationLedger(civilizationId);
        ledger.adjustReputation(playerId, delta);

        Civilization civilization = manager.getCivilization(civilizationId).orElse(null);
        if (civilization != null) {
            for (UUID villagerId : civilization.getVillagerIds()) {
                VillagerMemory memory = manager.getOrCreateMemory(villagerId);
                memory.adjustPlayerReputation(playerId, delta);
            }
        }

        manager.markDirty();
    }

    private int deltaFor(ReputationEvent event) {
        return switch (event) {
            case TRADED_WITH_VILLAGER -> ModConfig.COMMON.reputationTradeDelta.get();
            case ATTACKED_VILLAGER -> ModConfig.COMMON.reputationAttackDelta.get();
            case DEFENDED_VILLAGE -> ModConfig.COMMON.reputationDefendDelta.get();
        };
    }

    /**
     * A price multiplier a future trade-price hook could apply: friendly
     * reputation discounts, hostile reputation marks up. Not yet consumed
     * anywhere — see this class's Javadoc for why wiring it into the live
     * trade GUI is out of scope for this phase.
     */
    public double getTradeMultiplier(int reputation) {
        double clamped = Math.max(-100, Math.min(100, reputation));
        return 1.0 - (clamped / 100.0) * 0.25; // ±100 reputation → ∓25% price
    }
}
