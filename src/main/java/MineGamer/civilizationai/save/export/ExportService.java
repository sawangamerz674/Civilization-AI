package MineGamer.civilizationai.save.export;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.building.Building;
import MineGamer.civilizationai.domain.road.RoadSegment;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.save.serializers.BuildingSerializer;
import MineGamer.civilizationai.save.serializers.CivilizationSerializer;
import MineGamer.civilizationai.save.serializers.EconomyLedgerSerializer;
import MineGamer.civilizationai.save.serializers.ReputationLedgerSerializer;
import MineGamer.civilizationai.save.serializers.ResourceLedgerSerializer;
import MineGamer.civilizationai.save.serializers.RoadSegmentSerializer;
import MineGamer.civilizationai.save.serializers.TechnologyLedgerSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes one civilization's state to an {@code .dat} file under
 * {@code <world>/civilizationai_exports/}, reusing every existing
 * per-object serializer rather than inventing a parallel export format.
 * <p>
 * Exports the civilization's identity, resource/economy/reputation/
 * technology ledgers, and its buildings/roads' record data (type,
 * position, completion time) — but re-importing physical structures isn't
 * supported (see {@link ImportService}'s Javadoc), so the building/road
 * lists here exist for inspection (a human or another tool reading the
 * file) rather than round-trip import.
 */
public final class ExportService {

    private static final int EXPORT_FORMAT_VERSION = 1;

    public Path export(MinecraftServer server, Civilization civilization) throws IOException {
        CivilizationManager manager = SaveManager.getManager(server.getLevel(Level.OVERWORLD));

        CompoundTag root = new CompoundTag();
        root.putInt("ExportFormatVersion", EXPORT_FORMAT_VERSION);
        root.put("Civilization", CivilizationSerializer.write(civilization));

        manager.getResourceLedger(civilization.getId())
                .ifPresent(ledger -> root.put("ResourceLedger", ResourceLedgerSerializer.write(ledger)));
        manager.getEconomyLedger(civilization.getId())
                .ifPresent(ledger -> root.put("EconomyLedger", EconomyLedgerSerializer.write(ledger)));
        manager.getReputationLedger(civilization.getId())
                .ifPresent(ledger -> root.put("ReputationLedger", ReputationLedgerSerializer.write(ledger)));
        manager.getTechnologyLedger(civilization.getId())
                .ifPresent(ledger -> root.put("TechnologyLedger", TechnologyLedgerSerializer.write(ledger)));

        ListTag buildings = new ListTag();
        for (Building building : manager.getBuildingsForCivilization(civilization.getId())) {
            buildings.add(BuildingSerializer.write(building));
        }
        root.put("Buildings", buildings);

        ListTag roads = new ListTag();
        for (RoadSegment segment : manager.getRoadSegmentsForCivilization(civilization.getId())) {
            roads.add(RoadSegmentSerializer.write(segment));
        }
        root.put("RoadSegments", roads);

        Path directory = server.getWorldPath(LevelResource.ROOT).resolve("civilizationai_exports");
        Files.createDirectories(directory);

        String safeName = civilization.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String shortId = civilization.getId().toString().substring(0, 8);
        Path file = directory.resolve(safeName + "-" + shortId + ".dat");

        NbtIo.writeCompressed(root, file.toFile());
        return file;
    }
}
