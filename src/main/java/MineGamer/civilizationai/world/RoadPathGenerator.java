package MineGamer.civilizationai.world;

import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.construction.ConstructionStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Turns a {@link MineGamer.civilizationai.memory.TravelRoute}'s waypoints into a
 * ground-following sequence of road placements.
 * <p>
 * This intentionally does not do real pathfinding around obstacles — "roads
 * avoid cliffs" is implemented as an honest refusal (this method returns
 * {@link Optional#empty()} for the whole route) whenever a segment's
 * endpoint-to-endpoint height difference exceeds {@code maxSlope}, rather
 * than computing a route around the cliff. Within the accepted slope range,
 * single-block elevation changes get a stair block and sudden dips or
 * fluid crossings get bridged at the last stable height. A production-
 * quality implementation would search for a lower-slope alternative path
 * instead of refusing outright; that's a reasonable future enhancement,
 * not something this phase claims to do.
 */
public final class RoadPathGenerator {

    private RoadPathGenerator() {
    }

    public static Optional<List<BlockPlacement>> generate(ServerLevel level, GlobalPos start, List<BlockPos> waypoints,
                                                            GlobalPos end, int maxSlope, int bridgeDropThreshold,
                                                            String surfaceBlock, String stairBlock, String bridgeBlock) {
        List<BlockPos> points = new ArrayList<>();
        points.add(start.pos());
        points.addAll(waypoints);
        points.add(end.pos());

        List<BlockPlacement> plan = new ArrayList<>();
        BlockPos origin = start.pos();
        int previousPlacedHeight = groundHeight(level, origin.getX(), origin.getZ());

        for (int i = 0; i < points.size() - 1; i++) {
            BlockPos a = points.get(i);
            BlockPos b = points.get(i + 1);

            int heightA = groundHeight(level, a.getX(), a.getZ());
            int heightB = groundHeight(level, b.getX(), b.getZ());
            if (Math.abs(heightB - heightA) > maxSlope) {
                return Optional.empty();
            }

            int steps = Math.max(Math.abs(b.getX() - a.getX()), Math.abs(b.getZ() - a.getZ()));
            steps = Math.max(steps, 1);

            for (int step = 1; step <= steps; step++) {
                double t = step / (double) steps;
                int x = a.getX() + (int) Math.round((b.getX() - a.getX()) * t);
                int z = a.getZ() + (int) Math.round((b.getZ() - a.getZ()) * t);
                int sampledHeight = groundHeight(level, x, z);
                boolean fluid = !level.getFluidState(new BlockPos(x, sampledHeight, z)).isEmpty();

                int placeY;
                String placeBlock;
                if (fluid || Math.abs(sampledHeight - previousPlacedHeight) >= bridgeDropThreshold) {
                    placeY = previousPlacedHeight;
                    placeBlock = bridgeBlock;
                } else if (sampledHeight - previousPlacedHeight == 1 || sampledHeight - previousPlacedHeight == -1) {
                    placeY = sampledHeight;
                    placeBlock = stairBlock;
                    previousPlacedHeight = sampledHeight;
                } else if (sampledHeight == previousPlacedHeight) {
                    placeY = previousPlacedHeight;
                    placeBlock = surfaceBlock;
                } else {
                    // A local jump of 2+ blocks that the segment-level slope check let through
                    // (terrain noise between sparse waypoints) — smooth over it at the last
                    // stable height rather than attempting a multi-block ramp.
                    placeY = previousPlacedHeight;
                    placeBlock = bridgeBlock;
                }

                BlockPos relative = new BlockPos(x - origin.getX(), placeY - origin.getY(), z - origin.getZ());
                plan.add(new BlockPlacement(relative, placeBlock, ConstructionStage.PAVING));
            }
        }

        return Optional.of(plan);
    }

    private static int groundHeight(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
    }
}
