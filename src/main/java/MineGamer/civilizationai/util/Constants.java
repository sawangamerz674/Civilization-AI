package MineGamer.civilizationai.util;

/**
 * Shared constant values used across the mod. No logic lives here on purpose —
 * this class exists purely so that string/number literals referenced from
 * multiple packages have exactly one source of truth.
 */
public final class Constants {

    private Constants() {
        // Utility class — never instantiated.
    }

    /** Must match modid in mods.toml */
    public static final String MOD_ID = "civilizationai";

    public static final String MOD_NAME = "Civilization AI";

    /** Bumped whenever CivilizationSavedData's on-disk schema changes. */
    public static final int SAVE_DATA_VERSION = 6;

    /** Bumped whenever a network packet's wire format changes. */
    public static final String NETWORK_PROTOCOL_VERSION = "1";

    /** NBT key every persisted root tag stores its schema version under. */
    public static final String NBT_KEY_SCHEMA_VERSION = "SchemaVersion";
}
