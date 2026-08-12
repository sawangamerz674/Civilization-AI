package MineGamer.civilizationai.domain.construction;

import net.minecraft.core.BlockPos;

/**
 * One block to place, expressed relative to whatever origin the owning
 * {@link ConstructionJob} was sited at — this is what lets the same plan be
 * generated once (in {@code domain.building}/{@code world}) and executed
 * against an absolute world position later without recomputing anything.
 * <p>
 * {@code blockId} is a raw registry name string (e.g.
 * {@code "minecraft:oak_planks"}) rather than a {@link net.minecraft.world.level.block.Block}
 * reference, so this class — like the rest of {@code domain} — has no
 * dependency on live registry state and stays trivially serializable.
 * {@link MineGamer.civilizationai.world.BlockStateResolver} does the lookup at
 * execution time.
 */
public record BlockPlacement(BlockPos relativePos, String blockId, ConstructionStage stage) {
}
