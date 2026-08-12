package MineGamer.civilizationai.domain.needs;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.Profession;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises {@link JobRatioNeedsEvaluator} against a plain {@link CivilizationManager}
 * with no {@link net.minecraft.server.level.ServerLevel} or running server —
 * exactly the "domain stays testable without a running game" claim made
 * throughout {@code docs/ARCHITECTURE.md}.
 */
class JobRatioNeedsEvaluatorTest {

    // Vanilla registries (touched below by ResourceKey.create) require the game's
    // vanilla data to have been bootstrapped, which normally happens when a real
    // client/server starts. Plain JUnit runs don't do that, so it must happen here
    // first — in a static initializer, so it runs before OVERWORLD's field
    // initializer below, not in a @BeforeAll (which only runs after the class,
    // and thus OVERWORLD, has already finished initializing).
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final ResourceKey<Level> OVERWORLD =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation("minecraft", "overworld"));

    @Test
    void emptyPopulationHasNoNeeds() {
        CivilizationManager manager = new CivilizationManager(() -> { });
        Civilization civilization = manager.createCivilization("Test", GlobalPos.of(OVERWORLD, BlockPos.ZERO), 0);

        List<NeedScore> needs = new JobRatioNeedsEvaluator().evaluate(civilization, manager);
        assertTrue(needs.isEmpty());
    }

    @Test
    void unstaffedPopulationHasAFoodNeedSortedFirst() {
        CivilizationManager manager = new CivilizationManager(() -> { });
        Civilization civilization = manager.createCivilization("Test", GlobalPos.of(OVERWORLD, BlockPos.ZERO), 0);
        RandomSource random = RandomSource.create(1L);

        for (int i = 0; i < 5; i++) {
            manager.registerVillager(UUID.randomUUID(), civilization.getId(), 0, random);
        }

        List<NeedScore> needs = new JobRatioNeedsEvaluator().evaluate(civilization, manager);
        assertFalse(needs.isEmpty());
        for (int i = 1; i < needs.size(); i++) {
            assertTrue(needs.get(i - 1).priority() >= needs.get(i).priority(), "needs must be sorted most urgent first");
        }
        assertTrue(needs.stream().anyMatch(score -> score.type() == NeedType.FOOD),
                "5 unstaffed villagers should register a FOOD need");
    }

    @Test
    void fullyStaffedFoodNeedIsSatisfiedForASinglePopulationVillage() {
        CivilizationManager manager = new CivilizationManager(() -> { });
        Civilization civilization = manager.createCivilization("Test", GlobalPos.of(OVERWORLD, BlockPos.ZERO), 0);
        RandomSource random = RandomSource.create(2L);

        UUID villagerId = UUID.randomUUID();
        manager.registerVillager(villagerId, civilization.getId(), 0, random);
        manager.assignProfession(villagerId, Profession.FARMER);

        List<NeedScore> needs = new JobRatioNeedsEvaluator().evaluate(civilization, manager);
        assertTrue(needs.stream().noneMatch(score -> score.type() == NeedType.FOOD),
                "a 1-population village with its only villager farming shouldn't still need a farmer");
    }
}
