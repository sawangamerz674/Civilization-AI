package MineGamer.civilizationai.domain.resource;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks one civilization's stock of every {@link ResourceType}, plus how
 * much of each is currently reserved.
 * <p>
 * "Reserved" material is still physically in storage but earmarked — this
 * is the mechanism the spec describes as "citizens reserve materials before
 * construction." Phase 4 only provides the primitive operations
 * ({@link #reserve}, {@link #commitReservation}, {@link #releaseReservation});
 * nothing in this phase actually reserves anything yet, since there is no
 * building planner to do so until Phase 5. {@link #getAvailable} is what
 * everything else (trading, future consumption) should check against, not
 * {@link #getStock}, so a resource earmarked for a half-built house can't
 * simultaneously be sold or eaten.
 */
public final class ResourceLedger {

    private final UUID civilizationId;
    private final Map<ResourceType, Long> stock = new EnumMap<>(ResourceType.class);
    private final Map<ResourceType, Long> reserved = new EnumMap<>(ResourceType.class);

    public ResourceLedger(UUID civilizationId) {
        this.civilizationId = civilizationId;
    }

    /** Reconstruction used by the serializer to restore exact prior stock/reservation state. */
    public static ResourceLedger reconstruct(UUID civilizationId, Map<ResourceType, Long> stock,
                                              Map<ResourceType, Long> reserved) {
        ResourceLedger ledger = new ResourceLedger(civilizationId);
        ledger.stock.putAll(stock);
        ledger.reserved.putAll(reserved);
        return ledger;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }

    public long getStock(ResourceType type) {
        return stock.getOrDefault(type, 0L);
    }

    public long getReserved(ResourceType type) {
        return reserved.getOrDefault(type, 0L);
    }

    /** Stock minus whatever is already reserved — the true amount free to use. */
    public long getAvailable(ResourceType type) {
        return getStock(type) - getReserved(type);
    }

    /**
     * Adds to stock, capped at {@code capacity} — production beyond a
     * civilization's storage capacity is simply lost. Amounts of zero or
     * less are a no-op rather than an error, since callers (like
     * probabilistic mining yields) frequently compute a zero roll.
     */
    public void deposit(ResourceType type, long amount, long capacity) {
        if (amount <= 0) {
            return;
        }
        long updated = Math.min(capacity, getStock(type) + amount);
        stock.put(type, updated);
    }

    /**
     * Earmarks {@code amount} of {@code type} without removing it from
     * stock. Fails (returns false, no state change) if less than
     * {@code amount} is currently available.
     */
    public boolean reserve(ResourceType type, long amount) {
        if (amount <= 0) {
            return true;
        }
        if (getAvailable(type) < amount) {
            return false;
        }
        reserved.merge(type, amount, Long::sum);
        return true;
    }

    /** Releases a reservation without consuming the material (e.g. a cancelled build). */
    public void releaseReservation(ResourceType type, long amount) {
        long updated = Math.max(0, getReserved(type) - amount);
        if (updated == 0) {
            reserved.remove(type);
        } else {
            reserved.put(type, updated);
        }
    }

    /**
     * Consumes a reservation — removes it from both {@code reserved} and
     * {@code stock} (e.g. a build actually completing). Fails if the
     * reservation isn't at least {@code amount}.
     */
    public boolean commitReservation(ResourceType type, long amount) {
        if (getReserved(type) < amount) {
            return false;
        }
        releaseReservation(type, amount);
        stock.put(type, Math.max(0, getStock(type) - amount));
        return true;
    }

    /**
     * Consumes stock directly without ever having been reserved (e.g. food
     * being eaten). Fails if less than {@code amount} is available.
     */
    public boolean withdrawUnreserved(ResourceType type, long amount) {
        if (getAvailable(type) < amount) {
            return false;
        }
        stock.put(type, Math.max(0, getStock(type) - amount));
        return true;
    }

    public Map<ResourceType, Long> getAllStock() {
        return Map.copyOf(stock);
    }

    public Map<ResourceType, Long> getAllReserved() {
        return Map.copyOf(reserved);
    }
}
