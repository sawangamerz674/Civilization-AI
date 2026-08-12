package MineGamer.civilizationai.domain.construction;

import MineGamer.civilizationai.domain.CivilizationManager;
import net.minecraft.core.GlobalPos;

import java.util.List;
import java.util.UUID;

/**
 * A queue of {@link BlockPlacement}s to be placed incrementally, plus what
 * happens when the queue is empty. Both
 * {@link MineGamer.civilizationai.domain.building.BuildingConstructionSite} and
 * {@link MineGamer.civilizationai.domain.road.RoadConstructionSite} implement
 * this, which is what lets {@link MineGamer.civilizationai.world.ConstructionExecutor}
 * drive every in-progress building and road in a level through one shared
 * loop and one shared per-tick block budget, instead of two parallel
 * execution paths.
 */
public interface ConstructionJob {

    UUID getId();

    UUID getCivilizationId();

    /** The absolute world position every {@link BlockPlacement#relativePos()} in this job is relative to. */
    GlobalPos getOrigin();

    boolean isComplete();

    /**
     * Removes and returns up to {@code maxCount} placements from the front
     * of the queue. Callers must have already confirmed the job's chunk is
     * loaded — this method has no way to "give a placement back" if it
     * can't be applied, so skipping unloaded jobs entirely is the caller's
     * responsibility (see {@code ConstructionExecutor}).
     */
    List<BlockPlacement> takeNextBatch(int maxCount);

    /**
     * Called exactly once, when {@link #isComplete()} first becomes true.
     * Implementations use this to register their finished result (a
     * {@code Building} or {@code RoadSegment}) and release/commit any
     * reserved resources — whatever turns "in progress" into "done."
     */
    void onComplete(CivilizationManager manager, long gameTime);
}
