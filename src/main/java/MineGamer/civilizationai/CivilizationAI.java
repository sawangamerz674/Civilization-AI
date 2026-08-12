package MineGamer.civilizationai;

import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.registry.ModRegistries;
import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.util.ModLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Mod entry point.
 * <p>
 * Responsibilities, and only these — everything else is delegated to a
 * dedicated subsystem package so this class never grows past a short,
 * readable bootstrap sequence:
 * <ol>
 *     <li>Attach {@link ModRegistries}'s DeferredRegisters to the mod bus.</li>
 *     <li>Register the COMMON config spec.</li>
 *     <li>Nothing else — {@link MineGamer.civilizationai.event.ModEventBusSubscriber}
 *         and {@link MineGamer.civilizationai.event.ForgeEventSubscriber} are
 *         auto-discovered via {@code @Mod.EventBusSubscriber} and pick up
 *         from here.</li>
 * </ol>
 */
@Mod(Constants.MOD_ID)
public final class CivilizationAI {

    private static final Logger LOGGER = ModLogger.root();

    public CivilizationAI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistries.init(modEventBus);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("{} initializing.", Constants.MOD_NAME);
    }
}

