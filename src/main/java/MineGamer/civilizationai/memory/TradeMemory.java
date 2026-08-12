package MineGamer.civilizationai.memory;

import java.util.UUID;

/**
 * A remembered trade. {@code partnerId} is either another villager's UUID
 * or a player's UUID — both share the same UUID space in Minecraft, so no
 * separate "is player" flag is needed here; callers that care can resolve
 * the UUID against the level's entity/player lookup.
 */
public record TradeMemory(UUID partnerId, String itemId, int quantity, long gameTime) {
}
