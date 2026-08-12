package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.building.Building;
import MineGamer.civilizationai.domain.building.BuildingType;
import net.minecraft.nbt.CompoundTag;

public final class BuildingSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_ORIGIN = "Origin";
    private static final String KEY_COMPLETED = "CompletedGameTime";

    private BuildingSerializer() {
    }

    public static CompoundTag write(Building building) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, building.id());
        tag.putUUID(KEY_CIVILIZATION_ID, building.civilizationId());
        tag.putString(KEY_TYPE, building.type().name());
        tag.put(KEY_ORIGIN, NbtIoUtil.writeGlobalPos(building.origin()));
        tag.putLong(KEY_COMPLETED, building.completedGameTime());
        return tag;
    }

    public static Building read(CompoundTag tag) {
        return new Building(
                tag.getUUID(KEY_ID),
                tag.getUUID(KEY_CIVILIZATION_ID),
                BuildingType.valueOf(tag.getString(KEY_TYPE)),
                NbtIoUtil.readGlobalPos(tag.getCompound(KEY_ORIGIN)),
                tag.getLong(KEY_COMPLETED)
        );
    }
}
