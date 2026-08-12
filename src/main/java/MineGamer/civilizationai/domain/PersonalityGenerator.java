package MineGamer.civilizationai.domain;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a {@link PersonalityProfile} for a newly created villager.
 * <p>
 * Rolls a random number of traits (2–4) from the full trait pool, skipping
 * any trait whose contradicting counterpart has already been picked, so a
 * villager is never simultaneously e.g. BRAVE and COWARD.
 */
public final class PersonalityGenerator {

    /** Minimum number of traits rolled per villager. */
    private static final int MIN_TRAITS = 2;

    /** Maximum number of traits rolled per villager. */
    private static final int MAX_TRAITS = 4;

    /**
     * Trait pairs that must never both appear on the same villager. Order
     * within a pair doesn't matter — both directions are checked.
     */
    private static final List<Set<PersonalityTrait>> CONTRADICTIONS = List.of(
            EnumSet.of(PersonalityTrait.BRAVE, PersonalityTrait.COWARD),
            EnumSet.of(PersonalityTrait.LAZY, PersonalityTrait.HARDWORKING),
            EnumSet.of(PersonalityTrait.LEADER, PersonalityTrait.FOLLOWER),
            EnumSet.of(PersonalityTrait.AGGRESSIVE, PersonalityTrait.DEFENSIVE),
            EnumSet.of(PersonalityTrait.GREEDY, PersonalityTrait.GENEROUS)
    );

    private PersonalityGenerator() {
    }

    public static PersonalityProfile generate(RandomSource random) {
        List<PersonalityTrait> pool = new ArrayList<>(List.of(PersonalityTrait.values()));
        java.util.Collections.shuffle(pool, new java.util.Random(random.nextLong()));

        int targetCount = MIN_TRAITS + random.nextInt(MAX_TRAITS - MIN_TRAITS + 1);
        Set<PersonalityTrait> chosen = EnumSet.noneOf(PersonalityTrait.class);

        for (PersonalityTrait candidate : pool) {
            if (chosen.size() >= targetCount) {
                break;
            }
            if (conflictsWithAny(candidate, chosen)) {
                continue;
            }
            chosen.add(candidate);
        }

        return new PersonalityProfile(chosen);
    }

    private static boolean conflictsWithAny(PersonalityTrait candidate, Set<PersonalityTrait> chosen) {
        for (Set<PersonalityTrait> pair : CONTRADICTIONS) {
            if (pair.contains(candidate)) {
                for (PersonalityTrait opposite : pair) {
                    if (opposite != candidate && chosen.contains(opposite)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
