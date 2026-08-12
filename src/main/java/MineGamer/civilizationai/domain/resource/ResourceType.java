package MineGamer.civilizationai.domain.resource;

/**
 * A trackable civilization resource. Every unit produced, stored, reserved,
 * or consumed by the mod is one of these — see {@link ResourceLedger} for
 * per-civilization stock and {@link ProductionService} for how each is
 * produced.
 */
public enum ResourceType {
    WOOD,
    STONE,
    IRON,
    COAL,
    FOOD,
    SEEDS,
    LEATHER,
    GLASS,
    CLAY,
    COPPER,
    GOLD,
    EMERALD,
    DIAMOND
}
