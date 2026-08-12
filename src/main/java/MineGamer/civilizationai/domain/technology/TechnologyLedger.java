package MineGamer.civilizationai.domain.technology;

import java.util.UUID;

public final class TechnologyLedger {

    private final UUID civilizationId;
    private Technology currentTier;

    public TechnologyLedger(UUID civilizationId) {
        this.civilizationId = civilizationId;
        this.currentTier = Technology.PRIMITIVE;
    }

    public static TechnologyLedger reconstruct(UUID civilizationId, Technology currentTier) {
        TechnologyLedger ledger = new TechnologyLedger(civilizationId);
        ledger.currentTier = currentTier;
        return ledger;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }

    public Technology getCurrentTier() {
        return currentTier;
    }

    public void setCurrentTier(Technology tier) {
        this.currentTier = tier;
    }

    public boolean hasUnlocked(Technology tier) {
        return currentTier.ordinal() >= tier.ordinal();
    }
}
