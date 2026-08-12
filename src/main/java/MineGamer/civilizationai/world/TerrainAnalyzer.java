package MineGamer.civilizationai.world;

import MineGamer.civilizationai.domain.building.TerrainSurvey;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Reads real terrain from a {@link ServerLevel} to produce a
 * {@link TerrainSurvey} — the "survey land" step from the spec's build
 * sequence, and the only place in the mod that samples heightmaps.
 */
public final class TerrainAnalyzer {

    private TerrainAnalyzer() {
    }

    public static TerrainSurvey survey(ServerLevel level, BlockPos origin, int width, int depth) {
        int[][] heights = new int[width][depth];
        long sum = 0;
        int count = 0;
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                heights[dx][dz] = surfaceY;
                sum += surfaceY;
                count++;
            }
        }
        int flattenHeight = count == 0 ? origin.getY() : (int) Math.round(sum / (double) count);
        return new TerrainSurvey(width, depth, heights, flattenHeight);
    }

    /** True if the highest and lowest sampled points differ by no more than {@code maxVariance}. */
    public static boolean isFlatEnough(TerrainSurvey survey, int maxVariance) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = 0; dx < survey.getWidth(); dx++) {
            for (int dz = 0; dz < survey.getDepth(); dz++) {
                int h = survey.heightAt(dx, dz);
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
        }
        return (max - min) <= maxVariance;
    }

    /** True if no column in the footprint has a fluid (water/lava) at its surface. */
    public static boolean isBuildable(ServerLevel level, BlockPos origin, int width, int depth) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                BlockPos surfacePos = new BlockPos(x, surfaceY, z);
                if (!level.getFluidState(surfacePos).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
