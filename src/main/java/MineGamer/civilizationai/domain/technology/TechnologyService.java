package MineGamer.civilizationai.domain.technology;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;

/**
 * "Technology depends on prosperity" — prosperity here is population
 * (weighted, since a thriving population is the clearest sign of
 * prosperity this mod can measure) plus total resources currently banked
 * (not lifetime production, so a civilization that burns through its
 * stock on construction doesn't coast on past prosperity forever).
 * Advances at most one tier per evaluation cycle, same pacing discipline
 * as everything else.
 */
public final class TechnologyService {

    private static final int PROSPERITY_PER_POPULATION = 10;

    public void evaluate(Civilization civilization, CivilizationManager manager) {
        if (!ModConfig.COMMON.technologyEnabled.get()) {
            return;
        }

        TechnologyLedger ledger = manager.getOrCreateTechnologyLedger(civilization.getId());
        Technology next = ledger.getCurrentTier().next();
        if (next == null) {
            return;
        }

        double prosperity = computeProsperity(civilization, manager);
        if (prosperity >= next.getProsperityThreshold()) {
            ledger.setCurrentTier(next);
            manager.markDirty();
        }
    }

    private double computeProsperity(Civilization civilization, CivilizationManager manager) {
        double score = civilization.getPopulation() * (double) PROSPERITY_PER_POPULATION;
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilization.getId());
        for (ResourceType type : ResourceType.values()) {
            score += stock.getStock(type);
        }
        return score;
    }
}
