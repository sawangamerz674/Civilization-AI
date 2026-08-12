package MineGamer.civilizationai.domain;

import net.minecraft.core.GlobalPos;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A single civilization (village). Owns its identity, its origin point, and
 * the set of villagers currently belonging to it.
 * <p>
 * Population membership is intentionally only a set of UUIDs here — the
 * actual {@link VillagerProfile}/{@link MineGamer.civilizationai.memory.VillagerMemory}
 * objects live in {@link CivilizationManager}'s own maps, keyed by the same
 * UUID. This avoids two aggregates both claiming ownership of the same
 * profile object and keeps serialization of each concern independent.
 * <p>
 * Everything beyond identity/population (needs, resources, districts,
 * technology, ...) is added by later phases as new fields here plus a
 * schema bump — see {@code docs/ARCHITECTURE.md}.
 */
public final class Civilization {

    private final UUID id;
    private String name;
    private final GlobalPos origin;
    private final long foundedGameTime;
    private final Set<UUID> villagerIds = new LinkedHashSet<>();

    public Civilization(UUID id, String name, GlobalPos origin, long foundedGameTime) {
        this.id = id;
        this.name = name;
        this.origin = origin;
        this.foundedGameTime = foundedGameTime;
    }

    /** Reconstruction constructor used by the serializer; population is restored after. */
    public static Civilization reconstruct(UUID id, String name, GlobalPos origin, long foundedGameTime, Set<UUID> villagerIds) {
        Civilization civilization = new Civilization(id, name, origin, foundedGameTime);
        civilization.villagerIds.addAll(villagerIds);
        return civilization;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GlobalPos getOrigin() {
        return origin;
    }

    public long getFoundedGameTime() {
        return foundedGameTime;
    }

    public Set<UUID> getVillagerIds() {
        return Collections.unmodifiableSet(villagerIds);
    }

    public int getPopulation() {
        return villagerIds.size();
    }

    boolean addVillager(UUID villagerId) {
        return villagerIds.add(villagerId);
    }

    boolean removeVillager(UUID villagerId) {
        return villagerIds.remove(villagerId);
    }

    public boolean hasVillager(UUID villagerId) {
        return villagerIds.contains(villagerId);
    }
}
