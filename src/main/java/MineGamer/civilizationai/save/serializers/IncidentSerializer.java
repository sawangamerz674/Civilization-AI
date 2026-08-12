package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.incident.Incident;
import MineGamer.civilizationai.domain.incident.IncidentType;
import net.minecraft.nbt.CompoundTag;

public final class IncidentSerializer {

    private static final String KEY_ID = "Id";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_TRIGGERED = "TriggeredGameTime";
    private static final String KEY_EXPIRES = "ExpiresGameTime";

    private IncidentSerializer() {
    }

    public static CompoundTag write(Incident incident) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, incident.id());
        tag.putUUID(KEY_CIVILIZATION_ID, incident.civilizationId());
        tag.putString(KEY_TYPE, incident.type().name());
        tag.putLong(KEY_TRIGGERED, incident.triggeredGameTime());
        tag.putLong(KEY_EXPIRES, incident.expiresGameTime());
        return tag;
    }

    public static Incident read(CompoundTag tag) {
        return new Incident(
                tag.getUUID(KEY_ID),
                tag.getUUID(KEY_CIVILIZATION_ID),
                IncidentType.valueOf(tag.getString(KEY_TYPE)),
                tag.getLong(KEY_TRIGGERED),
                tag.getLong(KEY_EXPIRES)
        );
    }
}
