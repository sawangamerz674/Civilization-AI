package MineGamer.civilizationai.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * A villager's fixed set of {@link PersonalityTrait}s. Immutable after
 * generation — traits represent innate disposition, not something that
 * currently changes over a villager's lifetime. (A future phase could add
 * trait drift from lived experience; that would be a new class wrapping
 * this one, not a mutation of it.)
 */
public final class PersonalityProfile {

    private final Set<PersonalityTrait> traits;

    public PersonalityProfile(Set<PersonalityTrait> traits) {
        this.traits = EnumSet.copyOf(traits.isEmpty() ? EnumSet.noneOf(PersonalityTrait.class) : traits);
    }

    public boolean has(PersonalityTrait trait) {
        return traits.contains(trait);
    }

    public Set<PersonalityTrait> traits() {
        return EnumSet.copyOf(traits);
    }

    @Override
    public String toString() {
        return "PersonalityProfile" + traits;
    }
}
