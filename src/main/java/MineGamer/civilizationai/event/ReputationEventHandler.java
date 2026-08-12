package MineGamer.civilizationai.event;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.VillagerProfile;
import MineGamer.civilizationai.domain.reputation.ReputationEvent;
import MineGamer.civilizationai.domain.reputation.ReputationService;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.util.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * Records the three player actions this mod can actually detect as
 * {@link ReputationEvent}s — see that enum's Javadoc for why "stealing"
 * isn't among them. All three checks are gated by {@code reputationEnabled}.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ReputationEventHandler {

    private static final ReputationService REPUTATION_SERVICE = new ReputationService();

    private ReputationEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!ModConfig.COMMON.reputationEnabled.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        recordVillagerEvent(villager, player, ReputationEvent.ATTACKED_VILLAGER);
    }

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!ModConfig.COMMON.reputationEnabled.get()) {
            return;
        }
        if (!(event.getAbstractVillager() instanceof Villager villager)) {
            return;
        }
        recordVillagerEvent(villager, event.getEntity(), ReputationEvent.TRADED_WITH_VILLAGER);
    }

    /** A player killing a hostile mob near a civilization counts as defending it, even without a tracked villager involved. */
    @SubscribeEvent
    public static void onHostileDeath(LivingDeathEvent event) {
        if (!ModConfig.COMMON.reputationEnabled.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        CivilizationManager manager = SaveManager.getManager(overworld);

        BlockPos deathPos = event.getEntity().blockPosition();
        int radius = ModConfig.COMMON.defenseThreatRadius.get();
        double radiusSq = (double) radius * radius;

        for (Civilization civilization : manager.getCivilizationsInDimension(serverLevel.dimension())) {
            if (deathPos.distSqr(civilization.getOrigin().pos()) <= radiusSq) {
                REPUTATION_SERVICE.recordEvent(manager, civilization.getId(), player.getUUID(), ReputationEvent.DEFENDED_VILLAGE);
            }
        }
    }

    private static void recordVillagerEvent(Villager villager, Player player, ReputationEvent event) {
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        CivilizationManager manager = SaveManager.getManager(overworld);

        manager.getProfile(villager.getUUID())
                .map(VillagerProfile::getCivilizationId)
                .filter(Objects::nonNull)
                .ifPresent(civilizationId -> REPUTATION_SERVICE.recordEvent(manager, civilizationId, player.getUUID(), event));
    }
}
