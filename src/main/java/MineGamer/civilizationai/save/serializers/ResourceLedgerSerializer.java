package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reads/writes a single {@link ResourceLedger} to/from NBT — both its stock
 * and its reservations, so an in-progress reservation (once Phase 5 starts
 * creating them) survives a save/reload rather than silently vanishing.
 */
public final class ResourceLedgerSerializer {

    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_STOCK = "Stock";
    private static final String KEY_RESERVED = "Reserved";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_AMOUNT = "Amount";

    private ResourceLedgerSerializer() {
    }

    public static CompoundTag write(ResourceLedger ledger) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_CIVILIZATION_ID, ledger.getCivilizationId());
        tag.put(KEY_STOCK, writeAmounts(ledger.getAllStock()));
        tag.put(KEY_RESERVED, writeAmounts(ledger.getAllReserved()));
        return tag;
    }

    public static ResourceLedger read(CompoundTag tag) {
        UUID civilizationId = tag.getUUID(KEY_CIVILIZATION_ID);
        Map<ResourceType, Long> stock = readAmounts(tag.getList(KEY_STOCK, Tag.TAG_COMPOUND));
        Map<ResourceType, Long> reserved = readAmounts(tag.getList(KEY_RESERVED, Tag.TAG_COMPOUND));
        return ResourceLedger.reconstruct(civilizationId, stock, reserved);
    }

    private static ListTag writeAmounts(Map<ResourceType, Long> amounts) {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceType, Long> entry : amounts.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(KEY_TYPE, entry.getKey().name());
            entryTag.putLong(KEY_AMOUNT, entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    private static Map<ResourceType, Long> readAmounts(ListTag list) {
        Map<ResourceType, Long> amounts = new EnumMap<>(ResourceType.class);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            amounts.put(ResourceType.valueOf(entry.getString(KEY_TYPE)), entry.getLong(KEY_AMOUNT));
        }
        return amounts;
    }
}
