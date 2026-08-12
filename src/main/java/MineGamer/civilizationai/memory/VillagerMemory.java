package MineGamer.civilizationai.memory;

import MineGamer.civilizationai.util.BoundedList;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Everything one villager remembers. One instance per villager, keyed by
 * villager UUID in {@link MineGamer.civilizationai.domain.CivilizationManager}.
 * <p>
 * Every list-shaped category is a {@link BoundedList} so a villager that
 * lives for the entire lifetime of a world cannot accumulate unbounded
 * memory. Relationship and reputation maps are keyed by UUID and are
 * naturally bounded by the number of distinct villagers/players a villager
 * has actually interacted with, so they are plain maps.
 * <p>
 * Capacity numbers are deliberately generous but finite; they are not yet
 * exposed via {@link MineGamer.civilizationai.config.ModConfig} — if playtesting
 * in a later phase shows they need to be tunable, that's a config addition,
 * not a schema change, since the cap only affects future evictions.
 */
public final class VillagerMemory {

    private static final int TRADE_CAPACITY = 64;
    private static final int DANGER_CAPACITY = 32;
    private static final int RAID_CAPACITY = 16;
    private static final int DEATH_CAPACITY = 16;
    private static final int WEATHER_CAPACITY = 32;
    private static final int LOCATIONS_PER_CATEGORY_CAPACITY = 16;
    private static final int MAX_TRAVEL_ROUTES = 24;

    private final UUID villagerId;

    /** Positive = friend, negative = enemy, magnitude = strength of the relationship. */
    private final Map<UUID, Integer> relationships = new HashMap<>();

    /** Reputation this villager holds toward each player, independent of civilization-wide reputation. */
    private final Map<UUID, Integer> playerReputation = new HashMap<>();

    private final Map<MemoryLocationType, BoundedList<LocationMemory>> locations = new EnumMap<>(MemoryLocationType.class);
    private final BoundedList<TravelRoute> travelRoutes = new BoundedList<>(MAX_TRAVEL_ROUTES);
    private final BoundedList<TradeMemory> trades = new BoundedList<>(TRADE_CAPACITY);
    private final BoundedList<DangerMemory> dangerEvents = new BoundedList<>(DANGER_CAPACITY);
    private final BoundedList<RaidMemory> pastRaids = new BoundedList<>(RAID_CAPACITY);
    private final BoundedList<DeathMemory> pastDeaths = new BoundedList<>(DEATH_CAPACITY);
    private final BoundedList<WeatherMemory> weatherHistory = new BoundedList<>(WEATHER_CAPACITY);

    public VillagerMemory(UUID villagerId) {
        this.villagerId = villagerId;
        for (MemoryLocationType type : MemoryLocationType.values()) {
            locations.put(type, new BoundedList<>(LOCATIONS_PER_CATEGORY_CAPACITY));
        }
    }

    public UUID getVillagerId() {
        return villagerId;
    }

    // --- Relationships & reputation ---

    public void adjustRelationship(UUID otherVillagerId, int delta) {
        relationships.merge(otherVillagerId, delta, Integer::sum);
    }

    public int getRelationship(UUID otherVillagerId) {
        return relationships.getOrDefault(otherVillagerId, 0);
    }

    public Map<UUID, Integer> getRelationships() {
        return Map.copyOf(relationships);
    }

    public void adjustPlayerReputation(UUID playerId, int delta) {
        playerReputation.merge(playerId, delta, Integer::sum);
    }

    public int getPlayerReputation(UUID playerId) {
        return playerReputation.getOrDefault(playerId, 0);
    }

    public Map<UUID, Integer> getPlayerReputations() {
        return Map.copyOf(playerReputation);
    }

    // --- Locations ---

    public void rememberLocation(MemoryLocationType type, LocationMemory memory) {
        locations.get(type).add(memory);
    }

    public BoundedList<LocationMemory> getLocations(MemoryLocationType type) {
        return locations.get(type);
    }

    // --- Travel routes ---

    public void rememberRoute(TravelRoute route) {
        travelRoutes.add(route);
    }

    public BoundedList<TravelRoute> getTravelRoutes() {
        return travelRoutes;
    }

    // --- Trades ---

    public void rememberTrade(TradeMemory trade) {
        trades.add(trade);
    }

    public BoundedList<TradeMemory> getTrades() {
        return trades;
    }

    // --- Danger ---

    public void rememberDanger(DangerMemory danger) {
        dangerEvents.add(danger);
    }

    public BoundedList<DangerMemory> getDangerEvents() {
        return dangerEvents;
    }

    // --- Raids ---

    public void rememberRaid(RaidMemory raid) {
        pastRaids.add(raid);
    }

    public BoundedList<RaidMemory> getPastRaids() {
        return pastRaids;
    }

    // --- Deaths ---

    public void rememberDeath(DeathMemory death) {
        pastDeaths.add(death);
    }

    public BoundedList<DeathMemory> getPastDeaths() {
        return pastDeaths;
    }

    // --- Weather ---

    public void rememberWeather(WeatherMemory weather) {
        weatherHistory.add(weather);
    }

    public BoundedList<WeatherMemory> getWeatherHistory() {
        return weatherHistory;
    }
}
