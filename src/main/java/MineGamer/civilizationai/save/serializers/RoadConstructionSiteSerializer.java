package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.road.RoadConstructionSite;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

public final class RoadConstructionSiteSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_START = "Start";
    private static final String KEY_END = "End";
    private static final String KEY_PLAN = "RemainingPlan";

    private RoadConstructionSiteSerializer() {
    }

    public static CompoundTag write(RoadConstructionSite site) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, site.getId());
        tag.putUUID(KEY_CIVILIZATION_ID, site.getCivilizationId());
        tag.put(KEY_START, NbtIoUtil.writeGlobalPos(site.getStart()));
        tag.put(KEY_END, NbtIoUtil.writeGlobalPos(site.getEnd()));
        tag.put(KEY_PLAN, BlockPlacementSerializer.write(site.getRemainingPlan()));
        return tag;
    }

    public static RoadConstructionSite read(CompoundTag tag) {
        UUID id = tag.getUUID(KEY_ID);
        UUID civilizationId = tag.getUUID(KEY_CIVILIZATION_ID);
        GlobalPos start = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_START));
        GlobalPos end = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_END));
        List<BlockPlacement> plan = BlockPlacementSerializer.read(tag, KEY_PLAN);
        return RoadConstructionSite.reconstruct(id, civilizationId, start, end, plan);
    }
}
