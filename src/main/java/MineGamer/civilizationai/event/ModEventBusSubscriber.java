package MineGamer.civilizationai.event;

import MineGamer.civilizationai.network.NetworkHandler;
import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.util.ModLogger;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Listens on the mod event bus (registry-timed lifecycle events, as opposed
 * to the Forge bus used for in-game events). Registration of blocks/items is
 * handled by {@link MineGamer.civilizationai.registry.ModRegistries} directly via
 * DeferredRegister; this class handles the one-time setup step that must run
 * after registries are populated but before the server/client is fully live.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEventBusSubscriber {

    private static final Logger LOGGER = ModLogger.get("Setup");

    private ModEventBusSubscriber() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // enqueueWork guarantees this runs after all mods finish registering,
        // which NetworkHandler.register() depends on.
        event.enqueueWork(() -> {
            NetworkHandler.register();
            LOGGER.info("{} common setup complete.", Constants.MOD_NAME);
        });
    }
}
