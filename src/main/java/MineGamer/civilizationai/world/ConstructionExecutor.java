package MineGamer.civilizationai.world;

import MineGamer.civilizationai.api.event.BuildingCompletedEvent;
import MineGamer.civilizationai.api.event.RoadCompletedEvent;
import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.construction.ConstructionJob;
import MineGamer.civilizationai.domain.road.RoadConstructionSite;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.util.PerformanceProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs every server tick (not on the AI evaluation cadence — see
 * {@code docs/ARCHITECTURE.md}) and places a fair share of a global
 * per-tick block budget for every active {@link ConstructionJob} across
 * every civilization, in every dimension. The total budget is scaled by
 * {@code constructionSpeedMultiplier} (1.0 = normal speed) before being
 * split.
 * <p>
 * A job whose origin chunk isn't currently loaded is skipped entirely for
 * the tick — its queue is left untouched rather than losing placements —
 * so an unloaded/unvisited build site simply waits rather than silently
 * dropping progress.
 */
public final class ConstructionExecutor {

    private static final int SET_BLOCK_FLAGS = Block.UPDATE_ALL;

    private ConstructionExecutor() {
    }

    public static void tick(MinecraftServer server) {
        PerformanceProfiler.timeVoid("ConstructionExecutor.tick", () -> tickInternal(server));
    }

    private static void tickInternal(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        CivilizationManager manager = SaveManager.getManager(overworld);
        List<ConstructionJob> activeJobs = manager.getAllActiveConstructionJobs();
        if (activeJobs.isEmpty()) {
            return;
        }

        int totalBudget = ModConfig.COMMON.maxBlockPlacementsPerTick.get();
        double speedMultiplier = ModConfig.COMMON.constructionSpeedMultiplier.get();
        int effectiveBudget = Math.max(1, (int) Math.round(totalBudget * speedMultiplier));
        int perJobShare = Math.max(1, effectiveBudget / activeJobs.size());
        int remaining = effectiveBudget;

        List<ConstructionJob> completedThisTick = new ArrayList<>();
        boolean anyPlacementMade = false;

        for (ConstructionJob job : activeJobs) {
            if (remaining <= 0) {
                break;
            }

            GlobalPos origin = job.getOrigin();
            ResourceKey<Level> dimension = origin.dimension();
            ServerLevel jobLevel = server.getLevel(dimension);
            if (jobLevel == null || !jobLevel.isLoaded(origin.pos())) {
                continue;
            }

            int allowance = Math.min(perJobShare, remaining);
            List<BlockPlacement> batch = job.takeNextBatch(allowance);
            if (batch.isEmpty()) {
                continue;
            }

            for (BlockPlacement placement : batch) {
                BlockPos target = origin.pos().offset(placement.relativePos());
                if (!jobLevel.isLoaded(target)) {
                    continue;
                }
                BlockState state = BlockStateResolver.resolve(placement.blockId());
                jobLevel.setBlock(target, state, SET_BLOCK_FLAGS);
            }
            remaining -= batch.size();
            anyPlacementMade = true;

            if (job.isComplete()) {
                completedThisTick.add(job);
            }
        }

        if (!completedThisTick.isEmpty()) {
            long gameTime = overworld.getGameTime();
            for (ConstructionJob job : completedThisTick) {
                job.onComplete(manager, gameTime);
                manager.completeConstructionJob(job);
                postCompletionEvent(job);
            }
        }

        if (anyPlacementMade || !completedThisTick.isEmpty()) {
            manager.markDirty();
        }
    }

    /** Part of this mod's public API — see {@code api.event.BuildingCompletedEvent}/{@code RoadCompletedEvent}. */
    private static void postCompletionEvent(ConstructionJob job) {
        if (job instanceof BuildingConstructionSite site) {
            MinecraftForge.EVENT_BUS.post(new BuildingCompletedEvent(
                    site.getCivilizationId(), site.getBuildingType(), site.getOrigin()));
        } else if (job instanceof RoadConstructionSite site) {
            MinecraftForge.EVENT_BUS.post(new RoadCompletedEvent(
                    site.getCivilizationId(), site.getStart(), site.getEnd()));
        }
    }
}
