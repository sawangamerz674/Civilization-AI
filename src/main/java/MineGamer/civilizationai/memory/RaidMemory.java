package MineGamer.civilizationai.memory;

/**
 * A remembered raid event. {@code survived} refers to this specific
 * villager, not the civilization as a whole — each villager's memory
 * records raids from their own perspective.
 */
public record RaidMemory(long gameTime, int enemyCount, boolean survived) {
}
