package MineGamer.civilizationai.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Searches an expanding square ring around a center point for a footprint
 * that's simultaneously: not already claimed (per {@code occupancyCheck} —
 * typically "doesn't overlap any existing building"), free of surface
 * fluid, and flat enough. The ring step is the larger of width/depth so
 * candidate footprints don't trivially overlap each other as the search
 * expands.
 */
public final class SiteSelector {

    private SiteSelector() {
    }

    public static Optional<BlockPos> findSite(ServerLevel level, BlockPos searchCenter, int width, int depth,
                                                int searchRadius, int maxVariance, Predicate<BlockPos> occupancyCheck) {
        int step = Math.max(width, depth);

        for (int radius = 0; radius <= searchRadius; radius += step) {
            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    // Only test the ring boundary — interior points were already tested at a smaller radius.
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    BlockPos candidate = searchCenter.offset(dx, 0, dz);
                    if (!occupancyCheck.test(candidate)) {
                        continue;
                    }
                    if (!TerrainAnalyzer.isBuildable(level, candidate, width, depth)) {
                        continue;
                    }

                    var survey = TerrainAnalyzer.survey(level, candidate, width, depth);
                    if (!TerrainAnalyzer.isFlatEnough(survey, maxVariance)) {
                        continue;
                    }

                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }
}
