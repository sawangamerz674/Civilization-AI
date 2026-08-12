package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.api.event.VillagerRegisteredEvent;
import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements "migration": once per cycle, resolves one civilization member
 * to its real live entity (villagers don't carry a position in the data
 * model — only the real entity does) and checks whether it's now
 * meaningfully closer to a different civilization's origin than its own.
 * If so, re-registers it there via {@link CivilizationManager#registerVillager},
 * which already handles leaving the old civilization.
 * <p>
 * "Meaningfully closer" means at least {@code migrationMinImprovementBlocks}
 * nearer — without a margin, a villager sitting near the midpoint of two
 * civilizations would flap back and forth every time it's re-checked.
 */
public final class MigrationService {

    public void evaluate(Civilization civilization, CivilizationManager manager, MinecraftServer server, long gameTime) {
        if (!ModConfig.COMMON.migrationEnabled.get()) {
            return;
        }

        List<UUID> ids = new ArrayList<>(civilization.getVillagerIds());
        if (ids.isEmpty()) {
            return;
        }

        ServerLevel level = server.getLevel(civilization.getOrigin().dimension());
        if (level == null) {
            return;
        }

        UUID villagerId = ids.get(level.getRandom().nextInt(ids.size()));
        Entity entity = level.getEntity(villagerId);
        if (!(entity instanceof Villager villager)) {
            return;
        }

        BlockPos currentPos = villager.blockPosition();
        double currentDistSq = currentPos.distSqr(civilization.getOrigin().pos());

        Civilization closest = null;
        double closestDistSq = currentDistSq;
        for (Civilization other : manager.getCivilizationsInDimension(civilization.getOrigin().dimension())) {
            if (other.getId().equals(civilization.getId())) {
                continue;
            }
            double distSq = currentPos.distSqr(other.getOrigin().pos());
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = other;
            }
        }

        if (closest == null) {
            return;
        }

        double improvement = Math.sqrt(currentDistSq) - Math.sqrt(closestDistSq);
        if (improvement >= ModConfig.COMMON.migrationMinImprovementBlocks.get()) {
            RandomSource random = level.getRandom();
            manager.registerVillager(villagerId, closest.getId(), gameTime, random);
            MinecraftForge.EVENT_BUS.post(new VillagerRegisteredEvent(closest.getId(), villagerId));
        }
    }
}
