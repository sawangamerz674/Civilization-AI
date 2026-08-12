package MineGamer.civilizationai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Thin wrapper around a per-subsystem SLF4J logger.
 *
 * Every major subsystem (AI, Save, Network, Economy, ...) should call
 * {@link #get(String)} once, statically, and reuse the returned instance.
 * This keeps every log line prefixed consistently as
 * {@code [CivilizationAI/<subsystem>]} without every class needing to know
 * about markers or formatting conventions.
 */
public final class ModLogger {

    private static final Marker ROOT_MARKER = MarkerFactory.getMarker(Constants.MOD_NAME);

    private ModLogger() {
    }

    /**
     * @param subsystem short human-readable subsystem name, e.g. "Save", "Network", "AI"
     * @return a logger whose name is namespaced under the mod id
     */
    public static Logger get(String subsystem) {
        return LoggerFactory.getLogger(Constants.MOD_ID + "." + subsystem);
    }

    /** Root logger for top-level mod lifecycle messages (bootstrap, shutdown). */
    public static Logger root() {
        return LoggerFactory.getLogger(Constants.MOD_ID);
    }

    public static Marker marker() {
        return ROOT_MARKER;
    }
}
