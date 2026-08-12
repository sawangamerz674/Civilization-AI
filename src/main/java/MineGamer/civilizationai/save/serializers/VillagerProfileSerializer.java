package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.domain.PersonalityProfile;
import MineGamer.civilizationai.domain.PersonalityTrait;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.VillagerProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Reads/writes a single {@link VillagerProfile} to/from NBT, including its
 * embedded {@link PersonalityProfile} and (Phase 3+) its {@link Profession}.
 */
public final class VillagerProfileSerializer {

    private static final String KEY_VILLAGER_ID = "VillagerId";
    private static final String KEY_CIVILIZATION_ID = "CivilizationId";
    private static final String KEY_TRAITS = "Traits";
    private static final String KEY_JOINED = "JoinedGameTime";
    private static final String KEY_PROFESSION = "Profession";

    private VillagerProfileSerializer() {
    }

    public static CompoundTag write(VillagerProfile profile) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_VILLAGER_ID, profile.getVillagerId());
        if (profile.getCivilizationId() != null) {
            tag.putUUID(KEY_CIVILIZATION_ID, profile.getCivilizationId());
        }
        tag.put(KEY_TRAITS, writeTraits(profile.getPersonality()));
        tag.putLong(KEY_JOINED, profile.getJoinedGameTime());
        tag.putString(KEY_PROFESSION, profile.getProfession().name());
        return tag;
    }

    public static VillagerProfile read(CompoundTag tag) {
        UUID villagerId = tag.getUUID(KEY_VILLAGER_ID);
        UUID civilizationId = tag.hasUUID(KEY_CIVILIZATION_ID) ? tag.getUUID(KEY_CIVILIZATION_ID) : null;
        PersonalityProfile personality = readTraits(tag.getList(KEY_TRAITS, Tag.TAG_STRING));
        long joined = tag.getLong(KEY_JOINED);
        // Saves from before Phase 3 (schema < 3) have no Profession key; default to NONE.
        Profession profession = tag.contains(KEY_PROFESSION) ? Profession.valueOf(tag.getString(KEY_PROFESSION)) : Profession.NONE;
        return new VillagerProfile(villagerId, civilizationId, personality, joined, profession);
    }

    private static ListTag writeTraits(PersonalityProfile personality) {
        ListTag list = NbtIoUtil.newList();
        for (PersonalityTrait trait : personality.traits()) {
            list.add(StringTag.valueOf(trait.name()));
        }
        return list;
    }

    private static PersonalityProfile readTraits(ListTag list) {
        Set<PersonalityTrait> traits = EnumSet.noneOf(PersonalityTrait.class);
        for (int i = 0; i < list.size(); i++) {
            traits.add(PersonalityTrait.valueOf(list.getString(i)));
        }
        return new PersonalityProfile(traits);
    }
}
