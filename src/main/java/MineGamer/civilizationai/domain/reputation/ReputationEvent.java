package MineGamer.civilizationai.domain.reputation;

/**
 * A player action that moves a civilization's opinion of them, per the
 * spec's "Help village → Trust increases. Steal → Trust decreases. Attack →
 * Become enemy. Protect → Become hero."
 * <p>
 * {@code STOLE_FROM} is deliberately absent: detecting theft would need a
 * claimed-container/ownership concept this mod doesn't have (villagers
 * don't own chests here) — a documented gap, not an oversight. The three
 * events below are the ones {@code event.ReputationEventHandler} can
 * actually detect from real gameplay.
 */
public enum ReputationEvent {
    TRADED_WITH_VILLAGER,
    ATTACKED_VILLAGER,
    DEFENDED_VILLAGE
}
