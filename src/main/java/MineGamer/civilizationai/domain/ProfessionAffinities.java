package MineGamer.civilizationai.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Which {@link PersonalityTrait}s make a villager a good fit for each
 * {@link Profession}. Used by {@link JobAssignmentService} to score
 * candidates rather than assigning purely at random — a BRAVE, DEFENSIVE
 * villager becomes a GUARD before a LAZY, SOCIAL one does, for example.
 * <p>
 * This is intentionally a simple lookup table, not a weighted scoring
 * system — the affinity score is just "how many of these traits does the
 * candidate have". That is enough to make job assignment feel
 * personality-driven without over-engineering a phase that isn't the
 * economy/balance pass.
 */
final class ProfessionAffinities {

    private static final Map<Profession, Set<PersonalityTrait>> PREFERRED_TRAITS = build();

    private ProfessionAffinities() {
    }

    static Set<PersonalityTrait> getPreferredTraits(Profession profession) {
        return PREFERRED_TRAITS.getOrDefault(profession, Set.of());
    }

    private static Map<Profession, Set<PersonalityTrait>> build() {
        Map<Profession, Set<PersonalityTrait>> map = new EnumMap<>(Profession.class);
        map.put(Profession.FARMER, EnumSet.of(PersonalityTrait.HARDWORKING, PersonalityTrait.EFFICIENT));
        map.put(Profession.LUMBERJACK, EnumSet.of(PersonalityTrait.HARDWORKING, PersonalityTrait.EXPLORER));
        map.put(Profession.MINER, EnumSet.of(PersonalityTrait.HARDWORKING, PersonalityTrait.BRAVE));
        map.put(Profession.GUARD, EnumSet.of(PersonalityTrait.BRAVE, PersonalityTrait.DEFENSIVE, PersonalityTrait.AGGRESSIVE));
        map.put(Profession.BUILDER, EnumSet.of(PersonalityTrait.BUILDER, PersonalityTrait.CREATIVE, PersonalityTrait.EFFICIENT));
        map.put(Profession.BLACKSMITH, EnumSet.of(PersonalityTrait.CREATIVE, PersonalityTrait.EFFICIENT));
        map.put(Profession.TEACHER, EnumSet.of(PersonalityTrait.SOCIAL, PersonalityTrait.GENEROUS));
        map.put(Profession.PRIEST, EnumSet.of(PersonalityTrait.SOCIAL, PersonalityTrait.GENEROUS, PersonalityTrait.LEADER));
        map.put(Profession.SCOUT, EnumSet.of(PersonalityTrait.EXPLORER, PersonalityTrait.BRAVE));
        return map;
    }
}
