package MineGamer.civilizationai.save.serializers;

import MineGamer.civilizationai.memory.DangerMemory;
import MineGamer.civilizationai.memory.DeathMemory;
import MineGamer.civilizationai.memory.LocationMemory;
import MineGamer.civilizationai.memory.MemoryLocationType;
import MineGamer.civilizationai.memory.RaidMemory;
import MineGamer.civilizationai.memory.TradeMemory;
import MineGamer.civilizationai.memory.TravelRoute;
import MineGamer.civilizationai.memory.VillagerMemory;
import MineGamer.civilizationai.memory.WeatherMemory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads/writes a single {@link VillagerMemory} to/from NBT. This is the
 * largest serializer in the mod because {@code VillagerMemory} itself
 * aggregates many small categories — each gets one private read/write pair
 * below, mirroring the structure of the domain class itself so the two stay
 * easy to keep in sync.
 */
public final class VillagerMemorySerializer {

    private static final String KEY_VILLAGER_ID = "VillagerId";
    private static final String KEY_RELATIONSHIPS = "Relationships";
    private static final String KEY_PLAYER_REPUTATION = "PlayerReputation";
    private static final String KEY_LOCATIONS = "Locations";
    private static final String KEY_TRAVEL_ROUTES = "TravelRoutes";
    private static final String KEY_TRADES = "Trades";
    private static final String KEY_DANGER = "DangerEvents";
    private static final String KEY_RAIDS = "PastRaids";
    private static final String KEY_DEATHS = "PastDeaths";
    private static final String KEY_WEATHER = "WeatherHistory";

    private static final String KEY_UUID = "Uuid";
    private static final String KEY_VALUE = "Value";
    private static final String KEY_CATEGORY = "Category";
    private static final String KEY_POS = "Pos";
    private static final String KEY_GAME_TIME = "GameTime";
    private static final String KEY_START = "Start";
    private static final String KEY_END = "End";
    private static final String KEY_WAYPOINTS = "Waypoints";
    private static final String KEY_TIMES_TRAVELED = "TimesTraveled";
    private static final String KEY_LAST_USED = "LastUsed";
    private static final String KEY_PARTNER_ID = "PartnerId";
    private static final String KEY_ITEM_ID = "ItemId";
    private static final String KEY_QUANTITY = "Quantity";
    private static final String KEY_DANGER_TYPE = "DangerType";
    private static final String KEY_ENEMY_COUNT = "EnemyCount";
    private static final String KEY_SURVIVED = "Survived";
    private static final String KEY_DECEASED_ID = "DeceasedId";
    private static final String KEY_CAUSE = "Cause";
    private static final String KEY_RAINING = "Raining";
    private static final String KEY_THUNDERING = "Thundering";

    private VillagerMemorySerializer() {
    }

