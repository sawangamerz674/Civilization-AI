package MineGamer.civilizationai.save;

import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.util.ModLogger;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

/**
 * Upgrades a loaded {@link CompoundTag} from any previous schema version to
 * {@link Constants#SAVE_DATA_VERSION}, one step at a time, before the tag is
 * handed to {@link CivilizationSavedData#load(CompoundTag)}.
 * <p>
 * There is exactly one migration step per schema bump, applied sequentially
 * (v1→v2, then v2→v3, etc.) so that saves can skip multiple mod versions at
 * once without data loss. Phase 1 defines the version-1 baseline only; future
 * phases add a {@code migrateVxToVy} method and a branch below each time the
 * schema changes.
 */
final class SaveMigrator {

    private static final Logger LOGGER = ModLogger.get("Save");

    private SaveMigrator() {
    }

    static CompoundTag migrate(CompoundTag tag) {
        int version = tag.contains(Constants.NBT_KEY_SCHEMA_VERSION)
                ? tag.getInt(Constants.NBT_KEY_SCHEMA_VERSION)
                : 0;

        if (version == Constants.SAVE_DATA_VERSION) {
            return tag;
        }

        if (version > Constants.SAVE_DATA_VERSION) {
            LOGGER.warn("Civilization save data is from a newer mod version (schema {} > {}). " +
                    "Loading as-is; downgrading the mod is unsupported and may lose data.",
                    version, Constants.SAVE_DATA_VERSION);
            return tag;
        }

        LOGGER.info("Migrating civilization save data from schema {} to {}.", version, Constants.SAVE_DATA_VERSION);

        if (version < 2) {
            tag = migrateV1ToV2(tag);
            version = 2;
        }
        if (version < 3) {
            tag = migrateV2ToV3(tag);
            version = 3;
        }
        if (version < 4) {
            tag = migrateV3ToV4(tag);
            version = 4;
        }
        if (version < 5) {
            tag = migrateV4ToV5(tag);
            version = 5;
        }
        if (version < 6) {
            tag = migrateV5ToV6(tag);
            version = 6;
        }
        // Example of how future steps chain, left as documentation for Phase 7+:
        // if (version < 7) { tag = migrateV6ToV7(tag); version = 7; }

        tag.putInt(Constants.NBT_KEY_SCHEMA_VERSION, Constants.SAVE_DATA_VERSION);
        return tag;
    }

    /**
     * v1 (Phase 1) had no civilization/profile/memory data at all — the
     * envelope existed but carried nothing. v2 (Phase 2) introduces those
     * three lists. Nothing to convert; a v1 tag simply has none of them, and
     * {@link MineGamer.civilizationai.save.serializers.CivilizationManagerSerializer#read}
     * already treats a missing list the same as an empty one via
     * {@code CompoundTag#getList}'s default-empty behavior, so this step
     * only exists to document the boundary explicitly for future readers.
     */
    private static CompoundTag migrateV1ToV2(CompoundTag tag) {
        return tag;
    }

    /**
     * v3 (Phase 3) adds a {@code Profession} field to each villager profile.
     * {@link MineGamer.civilizationai.save.serializers.VillagerProfileSerializer#read}
     * already defaults a missing field to {@code Profession.NONE}, so — same
     * as v1→v2 — there is nothing to actively rewrite here. Kept as an
     * explicit no-op step rather than folded into v1→v2 so the schema
     * history stays one bump per phase.
     */
    private static CompoundTag migrateV2ToV3(CompoundTag tag) {
        return tag;
    }

    /**
     * v4 (Phase 4) adds the {@code ResourceLedgers} and {@code EconomyLedgers}
     * lists. A pre-v4 tag simply has neither list, and
     * {@link MineGamer.civilizationai.save.serializers.CivilizationManagerSerializer#read}
     * already treats a missing list as empty, so — same pattern as the two
     * migrations above — civilizations loaded from an older save start with
     * empty stock and neutral (1.0) prices rather than losing data, since
     * there was never any resource data to lose.
     */
    private static CompoundTag migrateV3ToV4(CompoundTag tag) {
        return tag;
    }

    /**
     * v5 (Phase 5) adds {@code Buildings}, {@code BuildingConstructionSites},
     * {@code RoadSegments}, and {@code RoadConstructionSites}. Same pattern
     * as every migration above: a pre-v5 tag has none of these lists,
     * {@link MineGamer.civilizationai.save.serializers.CivilizationManagerSerializer#read}
     * treats each missing list as empty, so civilizations loaded from an
     * older save simply start with no buildings and no roads — which is
     * exactly what was true of them before this phase existed.
     */
    private static CompoundTag migrateV4ToV5(CompoundTag tag) {
        return tag;
    }

    /**
     * v6 (Phase 6) adds {@code ReputationLedgers}, {@code Incidents}, and
     * {@code TechnologyLedgers}. Same pattern as every migration above: a
     * pre-v6 tag has none of these lists, treated as empty on read, so
     * civilizations loaded from an older save start with neutral reputation
     * (0 for every player), no active incidents, and PRIMITIVE technology —
     * exactly their real prior state, since none of this existed yet.
     */
    private static CompoundTag migrateV5ToV6(CompoundTag tag) {
        return tag;
    }
}
