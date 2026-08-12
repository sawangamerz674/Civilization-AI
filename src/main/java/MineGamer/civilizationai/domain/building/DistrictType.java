package MineGamer.civilizationai.domain.building;

/**
 * A category of village district. Not every value has a {@link BuildingType}
 * that produces it yet — MARKET and ENTERTAINMENT are reserved for a later
 * phase (trading buildings and leisure structures don't exist yet) but are
 * listed here now so {@link DistrictClusterer} and anything that switches on
 * this enum doesn't need to change when they're added.
 */
public enum DistrictType {
    RESIDENTIAL,
    MARKET,
    INDUSTRIAL,
    AGRICULTURAL,
    MILITARY,
    STORAGE,
    RELIGIOUS,
    EDUCATION,
    ENTERTAINMENT
}
