package MineGamer.civilizationai.memory;

/**
 * A remembered weather state change (recorded on transition, not every
 * tick — callers should only add an entry when raining/thundering actually
 * changes). Used by future phases to inform farming and construction
 * scheduling decisions ("it rains a lot here, prioritize the well").
 */
public record WeatherMemory(boolean raining, boolean thundering, long gameTime) {
}
