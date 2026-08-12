package MineGamer.civilizationai.domain.technology;

/**
 * The spec's technology tiers, in unlock order: Primitive → Stone Roads →
 * Water Wells → Large Farms → Windmills → Warehouses → Markets → Watch
 * Towers → Defensive Walls → Libraries. Progression is strictly sequential
 * (no skipping) and driven by a real "prosperity" score — see
 * {@link TechnologyService}.
 * <p>
 * Most tiers are informational/foundational in this phase — a real WATER_WELLS
 * or WINDMILLS gameplay bonus would need mechanics (irrigation range,
 * production speed-ups) this mod doesn't model yet. STONE_ROADS is the one
 * tier with a concrete effect: unlocking it raises the slope
 * {@code RoadPlanner} will accept, representing better road engineering.
 * The rest exist as real, persisted, advancing state ready for a future
 * phase to attach effects to — not stubs, just not load-bearing yet.
 */
public enum Technology {
    PRIMITIVE(0),
    STONE_ROADS(50),
    WATER_WELLS(120),
    LARGE_FARMS(220),
    WINDMILLS(350),
    WAREHOUSES(500),
    MARKETS(700),
    WATCH_TOWERS(950),
    DEFENSIVE_WALLS(1250),
    LIBRARIES(1600);

    private final int prosperityThreshold;

    Technology(int prosperityThreshold) {
        this.prosperityThreshold = prosperityThreshold;
    }

    /** Minimum prosperity score required to unlock this tier from the previous one. */
    public int getProsperityThreshold() {
        return prosperityThreshold;
    }

    /** Null if this is already the final tier. */
    public Technology next() {
        int nextOrdinal = ordinal() + 1;
        Technology[] values = values();
        return nextOrdinal < values.length ? values[nextOrdinal] : null;
    }
}
