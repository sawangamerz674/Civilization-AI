package MineGamer.civilizationai.event;

import MineGamer.civilizationai.entity.VillagerRegistrationService;
import MineGamer.civilizationai.util.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Listens for real {@link Villager} entities joining or dying and delegates
 * to {@link VillagerRegistrationService} — the thinnest possible bridge
 * between Forge's entity events and this mod's data model, kept free of any
 * logic itself so the actual registration/death-handling rules stay
 * testable in {@code VillagerRegistrationService} without a running game.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class VillagerLifecycleEventHandler {

    private static final VillagerRegistrationService REGISTRATION_SERVICE = new VillagerRegistrationService();

    private VillagerLifecycleEventHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        REGISTRATION_SERVICE.handleVillagerJoin(serverLevel, villager);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        REGISTRATION_SERVICE.handleVillagerDeath(serverLevel, villager, event.getSource());
    }
}
