package MineGamer.civilizationai.domain.building;

import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.resource.ResourceType;

import java.util.Map;

/**
 * A kind of building a civilization can construct. Each entry carries
 * everything {@link BuildingBlueprintGenerator} needs to procedurally shape
 * one — footprint, height, and a small block palette — rather than pointing
 * at a fixed schematic file. This is a deliberately small, fixed set (7
 * types); like {@link Profession}, a registry-based API for other mods to
 * add building types is Phase 7 territory.
 * <p>
 * {@code triggerProfession} is the profession whose presence signals this
 * building is needed (see {@link MineGamer.civilizationai.ai.BuildingPlanner}) —
 * {@code null} for HOUSE and WAREHOUSE, which are triggered by population
 * and stock pressure respectively rather than any one job.
 */
public enum BuildingType {

    HOUSE(5, 5, 4, DistrictType.RESIDENTIAL, null,
            Map.of(ResourceType.WOOD, 20L, ResourceType.STONE, 10L),
            "minecraft:cobblestone", "minecraft:oak_planks", "minecraft:oak_slab",
            "minecraft:torch", "minecraft:potted_poppy"),

    WAREHOUSE(7, 7, 5, DistrictType.STORAGE, null,
            Map.of(ResourceType.WOOD, 15L, ResourceType.STONE, 25L),
            "minecraft:stone_bricks", "minecraft:stone_bricks", "minecraft:stone_brick_slab",
            "minecraft:torch", null),

    WORKSHOP(5, 5, 4, DistrictType.INDUSTRIAL, Profession.BLACKSMITH,
            Map.of(ResourceType.STONE, 20L, ResourceType.IRON, 5L),
            "minecraft:cobblestone", "minecraft:stone_bricks", "minecraft:stone_slab",
            "minecraft:torch", null),

    GUARD_TOWER(3, 3, 8, DistrictType.MILITARY, Profession.GUARD,
            Map.of(ResourceType.STONE, 30L),
            "minecraft:stone_bricks", "minecraft:stone_bricks", "minecraft:stone_brick_slab",
            "minecraft:torch", null),

    FARM(7, 7, 1, DistrictType.AGRICULTURAL, Profession.FARMER,
            Map.of(ResourceType.WOOD, 10L),
            "minecraft:farmland", "minecraft:oak_fence", null,
            "minecraft:water", null),

    TEMPLE(5, 5, 6, DistrictType.RELIGIOUS, Profession.PRIEST,
            Map.of(ResourceType.STONE, 25L, ResourceType.GOLD, 2L),
            "minecraft:stone_bricks", "minecraft:quartz_block", "minecraft:quartz_slab",
            "minecraft:torch", null),

    SCHOOL(5, 5, 4, DistrictType.EDUCATION, Profession.TEACHER,
            Map.of(ResourceType.WOOD, 15L, ResourceType.STONE, 10L),
            "minecraft:cobblestone", "minecraft:oak_planks", "minecraft:oak_slab",
            "minecraft:torch", null);

    private final int width;
    private final int depth;
    private final int height;
    private final DistrictType districtType;
    private final Profession triggerProfession;
    private final Map<ResourceType, Long> resourceCost;
    private final String foundationBlock;
    private final String wallBlock;
    private final String roofBlock;
    private final String interiorBlock;
    private final String decorationBlock;

    BuildingType(int width, int depth, int height, DistrictType districtType, Profession triggerProfession,
                 Map<ResourceType, Long> resourceCost, String foundationBlock, String wallBlock,
                 String roofBlock, String interiorBlock, String decorationBlock) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.districtType = districtType;
        this.triggerProfession = triggerProfession;
        this.resourceCost = resourceCost;
        this.foundationBlock = foundationBlock;
        this.wallBlock = wallBlock;
        this.roofBlock = roofBlock;
        this.interiorBlock = interiorBlock;
        this.decorationBlock = decorationBlock;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int getHeight() {
        return height;
    }

    public DistrictType getDistrictType() {
        return districtType;
    }

    /** Null if this building isn't triggered by any one profession's presence. */
    public Profession getTriggerProfession() {
        return triggerProfession;
    }

    public Map<ResourceType, Long> getResourceCost() {
        return resourceCost;
    }

    public String getFoundationBlock() {
        return foundationBlock;
    }

    public String getWallBlock() {
        return wallBlock;
    }

    /** Null means this building type has no roof stage (e.g. FARM). */
    public String getRoofBlock() {
        return roofBlock;
    }

    /** Null means this building type has no interior stage. */
    public String getInteriorBlock() {
        return interiorBlock;
    }

    /** Null means this building type has no decoration stage. */
    public String getDecorationBlock() {
        return decorationBlock;
    }
}
