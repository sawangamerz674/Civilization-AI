package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.PersonalityTrait;
import MineGamer.civilizationai.domain.VillagerProfile;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Picks two random villagers in a civilization each cycle and nudges their
 * mutual relationship — finally giving Phase 2's
 * {@code VillagerMemory.relationships} map a writer. One interaction per
 * cycle, not an all-pairs sweep, for the same performance reason every
 * other planner in this mod stays incremental: a village's social fabric
 * should feel like it develops over time, not get computed exhaustively
 * every evaluation.
 * <p>
 * The sign and magnitude of the interaction are influenced by personality:
 * SOCIAL and GENEROUS villagers skew interactions positive; AGGRESSIVE and
 * GREEDY skew them negative. This is deliberately simple weighting, not a
 * simulation of what the interaction "was about."
 */
public final class RelationshipSimulator {

    public void evaluate(Civilization civilization, CivilizationManager manager, RandomSource random) {
        if (!ModConfig.COMMON.relationshipSimulationEnabled.get()) {
            return;
        }

        List<UUID> ids = new ArrayList<>(civilization.getVillagerIds());
        if (ids.size() < 2) {
            return;
        }

        UUID a = ids.get(random.nextInt(ids.size()));
        UUID b = ids.get(random.nextInt(ids.size()));
        if (a.equals(b)) {
            return;
        }

        VillagerProfile profileA = manager.getProfile(a).orElse(null);
        VillagerProfile profileB = manager.getProfile(b).orElse(null);
        int delta = computeDelta(profileA, profileB, random);

        manager.getOrCreateMemory(a).adjustRelationship(b, delta);
        manager.getOrCreateMemory(b).adjustRelationship(a, delta);
        manager.markDirty();
    }

    private int computeDelta(VillagerProfile a, VillagerProfile b, RandomSource random) {
        int positiveWeight = 7;
        int negativeWeight = 3;

        if (hasTrait(a, PersonalityTrait.SOCIAL) || hasTrait(b, PersonalityTrait.SOCIAL)) {
            positiveWeight += 2;
        }
        if (hasTrait(a, PersonalityTrait.GENEROUS) || hasTrait(b, PersonalityTrait.GENEROUS)) {
            positiveWeight += 1;
        }
        if (hasTrait(a, PersonalityTrait.AGGRESSIVE) || hasTrait(b, PersonalityTrait.AGGRESSIVE)) {
            negativeWeight += 2;
        }
        if (hasTrait(a, PersonalityTrait.GREEDY) || hasTrait(b, PersonalityTrait.GREEDY)) {
            negativeWeight += 1;
        }

        boolean positive = random.nextInt(positiveWeight + negativeWeight) < positiveWeight;
        return positive ? 1 + random.nextInt(3) : -(1 + random.nextInt(2));
    }

    private boolean hasTrait(VillagerProfile profile, PersonalityTrait trait) {
        return profile != null && profile.getPersonality().has(trait);
    }
}
