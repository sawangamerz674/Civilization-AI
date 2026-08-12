package MineGamer.civilizationai.domain.resource;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceLedgerTest {

    @Test
    void depositIncreasesStockAndAvailable() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.WOOD, 10, 1000);
        assertEquals(10, ledger.getStock(ResourceType.WOOD));
        assertEquals(10, ledger.getAvailable(ResourceType.WOOD));
    }

    @Test
    void depositIsCappedAtCapacity() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.WOOD, 50, 30);
        assertEquals(30, ledger.getStock(ResourceType.WOOD));
        ledger.deposit(ResourceType.WOOD, 50, 30);
        assertEquals(30, ledger.getStock(ResourceType.WOOD), "further deposits past capacity should be lost, not overflow");
    }

    @Test
    void reserveReducesAvailableButNotStock() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.STONE, 20, 1000);

        assertTrue(ledger.reserve(ResourceType.STONE, 5));
        assertEquals(20, ledger.getStock(ResourceType.STONE));
        assertEquals(15, ledger.getAvailable(ResourceType.STONE));
        assertEquals(5, ledger.getReserved(ResourceType.STONE));
    }

    @Test
    void reserveFailsWhenInsufficientAvailable() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.IRON, 3, 1000);
        assertFalse(ledger.reserve(ResourceType.IRON, 10));
        assertEquals(0, ledger.getReserved(ResourceType.IRON), "a failed reservation must not partially apply");
    }

    @Test
    void commitReservationConsumesBothReservedAndStock() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.WOOD, 20, 1000);
        ledger.reserve(ResourceType.WOOD, 8);

        assertTrue(ledger.commitReservation(ResourceType.WOOD, 8));
        assertEquals(12, ledger.getStock(ResourceType.WOOD));
        assertEquals(0, ledger.getReserved(ResourceType.WOOD));
        assertEquals(12, ledger.getAvailable(ResourceType.WOOD));
    }

    @Test
    void releaseReservationReturnsMaterialToAvailablePool() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.WOOD, 20, 1000);
        ledger.reserve(ResourceType.WOOD, 8);

        ledger.releaseReservation(ResourceType.WOOD, 8);
        assertEquals(20, ledger.getStock(ResourceType.WOOD));
        assertEquals(20, ledger.getAvailable(ResourceType.WOOD));
        assertEquals(0, ledger.getReserved(ResourceType.WOOD));
    }

    @Test
    void withdrawUnreservedFailsIfWouldDipIntoReservedMaterial() {
        ResourceLedger ledger = new ResourceLedger(UUID.randomUUID());
        ledger.deposit(ResourceType.FOOD, 10, 1000);
        ledger.reserve(ResourceType.FOOD, 6);

        assertFalse(ledger.withdrawUnreserved(ResourceType.FOOD, 5), "only 4 is available; reserved food must stay untouched");
        assertEquals(10, ledger.getStock(ResourceType.FOOD));
    }

    @Test
    void reconstructRestoresExactStockAndReservations() {
        UUID civId = UUID.randomUUID();
        ResourceLedger original = new ResourceLedger(civId);
        original.deposit(ResourceType.GOLD, 5, 1000);
        original.reserve(ResourceType.GOLD, 2);

        ResourceLedger restored = ResourceLedger.reconstruct(civId, original.getAllStock(), original.getAllReserved());
        assertEquals(5, restored.getStock(ResourceType.GOLD));
        assertEquals(2, restored.getReserved(ResourceType.GOLD));
        assertEquals(civId, restored.getCivilizationId());
    }
}
