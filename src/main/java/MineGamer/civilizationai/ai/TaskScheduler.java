package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.util.PerformanceProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Drives {@link CivilizationBrain} evaluation once per server tick, budgeted
 * so a server with hundreds of villagers across many civilizations never
 * evaluates all of them in a single tick.
 * <p>
 * Per civilization, this tracks the last game-time it was evaluated
 * (in-memory only — re-evaluating everything shortly after a world load is
 * cheap and not worth persisting) and only evaluates it again once its
 * interval has elapsed: {@code simulationTicksPerEvaluation} if
 * {@link ActivityTracker} reports a nearby player, otherwise the much
 * longer {@code inactiveVillageSimulationInterval} (LOD).
 * <p>
 * Within a single tick, civilizations are only evaluated until the running
 * total of their populations would exceed {@code maxVillagersProcessedPerTick}
 * — a civilization skipped this way is simply retried on a later tick,
 * since its {@code lastEvaluationTick} entry is left unchanged.
 * <p>
 * Singleton by construction (mirrors {@link MineGamer.civilizationai.network.NetworkHandler}'s
 * single-channel pattern) since there is exactly one scheduler per running
 * server, called from {@link MineGamer.civilizationai.event.ForgeEventSubscriber}.
 */
public final class TaskScheduler {

    private static final TaskScheduler INSTANCE = new TaskScheduler();

    public static TaskScheduler get() {
        return INSTANCE;
    }

    private final Map<UUID, Long> lastEvaluationTick = new HashMap<>();

    private TaskScheduler() {
    }

    public void onServerTick(MinecraftServer server) {
        PerformanceProfiler.timeVoid("TaskScheduler.tick", () -> onServerTickInternal(server));
    }

    private void onServerTickInternal(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        CivilizationManager manager = SaveManager.getManager(overworld);
        Collection<Civilization> civilizations = manager.getAllCivilizations();
        pruneStaleEntries(civilizations);
        if (civilizations.isEmpty()) {
            return;
        }

        long gameTime = overworld.getGameTime();
        int budget = ModConfig.COMMON.maxVillagersProcessedPerTick.get();
        int activityRadius = ModConfig.COMMON.civilizationActivityRadius.get();
        RandomSource random = overworld.getRandom();

        int processed = 0;
        for (Civilization civilization : civilizations) {
            boolean active = ActivityTracker.isActive(server, civilization, activityRadius);
            int interval = active
                    ? ModConfig.COMMON.simulationTicksPerEvaluation.get()
                    : ModConfig.COMMON.inactiveVillageSimulationInterval.get();

            long last = lastEvaluationTick.getOrDefault(civilization.getId(), Long.MIN_VALUE);
            boolean due = last == Long.MIN_VALUE || (gameTime - last) >= interval;
            if (!due) {
                continue;
            }

            if (processed > 0 && processed + civilization.getPopulation() > budget) {
                // Over budget for this tick; leave lastEvaluationTick untouched
                // so this civilization is retried as soon as a future tick has room.
                continue;
            }

            CivilizationBrain.evaluate(civilization, manager, server, gameTime, random);
            lastEvaluationTick.put(civilization.getId(), gameTime);
            processed += civilization.getPopulation();
        }
    }

    private void pruneStaleEntries(Collection<Civilization> civilizations) {
        if (lastEvaluationTick.isEmpty()) {
            return;
        }
        Set<UUID> validIds = civilizations.stream().map(Civilization::getId).collect(Collectors.toSet());
        lastEvaluationTick.keySet().removeIf(id -> !validIds.contains(id));
    }
}
