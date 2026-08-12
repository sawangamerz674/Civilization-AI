package MineGamer.civilizationai.save;

import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.save.serializers.CivilizationManagerSerializer;
import MineGamer.civilizationai.util.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The single root persisted object per {@code ServerLevel}. Owns the
 * {@link CivilizationManager} — every civilization, villager profile, and
 * villager memory in this level — plus the schema-versioned envelope around
 * it.
 * <p>
 * The manager is constructed with {@code this::setDirty} as its dirty
 * marker, so every mutation made anywhere through
 * {@link CivilizationManager} (creating a civilization, registering a
 * villager, recording a memory, ...) automatically flags this SavedData for
 * writing — no calling code needs to remember to do so itself.
 */
public class CivilizationSavedData extends SavedData {

    public static final String DATA_NAME = Constants.MOD_ID;

    private final int schemaVersion;
    private final CivilizationManager manager;

    public CivilizationSavedData() {
        this.schemaVersion = Constants.SAVE_DATA_VERSION;
        this.manager = new CivilizationManager(this::setDirty);
    }

    private CivilizationSavedData(int schemaVersion, CivilizationManager manager) {
        this.schemaVersion = schemaVersion;
        this.manager = manager;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    /** The single entry point for every civilization/villager read or mutation in this level. */
    public CivilizationManager getManager() {
        return manager;
    }

    /**
     * Factory used by {@link net.minecraft.world.level.storage.DimensionDataStorage}
     * when no saved data exists yet for this level.
     */
    public static CivilizationSavedData create() {
        return new CivilizationSavedData();
    }

    /**
     * Factory used by {@link net.minecraft.world.level.storage.DimensionDataStorage}
     * to reconstruct saved data from disk. Runs {@link SaveMigrator} first so
     * that every loaded instance is guaranteed to be on the current schema
     * version by the time simulation code touches it.
     */
    public static CivilizationSavedData load(CompoundTag tag) {
        CompoundTag migrated = SaveMigrator.migrate(tag);
        int version = migrated.getInt(Constants.NBT_KEY_SCHEMA_VERSION);

        // The dirty-marker Runnable is bound after construction below, since
        // SavedData#setDirty isn't available until `data` exists. A small
        // indirection array lets the lambda passed to the manager capture a
        // reference that's filled in immediately after construction.
        CivilizationSavedData[] holder = new CivilizationSavedData[1];
        CivilizationManager manager = CivilizationManagerSerializer.read(migrated, () -> {
            if (holder[0] != null) {
                holder[0].setDirty();
            }
        });
        CivilizationSavedData data = new CivilizationSavedData(version, manager);
        holder[0] = data;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(Constants.NBT_KEY_SCHEMA_VERSION, Constants.SAVE_DATA_VERSION);
        CivilizationManagerSerializer.write(manager, tag);
        return tag;
    }
}
