package MineGamer.civilizationai.domain;

/**
 * A procedural personality trait a villager can have. Traits are read by
 * later phases (job assignment, building AI, war response, ...) to bias
 * decisions — e.g. a BUILDER villager is preferred for construction tasks,
 * a COWARD flees raids sooner than a BRAVE villager.
 * <p>
 * Traits are grouped into mutually-exclusive pairs by
 * {@link PersonalityGenerator} so a villager is never generated as both
 * halves of an obvious contradiction (see {@link PersonalityGenerator#CONTRADICTIONS}).
 */
public enum PersonalityTrait {
    BRAVE,
    COWARD,
    GREEDY,
    GENEROUS,
    LAZY,
    HARDWORKING,
    CREATIVE,
    EFFICIENT,
    EXPLORER,
    BUILDER,
    FARMER,
    SOCIAL,
    AGGRESSIVE,
    DEFENSIVE,
    LEADER,
    FOLLOWER
}
