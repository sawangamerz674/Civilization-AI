package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.domain.Civilization;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Answers whether a civilization currently has a player nearby, which
 * {@link TaskScheduler} uses to decide between full-rate and LOD (reduced
 * frequency) simulation. A civilization in an unloaded dimension, or one
 * with nobody within the configured radius, is inactive.
 */
public final class ActivityTracker {

    private ActivityTracker() {
    }

    public static boolean isActive(MinecraftServer server, Civilization civilization, int radiusBlocks) {
        ResourceKey<Level> dimension = civilization.getOrigin().dimension();
        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            return false;
        }

        BlockPos origin = civilization.getOrigin().pos();
        double radiusSq = (double) radiusBlocks * (double) radiusBlocks;

        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(origin) <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
