package MineGamer.civilizationai.domain.needs;

import MineGamer.civilizationai.domain.Profession;

/**
 * A category of civilization need whose fulfilment maps directly to staffing
 * one {@link Profession}. Every need in Phase 3 has exactly one mapped
 * profession — needs that don't reduce to "assign someone this job" (morale,
 * technology, overall economy health) belong to later phases once the
 * systems that actually drive them (Phase 4 economy, Phase 6 tech tree)
 * exist, rather than being stubbed out here with nothing to evaluate them.
 */
public enum NeedType {
    FOOD(Profession.FARMER),
    WOOD(Profession.LUMBERJACK),
    STONE(Profession.MINER),
    SAFETY(Profession.GUARD),
    HOUSING(Profession.BUILDER),
    TOOLS(Profession.BLACKSMITH),
    EDUCATION(Profession.TEACHER),
    RELIGION(Profession.PRIEST),
    EXPLORATION(Profession.SCOUT);

    private final Profession profession;

    NeedType(Profession profession) {
        this.profession = profession;
    }

    public Profession getProfession() {
        return profession;
    }
}
