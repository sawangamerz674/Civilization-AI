package MineGamer.civilizationai.api;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.incident.IncidentTriggerService;
import MineGamer.civilizationai.domain.incident.IncidentType;
import MineGamer.civilizationai.domain.technology.Technology;
import MineGamer.civilizationai.save.SaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The stable, read-oriented surface other mods should use rather than
 * reaching into {@code CivilizationManager} directly — {@code CivilizationManager}
 * is this mod's internal implementation and can change shape between
 * versions; this class is the compatibility boundary.
 * <p>
 * Every method resolves the manager itself (via {@link SaveManager}, always
 * attached to the overworld regardless of which level is passed — see that
 * class's Javadoc), so callers never need to know that detail.
 */
public final class CivilizationAIApi {

    private static final IncidentTriggerService INCIDENT_TRIGGER_SERVICE = new IncidentTriggerService();

    private CivilizationAIApi() {
    }

    /** The nearest civilization to {@code pos} in {@code level}'s dimension, within {@code radiusBlocks}, if any. */
    public static Optional<Civilization> getNearestCivilization(ServerLevel level, BlockPos pos, int radiusBlocks) {
        CivilizationManager manager = manager(level);
        double radiusSq = (double) radiusBlocks * radiusBlocks;

        Civilization nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Civilization candidate : manager.getCivilizationsInDimension(level.dimension())) {
            double distSq = pos.distSqr(candidate.getOrigin().pos());
            if (distSq <= radiusSq && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return Optional.ofNullable(nearest);
    }

    public static Optional<Civilization> getCivilization(ServerLevel level, UUID civilizationId) {
        return manager(level).getCivilization(civilizationId);
    }

    /** Which civilization, if any, a tracked villager UUID currently belongs to. */
    public static Optional<Civilization> getCivilizationOf(ServerLevel level, UUID villagerId) {
        CivilizationManager manager = manager(level);
        return manager.getProfile(villagerId)
                .map(profile -> profile.getCivilizationId())
                .flatMap(manager::getCivilization);
    }

    public static Collection<Civilization> getAllCivilizations(ServerLevel level) {
        return manager(level).getAllCivilizations();
    }

    /** A civilization's collective opinion of a player (see {@code domain.reputation.ReputationLedger}). Defaults to 0. */
    public static int getReputation(ServerLevel level, UUID civilizationId, UUID playerId) {
        return manager(level).getReputationLedger(civilizationId)
                .map(ledger -> ledger.getReputation(playerId))
                .orElse(0);
    }

    public static Technology getTechnologyTier(ServerLevel level, UUID civilizationId) {
        return manager(level).getTechnologyLedger(civilizationId)
                .map(ledger -> ledger.getCurrentTier())
                .orElse(Technology.PRIMITIVE);
    }

    /**
     * Lets another mod trigger one of this mod's own incidents (e.g. a
     * compat mod representing an in-progress raid from its own system could
     * call this with {@link IncidentType#BANDIT_RAID} to make this mod's
     * SAFETY-priority response kick in). Respects the same dedup rules as
     * every internal caller — see {@link IncidentTriggerService#triggerIncident}.
     */
    public static void triggerIncident(ServerLevel level, UUID civilizationId, IncidentType type) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        CivilizationManager manager = SaveManager.getManager(overworld);
        INCIDENT_TRIGGER_SERVICE.triggerIncident(manager, civilizationId, type, overworld.getGameTime());
    }

    private static CivilizationManager manager(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        return SaveManager.getManager(overworld);
    }
}
