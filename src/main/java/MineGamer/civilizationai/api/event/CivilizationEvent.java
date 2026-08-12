package MineGamer.civilizationai.api.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

/**
 * Base type for every event this mod posts to {@code MinecraftForge.EVENT_BUS}.
 * This is the mod's public "register AI behaviors" extension point from the
 * spec's API section — other mods add a compile-time dependency on this
 * mod's jar, subscribe to whichever of these events they care about, and
 * react. No source changes to this mod are needed for that.
 * <p>
 * These are plain Forge events, not cancelable — they're notifications that
 * something already happened (a civilization was created, a building
 * finished), not requests for permission to let it happen.
 */
public abstract class CivilizationEvent extends Event {

    private final UUID civilizationId;

    protected CivilizationEvent(UUID civilizationId) {
        this.civilizationId = civilizationId;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }
}
