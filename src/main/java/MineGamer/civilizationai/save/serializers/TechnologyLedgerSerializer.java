package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.technology.Technology;
import MineGamer.civilizationai.domain.technology.TechnologyLedger;
import net.minecraft.nbt.CompoundTag;

public final class TechnologyLedgerSerializer {

    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_CURRENT_TIER = "CurrentTier";

    private TechnologyLedgerSerializer() {
    }

    public static CompoundTag write(TechnologyLedger ledger) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_CIVILIZATION_ID, ledger.getCivilizationId());
        tag.putString(KEY_CURRENT_TIER, ledger.getCurrentTier().name());
        return tag;
    }

    public static TechnologyLedger read(CompoundTag tag) {
        return TechnologyLedger.reconstruct(
                tag.getUUID(KEY_CIVILIZATION_ID),
                Technology.valueOf(tag.getString(KEY_CURRENT_TIER))
        );
    }
}
