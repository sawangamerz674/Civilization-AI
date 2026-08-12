package MineGamer.civilizationai.save.export;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.reputation.ReputationLedger;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import MineGamer.civilizationai.domain.technology.TechnologyLedger;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.save.serializers.EconomyLedgerSerializer;
import MineGamer.civilizationai.save.serializers.ReputationLedgerSerializer;
import MineGamer.civilizationai.save.serializers.ResourceLedgerSerializer;
import MineGamer.civilizationai.save.serializers.TechnologyLedgerSerializer;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Reads an {@link ExportService} file back and creates a <em>new</em>
 * civilization from it at a caller-supplied origin.
 * <p>
 * Deliberately does not restore buildings or roads — the blocks a building
 * or road placed are still sitting in whatever world/location they were
 * originally built in, wherever that was. Silently "restoring" building
 * records without the actual blocks would leave the manager claiming
 * structures exist that a player can't see or use, which is worse than not
 * importing them at all. What this restores is the civilization's
 * <em>economic and technological state</em>: name, resource stock, price
 * multipliers, player reputation, and technology tier — genuinely useful
 * for a backup/restore or "transplant this village's progress elsewhere"
 * use case, honestly short of a full world-state restore.
 */
public final class ImportService {

    public Civilization importFile(MinecraftServer server, Path file, GlobalPos newOrigin, long gameTime) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file.toFile());

        String name = root.getCompound("Civilization").getString("Name");

        CivilizationManager manager = SaveManager.getManager(server.getLevel(Level.OVERWORLD));
        Civilization created = manager.createCivilization(name, newOrigin, gameTime);

        if (root.contains("ResourceLedger")) {
            ResourceLedger imported = ResourceLedgerSerializer.read(root.getCompound("ResourceLedger"));
            ResourceLedger target = manager.getOrCreateResourceLedger(created.getId());
            for (Map.Entry<ResourceType, Long> entry : imported.getAllStock().entrySet()) {
                target.deposit(entry.getKey(), entry.getValue(), Long.MAX_VALUE);
            }
        }

        if (root.contains("EconomyLedger")) {
            EconomyLedger imported = EconomyLedgerSerializer.read(root.getCompound("EconomyLedger"));
            EconomyLedger target = manager.getOrCreateEconomyLedger(created.getId());
            imported.getAllMultipliers().forEach(target::setPriceMultiplier);
        }

        if (root.contains("ReputationLedger")) {
            ReputationLedger imported = ReputationLedgerSerializer.read(root.getCompound("ReputationLedger"));
            ReputationLedger target = manager.getOrCreateReputationLedger(created.getId());
            imported.getAllReputation().forEach(target::adjustReputation);
        }

        if (root.contains("TechnologyLedger")) {
            TechnologyLedger imported = TechnologyLedgerSerializer.read(root.getCompound("TechnologyLedger"));
            manager.getOrCreateTechnologyLedger(created.getId()).setCurrentTier(imported.getCurrentTier());
        }

        manager.markDirty();
        return created;
    }
}
