package MineGamer.civilizationai.domain;

/**
 * A dynamic job a villager can hold, assigned by {@link JobAssignmentService}
 * in response to civilization needs (see {@code domain.needs}). This is a
 * fixed enum in Phase 3; Phase 7's public API is expected to replace this
 * with a proper registry so other mods can register additional professions
 * — that is a deliberate, documented limitation of this phase, not an
 * oversight.
 */
public enum Profession {
    /** No job assigned yet — the pool every department manager assigns from. */
    NONE,
    FARMER,
    LUMBERJACK,
    MINER,
    GUARD,
    BUILDER,
    BLACKSMITH,
    TEACHER,
    PRIEST,
    SCOUT
}
