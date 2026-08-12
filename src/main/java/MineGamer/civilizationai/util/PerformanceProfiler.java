package MineGamer.civilizationai.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight per-label timing stats — total/average/max duration and call
 * count — with no external dependency and negligible overhead (a handful of
 * map updates per call). Not a full profiler (no call-tree, no sampling);
 * this answers "is {@code TaskScheduler} or {@code ConstructionExecutor}
 * the one spending time on this tick," which is the question that actually
 * matters for the "no TPS drops" goal, without pulling in a profiling
 * library.
 */
public final class PerformanceProfiler {

    private static final Map<String, Long> totalNanos = new ConcurrentHashMap<>();
    private static final Map<String, Long> callCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> maxNanos = new ConcurrentHashMap<>();

    private PerformanceProfiler() {
    }

    public static void record(String label, long durationNanos) {
        totalNanos.merge(label, durationNanos, Long::sum);
        callCounts.merge(label, 1L, Long::sum);
        maxNanos.merge(label, durationNanos, Math::max);
    }

    /** Runs {@code action}, recording its duration under {@code label} regardless of how it returns. */
    public static void timeVoid(String label, Runnable action) {
        long start = System.nanoTime();
        try {
            action.run();
        } finally {
            record(label, System.nanoTime() - start);
        }
    }

    /** Human-readable report, one line per label, sorted alphabetically for stable command output. */
    public static String formatReport() {
        if (totalNanos.isEmpty()) {
            return "No performance data recorded yet.";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : new TreeMap<>(totalNanos).entrySet()) {
            String label = entry.getKey();
            long total = entry.getValue();
            long count = callCounts.getOrDefault(label, 1L);
            long max = maxNanos.getOrDefault(label, 0L);
            double avgMs = (total / (double) count) / 1_000_000.0;
            double maxMs = max / 1_000_000.0;
            builder.append(String.format("%s: avg %.3fms, max %.3fms, calls %d%n", label, avgMs, maxMs, count));
        }
        return builder.toString();
    }

    public static void reset() {
        totalNanos.clear();
        callCounts.clear();
        maxNanos.clear();
    }
}
