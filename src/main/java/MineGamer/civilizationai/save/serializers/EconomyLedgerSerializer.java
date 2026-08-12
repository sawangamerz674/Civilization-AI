package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reads/writes a single {@link EconomyLedger} to/from NBT.
 */
public final class EconomyLedgerSerializer {

    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_MULTIPLIERS = "PriceMultipliers";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_MULTIPLIER = "Multiplier";

    private EconomyLedgerSerializer() {
    }

    public static CompoundTag write(EconomyLedger ledger) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_CIVILIZATION_ID, ledger.getCivilizationId());

        ListTag list = new ListTag();
        for (Map.Entry<ResourceType, Double> entry : ledger.getAllMultipliers().entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(KEY_TYPE, entry.getKey().name());
            entryTag.putDouble(KEY_MULTIPLIER, entry.getValue());
            list.add(entryTag);
        }
        tag.put(KEY_MULTIPLIERS, list);

        return tag;
    }

    public static EconomyLedger read(CompoundTag tag) {
        UUID civilizationId = tag.getUUID(KEY_CIVILIZATION_ID);

        Map<ResourceType, Double> multipliers = new EnumMap<>(ResourceType.class);
        ListTag list = tag.getList(KEY_MULTIPLIERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            multipliers.put(ResourceType.valueOf(entry.getString(KEY_TYPE)), entry.getDouble(KEY_MULTIPLIER));
        }

        return EconomyLedger.reconstruct(civilizationId, multipliers);
    }
}
