package MineGamer.civilizationai.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A route a villager has walked repeatedly. {@code timesTraveled} is what
 * Phase 5's road generation will threshold against to decide a route is
 * worth paving — this class only records the fact, it doesn't build roads.
 * <p>
 * Waypoints are stored coarsely (not every block) — callers are expected to
 * sample positions periodically rather than push one per tick, so this list
 * stays small even for long routes. It is not itself bounded, since a
 * single route's waypoint count is naturally limited by the sampling
 * interval rather than by time elapsed.
 */
public final class TravelRoute {

    private final GlobalPos start;
    private final GlobalPos end;
    private final List<BlockPos> waypoints = new ArrayList<>();
    private int timesTraveled;
    private long lastUsedGameTime;

    public TravelRoute(GlobalPos start, GlobalPos end, long gameTime) {
        this.start = start;
        this.end = end;
        this.timesTraveled = 0;
        this.lastUsedGameTime = gameTime;
    }

    public static TravelRoute reconstruct(GlobalPos start, GlobalPos end, List<BlockPos> waypoints,
                                           int timesTraveled, long lastUsedGameTime) {
        TravelRoute route = new TravelRoute(start, end, lastUsedGameTime);
        route.waypoints.addAll(waypoints);
        route.timesTraveled = timesTraveled;
        return route;
    }

    public void recordUse(long gameTime) {
        this.timesTraveled++;
        this.lastUsedGameTime = gameTime;
    }

    public void addWaypoint(BlockPos pos) {
        waypoints.add(pos);
    }

    public GlobalPos getStart() {
        return start;
    }

    public GlobalPos getEnd() {
        return end;
    }

    public List<BlockPos> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public int getTimesTraveled() {
        return timesTraveled;
    }

    public long getLastUsedGameTime() {
        return lastUsedGameTime;
    }
}
