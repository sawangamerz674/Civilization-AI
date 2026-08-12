package MineGamer.civilizationai.memory;

import net.minecraft.core.GlobalPos;

/**
 * A remembered position, tagged with when it was last observed. Used for
 * home/workplace/food/favorite places/crops/animals/mines/buildings — the
 * category is the map key in {@link VillagerMemory}, not stored redundantly
 * on each entry.
 */
public record LocationMemory(GlobalPos pos, long lastSeenGameTime) {
}
