package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.road.RoadSegment;
import net.minecraft.nbt.CompoundTag;

public final class RoadSegmentSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_START = "Start";
    private static final String KEY_END = "End";
    private static final String KEY_COMPLETED = "CompletedGameTime";

    private RoadSegmentSerializer() {
    }

    public static CompoundTag write(RoadSegment segment) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, segment.id());
        tag.putUUID(KEY_CIVILIZATION_ID, segment.civilizationId());
        tag.put(KEY_START, NbtIoUtil.writeGlobalPos(segment.start()));
        tag.put(KEY_END, NbtIoUtil.writeGlobalPos(segment.end()));
        tag.putLong(KEY_COMPLETED, segment.completedGameTime());
        return tag;
    }

    public static RoadSegment read(CompoundTag tag) {
        return new RoadSegment(
                tag.getUUID(KEY_ID),
                tag.getUUID(KEY_CIVILIZATION_ID),
                NbtIoUtil.readGlobalPos(tag.getCompound(KEY_START)),
                NbtIoUtil.readGlobalPos(tag.getCompound(KEY_END)),
                tag.getLong(KEY_COMPLETED)
        );
    }
}
