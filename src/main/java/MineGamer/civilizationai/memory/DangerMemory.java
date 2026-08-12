package MineGamer.civilizationai.memory;

import net.minecraft.core.GlobalPos;

/**
 * A remembered source of danger (a mob encounter, environmental hazard,
 * etc). {@code dangerType} is a free-form identifier rather than an enum —
 * Phase 3+ AI and later mod-provided danger sources (via the API in Phase 7)
 * both need to be able to record new kinds of danger without a schema
 * change here.
 */
public record DangerMemory(GlobalPos pos, String dangerType, long gameTime) {
}