    public static CompoundTag write(VillagerMemory memory) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_VILLAGER_ID, memory.getVillagerId());

        tag.put(KEY_RELATIONSHIPS, writeUuidIntMap(memory.getRelationships()));
        tag.put(KEY_PLAYER_REPUTATION, writeUuidIntMap(memory.getPlayerReputations()));

        ListTag locationsList = NbtIoUtil.newList();
        for (MemoryLocationType type : MemoryLocationType.values()) {
            for (LocationMemory location : memory.getLocations(type)) {
                CompoundTag entry = new CompoundTag();
                entry.putString(KEY_CATEGORY, type.name());
                entry.put(KEY_POS, NbtIoUtil.writeGlobalPos(location.pos()));
                entry.putLong(KEY_GAME_TIME, location.lastSeenGameTime());
                locationsList.add(entry);
            }
        }
        tag.put(KEY_LOCATIONS, locationsList);

        ListTag routesList = NbtIoUtil.newList();
        for (TravelRoute route : memory.getTravelRoutes()) {
            routesList.add(writeRoute(route));
        }
        tag.put(KEY_TRAVEL_ROUTES, routesList);

        ListTag tradesList = NbtIoUtil.newList();
        for (TradeMemory trade : memory.getTrades()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(KEY_PARTNER_ID, trade.partnerId());
            entry.putString(KEY_ITEM_ID, trade.itemId());
            entry.putInt(KEY_QUANTITY, trade.quantity());
            entry.putLong(KEY_GAME_TIME, trade.gameTime());
            tradesList.add(entry);
        }
        tag.put(KEY_TRADES, tradesList);

        ListTag dangerList = NbtIoUtil.newList();
        for (DangerMemory danger : memory.getDangerEvents()) {
            CompoundTag entry = new CompoundTag();
            entry.put(KEY_POS, NbtIoUtil.writeGlobalPos(danger.pos()));
            entry.putString(KEY_DANGER_TYPE, danger.dangerType());
            entry.putLong(KEY_GAME_TIME, danger.gameTime());
            dangerList.add(entry);
        }
        tag.put(KEY_DANGER, dangerList);

        ListTag raidsList = NbtIoUtil.newList();
        for (RaidMemory raid : memory.getPastRaids()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(KEY_GAME_TIME, raid.gameTime());
            entry.putInt(KEY_ENEMY_COUNT, raid.enemyCount());
            entry.putBoolean(KEY_SURVIVED, raid.survived());
            raidsList.add(entry);
        }
        tag.put(KEY_RAIDS, raidsList);

        ListTag deathsList = NbtIoUtil.newList();
        for (DeathMemory death : memory.getPastDeaths()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(KEY_DECEASED_ID, death.deceasedVillagerId());
            entry.putString(KEY_CAUSE, death.cause());
            entry.put(KEY_POS, NbtIoUtil.writeGlobalPos(death.pos()));
            entry.putLong(KEY_GAME_TIME, death.gameTime());
            deathsList.add(entry);
        }
        tag.put(KEY_DEATHS, deathsList);

        ListTag weatherList = NbtIoUtil.newList();
        for (WeatherMemory weather : memory.getWeatherHistory()) {
            CompoundTag entry = new CompoundTag();
            entry.putBoolean(KEY_RAINING, weather.raining());
            entry.putBoolean(KEY_THUNDERING, weather.thundering());
            entry.putLong(KEY_GAME_TIME, weather.gameTime());
            weatherList.add(entry);
        }
        tag.put(KEY_WEATHER, weatherList);

        return tag;
    }

    public static VillagerMemory read(CompoundTag tag) {
        UUID villagerId = tag.getUUID(KEY_VILLAGER_ID);
        VillagerMemory memory = new VillagerMemory(villagerId);

        for (Map.Entry<UUID, Integer> e : readUuidIntMap(tag.getList(KEY_RELATIONSHIPS, Tag.TAG_COMPOUND)).entrySet()) {
            memory.adjustRelationship(e.getKey(), e.getValue());
        }
        for (Map.Entry<UUID, Integer> e : readUuidIntMap(tag.getList(KEY_PLAYER_REPUTATION, Tag.TAG_COMPOUND)).entrySet()) {
            memory.adjustPlayerReputation(e.getKey(), e.getValue());
        }

        ListTag locationsList = tag.getList(KEY_LOCATIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < locationsList.size(); i++) {
            CompoundTag entry = locationsList.getCompound(i);
            MemoryLocationType type = MemoryLocationType.valueOf(entry.getString(KEY_CATEGORY));
            GlobalPos pos = NbtIoUtil.readGlobalPos(entry.getCompound(KEY_POS));
            long gameTime = entry.getLong(KEY_GAME_TIME);
            memory.rememberLocation(type, new LocationMemory(pos, gameTime));
        }

        ListTag routesList = tag.getList(KEY_TRAVEL_ROUTES, Tag.TAG_COMPOUND);
        for (int i = 0; i < routesList.size(); i++) {
            memory.rememberRoute(readRoute(routesList.getCompound(i)));
        }

        ListTag tradesList = tag.getList(KEY_TRADES, Tag.TAG_COMPOUND);
        for (int i = 0; i < tradesList.size(); i++) {
            CompoundTag entry = tradesList.getCompound(i);
            memory.rememberTrade(new TradeMemory(
                    entry.getUUID(KEY_PARTNER_ID),
                    entry.getString(KEY_ITEM_ID),
                    entry.getInt(KEY_QUANTITY),
                    entry.getLong(KEY_GAME_TIME)
            ));
        }

        ListTag dangerList = tag.getList(KEY_DANGER, Tag.TAG_COMPOUND);
        for (int i = 0; i < dangerList.size(); i++) {
            CompoundTag entry = dangerList.getCompound(i);
            memory.rememberDanger(new DangerMemory(
                    NbtIoUtil.readGlobalPos(entry.getCompound(KEY_POS)),
                    entry.getString(KEY_DANGER_TYPE),
                    entry.getLong(KEY_GAME_TIME)
            ));
        }

        ListTag raidsList = tag.getList(KEY_RAIDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < raidsList.size(); i++) {
            CompoundTag entry = raidsList.getCompound(i);
            memory.rememberRaid(new RaidMemory(
                    entry.getLong(KEY_GAME_TIME),
                    entry.getInt(KEY_ENEMY_COUNT),
                    entry.getBoolean(KEY_SURVIVED)
            ));
        }

        ListTag deathsList = tag.getList(KEY_DEATHS, Tag.TAG_COMPOUND);
        for (int i = 0; i < deathsList.size(); i++) {
            CompoundTag entry = deathsList.getCompound(i);
            memory.rememberDeath(new DeathMemory(
                    entry.getUUID(KEY_DECEASED_ID),
                    entry.getString(KEY_CAUSE),
                    NbtIoUtil.readGlobalPos(entry.getCompound(KEY_POS)),
                    entry.getLong(KEY_GAME_TIME)
            ));
        }

        ListTag weatherList = tag.getList(KEY_WEATHER, Tag.TAG_COMPOUND);
        for (int i = 0; i < weatherList.size(); i++) {
            CompoundTag entry = weatherList.getCompound(i);
            memory.rememberWeather(new WeatherMemory(
                    entry.getBoolean(KEY_RAINING),
                    entry.getBoolean(KEY_THUNDERING),
                    entry.getLong(KEY_GAME_TIME)
            ));
        }

        return memory;
    }

    private static CompoundTag writeRoute(TravelRoute route) {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_START, NbtIoUtil.writeGlobalPos(route.getStart()));
        tag.put(KEY_END, NbtIoUtil.writeGlobalPos(route.getEnd()));

        ListTag waypoints = NbtIoUtil.newList();
        for (BlockPos pos : route.getWaypoints()) {
            waypoints.add(NbtIoUtil.writeBlockPos(pos));
        }
        tag.put(KEY_WAYPOINTS, waypoints);

        tag.putInt(KEY_TIMES_TRAVELED, route.getTimesTraveled());
        tag.putLong(KEY_LAST_USED, route.getLastUsedGameTime());
        return tag;
    }

    private static TravelRoute readRoute(CompoundTag tag) {
        GlobalPos start = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_START));
        GlobalPos end = NbtIoUtil.readGlobalPos(tag.getCompound(KEY_END));

        List<BlockPos> waypoints = new ArrayList<>();
        ListTag waypointsList = tag.getList(KEY_WAYPOINTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < waypointsList.size(); i++) {
            waypoints.add(NbtIoUtil.readBlockPos(waypointsList.getCompound(i)));
        }

        int timesTraveled = tag.getInt(KEY_TIMES_TRAVELED);
        long lastUsed = tag.getLong(KEY_LAST_USED);
        return TravelRoute.reconstruct(start, end, waypoints, timesTraveled, lastUsed);
    }

    private static ListTag writeUuidIntMap(Map<UUID, Integer> map) {
        ListTag list = NbtIoUtil.newList();
        for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(KEY_UUID, entry.getKey());
            entryTag.putInt(KEY_VALUE, entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    private static Map<UUID, Integer> readUuidIntMap(ListTag list) {
        Map<UUID, Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            map.put(entry.getUUID(KEY_UUID), entry.getInt(KEY_VALUE));
        }
        return map;
    }
}
