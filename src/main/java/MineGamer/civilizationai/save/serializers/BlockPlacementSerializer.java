package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.construction.ConstructionStage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads/writes a {@code List<BlockPlacement>} — shared by
 * {@link BuildingConstructionSiteSerializer} and
 * {@link RoadConstructionSiteSerializer} since both persist a remaining
 * block queue in exactly this shape.
 */
final class BlockPlacementSerializer {

    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_Z = "Z";
    private static final String KEY_BLOCK_ID = "BlockId";
    private static final String KEY_STAGE = "Stage";

    private BlockPlacementSerializer() {
    }

    static ListTag write(List<BlockPlacement> placements) {
        ListTag list = new ListTag();
        for (BlockPlacement placement : placements) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(KEY_X, placement.relativePos().getX());
            tag.putInt(KEY_Y, placement.relativePos().getY());
            tag.putInt(KEY_Z, placement.relativePos().getZ());
            tag.putString(KEY_BLOCK_ID, placement.blockId());
            tag.putString(KEY_STAGE, placement.stage().name());
            list.add(tag);
        }
        return list;
    }

    static List<BlockPlacement> read(ListTag list) {
        List<BlockPlacement> placements = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            BlockPos pos = new BlockPos(tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z));
            String blockId = tag.getString(KEY_BLOCK_ID);
            ConstructionStage stage = ConstructionStage.valueOf(tag.getString(KEY_STAGE));
            placements.add(new BlockPlacement(pos, blockId, stage));
        }
        return placements;
    }

    // Package-private helper for reading a nested compound list where the caller already has TAG_COMPOUND.
    static List<BlockPlacement> read(CompoundTag parent, String key) {
        return read(parent.getList(key, Tag.TAG_COMPOUND));
    }
}
