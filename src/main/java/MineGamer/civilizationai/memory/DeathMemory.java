package MineGamer.civilizationai.memory;

import net.minecraft.core.GlobalPos;

import java.util.UUID;

/**
 * A remembered death of a fellow villager, as witnessed or learned by this
 * villager. {@code cause} is free-form (e.g. "raid", "starvation", "fall")
 * for the same extensibility reason as {@link DangerMemory#dangerType()}.
 */
public record DeathMemory(UUID deceasedVillagerId, String cause, GlobalPos pos, long gameTime) {
}
