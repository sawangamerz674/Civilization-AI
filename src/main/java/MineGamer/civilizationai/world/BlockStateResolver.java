package MineGamer.civilizationai.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Resolves the raw block id strings stored in {@link MineGamer.civilizationai.domain.construction.BlockPlacement}
 * to an actual {@link BlockState} against the live registry. Kept as the
 * single lookup point so {@code domain} never needs a registry reference —
 * see the Javadoc on {@code BlockPlacement} for why that separation matters.
 */
public final class BlockStateResolver {

    private BlockStateResolver() {
    }

    /** Falls back to air for an unknown id rather than throwing — a bad id shouldn't crash construction. */
    public static BlockState resolve(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(location);
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.defaultBlockState();
    }
}
