package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.incident.IncidentTriggerService;
import MineGamer.civilizationai.domain.incident.IncidentType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Implements "villages detect hostile civilizations" with a real check —
 * counts actual {@link Monster} entities within {@code defenseThreatRadius}
 * blocks of a civilization's origin, and if that count meets an
 * effective threshold (the configured {@code defenseThreatThreshold},
 * scaled down as {@code difficultyScalar} rises — higher difficulty means
 * fewer hostiles are needed to count as a raid), triggers a BANDIT_RAID
 * incident via {@link IncidentTriggerService#triggerIncident}, which in
 * turn boosts SAFETY priority through
 * {@link MineGamer.civilizationai.domain.incident.DefenseAwareNeedsEvaluator}
 * — "train guards" in response to a real, not scripted, threat.
 * <p>
 * "Build walls," "patrol roads," "repair defenses," and "retreat civilians"
 * from the spec's WAR SYSTEM section are not implemented — each needs
 * entity AI/pathing control this mod never adds (no phase overrides
 * villager goals or movement). GUARD_TOWER construction (Phase 5) already
 * responds to a staffed GUARD, which is the closest this mod gets to "build
 * walls" without inventing a new building type this late.
 */
public final class DefenseService {

    private final IncidentTriggerService incidentTriggerService;

    public DefenseService(IncidentTriggerService incidentTriggerService) {
        this.incidentTriggerService = incidentTriggerService;
    }

    public void evaluate(Civilization civilization, CivilizationManager manager, MinecraftServer server, long gameTime) {
        if (!ModConfig.COMMON.warSystemEnabled.get()) {
            return;
        }

        ServerLevel level = server.getLevel(civilization.getOrigin().dimension());
        if (level == null) {
            return;
        }

        int radius = ModConfig.COMMON.defenseThreatRadius.get();
        BlockPos origin = civilization.getOrigin().pos();
        AABB area = new AABB(origin).inflate(radius);
        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area);

        // Higher difficulty means fewer hostiles are needed to count as a raid.
        double difficulty = Math.max(0.1, ModConfig.COMMON.difficultyScalar.get());
        int effectiveThreshold = Math.max(1, (int) Math.round(ModConfig.COMMON.defenseThreatThreshold.get() / difficulty));

        if (hostiles.size() >= effectiveThreshold) {
            incidentTriggerService.triggerIncident(manager, civilization.getId(), IncidentType.BANDIT_RAID, gameTime);
        }
    }
}
