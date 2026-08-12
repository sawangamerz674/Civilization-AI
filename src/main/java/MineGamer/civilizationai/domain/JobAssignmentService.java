package MineGamer.civilizationai.domain;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Assigns one villager in a civilization to a target {@link Profession}.
 * <p>
 * Only ever pulls from villagers whose current profession is {@link Profession#NONE}
 * — this phase deliberately never poaches a villager from one job to
 * another. That keeps behavior predictable and avoids job-assignment
 * thrashing (a civilization endlessly reshuffling its own workforce every
 * evaluation cycle). Rebalancing an overstaffed department is left as a
 * documented candidate for a later phase once real resource shortages
 * (Phase 4) can justify it.
 * <p>
 * Among eligible candidates, the one with the highest {@link ProfessionAffinities}
 * match wins; ties are broken randomly so equally-suited villagers don't
 * always resolve in UUID iteration order.
 */
public final class JobAssignmentService {

    public Optional<UUID> assignBestFit(Civilization civilization, CivilizationManager manager,
                                         Profession targetProfession, RandomSource random) {
        List<VillagerProfile> unemployed = new ArrayList<>();
        for (UUID villagerId : civilization.getVillagerIds()) {
            manager.getProfile(villagerId)
                    .filter(profile -> profile.getProfession() == Profession.NONE)
                    .ifPresent(unemployed::add);
        }

        if (unemployed.isEmpty()) {
            return Optional.empty();
        }

        Set<PersonalityTrait> preferred = ProfessionAffinities.getPreferredTraits(targetProfession);

        int bestScore = -1;
        List<VillagerProfile> bestCandidates = new ArrayList<>();
        for (VillagerProfile candidate : unemployed) {
            int score = affinityScore(candidate, preferred);
            if (score > bestScore) {
                bestScore = score;
                bestCandidates.clear();
                bestCandidates.add(candidate);
            } else if (score == bestScore) {
                bestCandidates.add(candidate);
            }
        }

        VillagerProfile chosen = bestCandidates.get(random.nextInt(bestCandidates.size()));
        manager.assignProfession(chosen.getVillagerId(), targetProfession);
        return Optional.of(chosen.getVillagerId());
    }

    private int affinityScore(VillagerProfile candidate, Set<PersonalityTrait> preferredTraits) {
        int score = 0;
        for (PersonalityTrait trait : preferredTraits) {
            if (candidate.getPersonality().has(trait)) {
                score++;
            }
        }
        return score;
    }
}
