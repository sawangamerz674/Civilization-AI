package MineGamer.civilizationai.domain.technology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnologyTest {

    @Test
    void primitiveIsFirstTierWithZeroThreshold() {
        assertEquals(0, Technology.PRIMITIVE.getProsperityThreshold());
        assertEquals(Technology.STONE_ROADS, Technology.PRIMITIVE.next());
    }

    @Test
    void tiersAreStrictlySequentialByThreshold() {
        Technology[] tiers = Technology.values();
        for (int i = 1; i < tiers.length; i++) {
            assertTrue(tiers[i].getProsperityThreshold() > tiers[i - 1].getProsperityThreshold(),
                    tiers[i] + " should require more prosperity than " + tiers[i - 1]);
        }
    }

    @Test
    void librariesIsTheFinalTierWithNoNext() {
        assertEquals(Technology.LIBRARIES, Technology.values()[Technology.values().length - 1]);
        assertNull(Technology.LIBRARIES.next());
    }

    @Test
    void ledgerStartsAtPrimitiveAndTracksUnlockedTiers() {
        TechnologyLedger ledger = new TechnologyLedger(java.util.UUID.randomUUID());
        assertEquals(Technology.PRIMITIVE, ledger.getCurrentTier());
        assertTrue(ledger.hasUnlocked(Technology.PRIMITIVE));
        assertTrue(!ledger.hasUnlocked(Technology.STONE_ROADS));

        ledger.setCurrentTier(Technology.WATER_WELLS);
        assertTrue(ledger.hasUnlocked(Technology.STONE_ROADS), "unlocking a later tier implies every earlier tier is unlocked");
        assertTrue(ledger.hasUnlocked(Technology.WATER_WELLS));
        assertTrue(!ledger.hasUnlocked(Technology.LARGE_FARMS));
    }
}
