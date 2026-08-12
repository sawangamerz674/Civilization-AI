package MineGamer.civilizationai.domain;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalityGeneratorTest {

    private static final List<Set<PersonalityTrait>> CONTRADICTIONS = List.of(
            EnumSet.of(PersonalityTrait.BRAVE, PersonalityTrait.COWARD),
            EnumSet.of(PersonalityTrait.LAZY, PersonalityTrait.HARDWORKING),
            EnumSet.of(PersonalityTrait.LEADER, PersonalityTrait.FOLLOWER),
            EnumSet.of(PersonalityTrait.AGGRESSIVE, PersonalityTrait.DEFENSIVE),
            EnumSet.of(PersonalityTrait.GREEDY, PersonalityTrait.GENEROUS)
    );

    @RepeatedTest(50)
    void generatedProfileNeverContainsBothHalvesOfAContradiction() {
        RandomSource random = RandomSource.create();
        PersonalityProfile profile = PersonalityGenerator.generate(random);

        for (Set<PersonalityTrait> pair : CONTRADICTIONS) {
            long matches = pair.stream().filter(profile::has).count();
            assertTrue(matches <= 1, "profile should not have both traits of a contradictory pair: " + pair);
        }
    }

    @Test
    void generatedProfileHasBetweenTwoAndFourTraits() {
        RandomSource random = RandomSource.create(12345L);
        PersonalityProfile profile = PersonalityGenerator.generate(random);
        int size = profile.traits().size();
        assertTrue(size >= 2 && size <= 4, "expected 2-4 traits, got " + size);
    }
}
