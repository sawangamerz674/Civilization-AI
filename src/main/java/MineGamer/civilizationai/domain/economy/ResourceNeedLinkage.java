package MineGamer.civilizationai.domain.economy;

import MineGamer.civilizationai.domain.needs.NeedType;
import MineGamer.civilizationai.domain.resource.ResourceType;

import java.util.Map;
import java.util.Optional;

/**
 * Which {@link NeedType} a resource's scarcity should pressure. Most
 * resources map to the one job that produces them — all of MINER's
 * secondary yields (COAL, IRON, COPPER, GOLD, EMERALD, DIAMOND) link back
 * to the STONE need, since staffing more miners is the only lever that
 * increases their supply, same as it is for stone itself.
 * <p>
 * LEATHER, GLASS, and CLAY are deliberately absent: no current profession
 * produces them (they'd come from animal husbandry, sand-smelting, or
 * riverbank gathering respectively), so there is no job to pressure yet.
 * Their prices still move in {@link EconomyLedger} — a future phase that
 * adds a producing profession only needs to add an entry here, not change
 * how the linkage is consumed.
 */
final class ResourceNeedLinkage {

    private static final Map<ResourceType, NeedType> LINKS = Map.ofEntries(
            Map.entry(ResourceType.WOOD, NeedType.WOOD),
            Map.entry(ResourceType.STONE, NeedType.STONE),
            Map.entry(ResourceType.FOOD, NeedType.FOOD),
            Map.entry(ResourceType.SEEDS, NeedType.FOOD),
            Map.entry(ResourceType.COAL, NeedType.STONE),
            Map.entry(ResourceType.IRON, NeedType.STONE),
            Map.entry(ResourceType.COPPER, NeedType.STONE),
            Map.entry(ResourceType.GOLD, NeedType.STONE),
            Map.entry(ResourceType.EMERALD, NeedType.STONE),
            Map.entry(ResourceType.DIAMOND, NeedType.STONE)
    );

    private ResourceNeedLinkage() {
    }

    static Optional<NeedType> getLinkedNeed(ResourceType type) {
        return Optional.ofNullable(LINKS.get(type));
    }
}
