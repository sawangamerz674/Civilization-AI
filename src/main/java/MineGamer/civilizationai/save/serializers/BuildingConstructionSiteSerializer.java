package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.building.BuildingType;
import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.resource.ResourceType;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BuildingConstructionSiteSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_ORIGIN = "Origin";
    private static final String KEY_COST = "ReservedCost";
    private static final String KEY_PLAN = "RemainingPlan";
    private static final String KEY_STARTED = "StartedGameTime";
    private static final String KEY_RESOURCE_TYPE = "ResourceType";
    private static final String KEY_AMOUNT = "Amount";

    private BuildingConstructionSiteSerializer() {
    }

    public static CompoundTag write(BuildingConstructionSite site) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, site.getId());
        tag.putUUID(KEY_CIVILIZATION_ID, site.getCivilizationId());
        tag.putString(KEY_TYPE, site.getBuildingType().name());
        tag.put(KEY_ORIGIN, NbtIoUtil.writeGlobalPos(site.getOrigin()));
        tag.put(KEY_COST, writeCost(site.getReservedCost()));
        tag.put(KEY_PLAN, BlockPlacementSerializer.write(site.getRemainingPlan()));
        tag.putLong(KEY_STARTED, site.getStartedGameTime());
        return tag;
    }

    public static BuildingConstructionSite read(CompoundTag tag) {
        UUID id = tag.getUUID(KEY_ID);
        UUID civilizationId = tag.getUUID(KEY_CIVILIZATION_ID);
        BuildingType type = BuildingType.valueOf(tag.getString(KEY_TYPE));
        GlobalPos origin = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_ORIGIN));
        Map<ResourceType, Long> cost = readCost(tag.getList(KEY_COST, Tag.TAG_COMPOUND));
        List<BlockPlacement> plan = BlockPlacementSerializer.read(tag, KEY_PLAN);
        long started = tag.getLong(KEY_STARTED);
        return BuildingConstructionSite.reconstruct(id, civilizationId, type, origin, cost, plan, started);
    }

    private static ListTag writeCost(Map<ResourceType, Long> cost) {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceType, Long> entry : cost.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(KEY_RESOURCE_TYPE, entry.getKey().name());
            entryTag.putLong(KEY_AMOUNT, entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    private static Map<ResourceType, Long> readCost(ListTag list) {
        Map<ResourceType, Long> map = new EnumMap<>(ResourceType.class);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            map.put(ResourceType.valueOf(entry.getString(KEY_RESOURCE_TYPE)), entry.getLong(KEY_AMOUNT));
        }
        return map;
    }
}
