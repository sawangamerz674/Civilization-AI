package MineGamer.civilizationai.memory;

/**
 * Category a remembered location falls under. A villager's memory keeps a
 * separate bounded list per category (see {@link VillagerMemory}) rather
 * than one flat list, so lookups like "where did I last see food" don't
 * need to filter a mixed list.
 */
public enum MemoryLocationType {
    HOME,
    WORKPLACE,
    FOOD,
    FAVORITE_PLACE,
    CROP,
    ANIMAL,
    MINE,
    BUILDING
}
