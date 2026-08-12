package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.Civilization;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Reads/writes a single {@link Civilization} to/from NBT. Does not know
 * about the manager or the wider save file — {@link CivilizationManagerSerializer}
 * calls this once per civilization.
 */
public final class CivilizationSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_NAME = "Name";
    private static final String KEY_ORIGIN = "Origin";
    private static final String KEY_FOUNDED = "FoundedGameTime";
    private static final String KEY_VILLAGERS = "Villagers";

    private CivilizationSerializer() {
    }

    public static CompoundTag write(Civilization civilization) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, civilization.getId());
        tag.putString(KEY_NAME, civilization.getName());
        tag.put(KEY_ORIGIN, NbtIoUtil.writeGlobalPos(civilization.getOrigin()));
        tag.putLong(KEY_FOUNDED, civilization.getFoundedGameTime());

        ListTag villagerList = NbtIoUtil.newList();
        for (UUID villagerId : civilization.getVillagerIds()) {
            villagerList.add(StringTag.valueOf(villagerId.toString()));
        }
        tag.put(KEY_VILLAGERS, villagerList);

        return tag;
    }

    public static Civilization read(CompoundTag tag) {
        UUID id = tag.getUUID(KEY_ID);
        String name = tag.getString(KEY_NAME);
        GlobalPos origin = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_ORIGIN));
        long founded = tag.getLong(KEY_FOUNDED);

        Set<UUID> villagerIds = new HashSet<>();
        ListTag villagerList = tag.getList(KEY_VILLAGERS, Tag.TAG_STRING);
        for (int i = 0; i < villagerList.size(); i++) {
            villagerIds.add(UUID.fromString(villagerList.getString(i)));
        }

        return Civilization.reconstruct(id, name, origin, founded, villagerIds);
    }
}
