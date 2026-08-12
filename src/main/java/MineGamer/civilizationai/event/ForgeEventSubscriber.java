package MineGamer.civilizationai.event;

import MineGamer.civilizationai.ai.TaskScheduler;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.world.ConstructionExecutor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Listens on the Forge event bus for in-game/server lifecycle events.
 * <p>
 * Save init/flush are wired directly to {@link SaveManager}. The tick hook
 * runs two independent things every tick: {@link ConstructionExecutor},
 * unconditionally (block placement should look smooth regardless of AI
 * pacing), and {@link TaskScheduler}, which internally budgets how often
 * each civilization's AI actually re-evaluates. Both need the current
 * {@link MinecraftServer}, obtained via {@link ServerLifecycleHooks} since
 * {@code ServerTickEvent} doesn't carry a server reference directly.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ForgeEventSubscriber {

    private ForgeEventSubscriber() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            SaveManager.onServerStarting(overworld);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            SaveManager.onServerStopping(overworld);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ConstructionExecutor.tick(server);
            TaskScheduler.get().onServerTick(server);
        }
    }
}
