package MineGamer.civilizationai.registry;

import MineGamer.civilizationai.util.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Single owner of every {@link DeferredRegister} the mod uses.
 * <p>
 * Phase 1 intentionally registers no content — the registers themselves are
 * wired up and attached to the mod event bus so that later phases (buildings,
 * job-related items, district markers, etc.) only ever need to add entries
 * here, never create a new DeferredRegister elsewhere. Centralizing this
 * avoids duplicate registry names and keeps registration order deterministic.
 */
public final class ModRegistries {

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    private ModRegistries() {
    }

    /**
     * Called once from the {@link MineGamer.civilizationai.CivilizationAI} constructor.
     * Attaches every DeferredRegister to the supplied mod event bus. Must run
     * before {@code FMLCommonSetupEvent}.
     */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }
}
