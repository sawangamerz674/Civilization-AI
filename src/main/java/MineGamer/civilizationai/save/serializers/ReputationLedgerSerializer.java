package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.reputation.ReputationLedger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ReputationLedgerSerializer {

    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_REPUTATION = "Reputation";
    private static final String KEY_PLAYER_ID = "PlayerId";
    private static final String KEY_VALUE = "Value";

    private ReputationLedgerSerializer() {
    }

    public static CompoundTag write(ReputationLedger ledger) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_CIVILIZATION_ID, ledger.getCivilizationId());

        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : ledger.getAllReputation().entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(KEY_PLAYER_ID, entry.getKey());
            entryTag.putInt(KEY_VALUE, entry.getValue());
            list.add(entryTag);
        }
        tag.put(KEY_REPUTATION, list);

        return tag;
    }

    public static ReputationLedger read(CompoundTag tag) {
        UUID civilizationId = tag.getUUID(KEY_CIVILIZATION_ID);

        Map<UUID, Integer> reputation = new HashMap<>();
        ListTag list = tag.getList(KEY_REPUTATION, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            reputation.put(entry.getUUID(KEY_PLAYER_ID), entry.getInt(KEY_VALUE));
        }

        return ReputationLedger.reconstruct(civilizationId, reputation);
    }
}
