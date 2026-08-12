package MineGamer.civilizationai.domain.incident;

/**
 * A dynamic event affecting one civilization, triggered by
 * {@link IncidentTriggerService} from real simulation state rather than on
 * a fixed schedule. Not every type from the spec's EVENTS section is
 * present — FLOOD, TRADE_CARAVANS, BUILDING_COLLAPSE, and MIGRATION are
 * deliberately omitted: the first three would need terrain/weather
 * checks, NPC caravan spawning, or safe partial-structure destruction that
 * this phase doesn't implement, and migration is already handled as its
 * own mechanism by {@code ai.MigrationService} rather than as a narrative
 * event.
 * <p>
 * FAMINE, DROUGHT, DISEASE, and BANDIT_RAID have real mechanical effects
 * (see {@link IncidentTriggerService} and {@code domain.resource.ProductionService}).
 * FIRE, HARVEST_FESTIVAL, MARKET_BOOM, and CIVIL_UNREST are informational —
 * they're real, queryable, and expire normally, but nothing currently reads
 * them to change simulation behavior beyond what already produced them.
 */
public enum IncidentType {
    FAMINE,
    DROUGHT,
    DISEASE,
    FIRE,
    BANDIT_RAID,
    HARVEST_FESTIVAL,
    MARKET_BOOM,
    CIVIL_UNREST
}
