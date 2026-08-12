package MineGamer.civilizationai.save;

import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.util.ModLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.slf4j.Logger;

import net.minecraft.world.level.saveddata.SavedData;

/**
 * Central access point for {@link CivilizationSavedData}. Every subsystem
 * that needs to read or mutate persisted state should go through this class
 * rather than touching {@link DimensionDataStorage} directly — that keeps
 * the storage key and factory wiring in exactly one place.
 * <p>
 * Civilization data is stored once, attached to the overworld's data storage,
 * since a civilization simulation spans dimensions rather than belonging to
 * any single one.
 */
public final class SaveManager {

    private static final Logger LOGGER = ModLogger.get("Save");

    private SaveManager() {
    }

    /**
     * Retrieves the mod's saved data for the given server level, creating it
     * on first access. Safe to call every tick — {@link DimensionDataStorage}
     * caches the instance internally.
     */
    public static CivilizationSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(
                CivilizationSavedData::load,
                CivilizationSavedData::create,
                Constants.MOD_ID
        );
    }

    /**
     * Convenience accessor most subsystems should actually call — skips
     * straight to the {@link CivilizationManager} rather than making every
     * caller unwrap {@link CivilizationSavedData} first.
     */
    public static CivilizationManager getManager(ServerLevel level) {
        return get(level).getManager();
    }

    /**
     * Called from {@code ServerStartingEvent}. Currently only logs; reserved
     * for future phases that need to warm caches (e.g. rebuild in-memory
     * civilization indices) as soon as the overworld is available.
     */
    public static void onServerStarting(ServerLevel overworld) {
        CivilizationSavedData data = get(overworld);
        LOGGER.info("Civilization save data loaded (schema version {}).", data.getSchemaVersion());
    }

    /**
     * Called from {@code ServerStoppingEvent}. Forces a final write so no
     * in-memory state is lost if the server is killed immediately after.
     */
    public static void onServerStopping(ServerLevel overworld) {
        CivilizationSavedData data = get(overworld);
        data.setDirty();
        LOGGER.info("Civilization save data flushed.");
    }
}
