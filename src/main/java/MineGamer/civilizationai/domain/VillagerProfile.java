package MineGamer.civilizationai.domain;

import java.util.UUID;

/**
 * Persistent identity data for a single villager, independent of the live
 * entity instance (which can unload/reload constantly). Looked up by
 * villager UUID via {@link CivilizationManager}.
 * <p>
 * {@code civilizationId} is nullable: a villager can exist in the data
 * model without belonging to any civilization yet (e.g. freshly spawned,
 * not yet claimed by a village — Phase 3 decides when/how that happens).
 * <p>
 * Job/profession assignment is deliberately not modeled here yet — that's
 * introduced in Phase 3 alongside the needs-evaluation system that decides
 * it, to avoid adding a field now that would sit unused for a whole phase.
 */
public final class VillagerProfile {

    private final UUID villagerId;
    private UUID civilizationId;
    private final PersonalityProfile personality;
    private final long joinedGameTime;
    private Profession profession;

    public VillagerProfile(UUID villagerId, UUID civilizationId, PersonalityProfile personality, long joinedGameTime) {
        this(villagerId, civilizationId, personality, joinedGameTime, Profession.NONE);
    }

    /** Full constructor, used by the serializer to restore an exact prior state including profession. */
    public VillagerProfile(UUID villagerId, UUID civilizationId, PersonalityProfile personality,
                            long joinedGameTime, Profession profession) {
        this.villagerId = villagerId;
        this.civilizationId = civilizationId;
        this.personality = personality;
        this.joinedGameTime = joinedGameTime;
        this.profession = profession;
    }

    public UUID getVillagerId() {
        return villagerId;
    }

    public UUID getCivilizationId() {
        return civilizationId;
    }

    void setCivilizationId(UUID civilizationId) {
        this.civilizationId = civilizationId;
    }

    public PersonalityProfile getPersonality() {
        return personality;
    }

    public long getJoinedGameTime() {
        return joinedGameTime;
    }

    public Profession getProfession() {
        return profession;
    }

    void setProfession(Profession profession) {
        this.profession = profession;
    }

    public boolean isAssigned() {
        return civilizationId != null;
    }
}
