package MineGamer.civilizationai.command;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.building.BuildingConstructionSite;
import MineGamer.civilizationai.domain.economy.EconomyLedger;
import MineGamer.civilizationai.domain.incident.Incident;
import MineGamer.civilizationai.domain.needs.JobRatioNeedsEvaluator;
import MineGamer.civilizationai.domain.needs.NeedScore;
import MineGamer.civilizationai.domain.reputation.ReputationLedger;
import MineGamer.civilizationai.domain.resource.ResourceLedger;
import MineGamer.civilizationai.domain.resource.ResourceType;
import MineGamer.civilizationai.domain.technology.Technology;
import MineGamer.civilizationai.domain.technology.TechnologyLedger;
import MineGamer.civilizationai.save.SaveManager;
import MineGamer.civilizationai.save.export.ExportService;
import MineGamer.civilizationai.save.export.ImportService;
import MineGamer.civilizationai.util.Constants;
import MineGamer.civilizationai.util.PerformanceProfiler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Registers {@code /civilization ...}. Read-only subcommands (info, list,
 * stats, reputation, debug) are open to every player; mutating ones
 * (create, reset, export, import) require permission level 2, matching
 * vanilla's convention for commands that alter world/server state.
 * <p>
 * Every subcommand that acts on "a civilization" without one named
 * explicitly resolves to the nearest civilization to the command sender's
 * current position, in their current dimension — the natural interpretation
 * for a player standing in or near a village.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class CivilizationCommands {

    private static final ExportService EXPORT_SERVICE = new ExportService();
    private static final ImportService IMPORT_SERVICE = new ImportService();
    private static final JobRatioNeedsEvaluator NEEDS_EVALUATOR = new JobRatioNeedsEvaluator();

    private CivilizationCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("civilization")
                        .then(Commands.literal("info").executes(CivilizationCommands::info))
                        .then(Commands.literal("list").executes(CivilizationCommands::list))
                        .then(Commands.literal("stats").executes(CivilizationCommands::stats))
                        .then(Commands.literal("reputation")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> reputation(ctx, EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2))
                                .executes(CivilizationCommands::debug))
                        .then(Commands.literal("create")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> create(ctx, StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(2))
                                .executes(CivilizationCommands::reset))
                        .then(Commands.literal("export")
                                .requires(source -> source.hasPermission(2))
                                .executes(CivilizationCommands::export))
                        .then(Commands.literal("import")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("file", StringArgumentType.word())
                                        .executes(ctx -> importCivilization(ctx, StringArgumentType.getString(ctx, "file")))))
        );
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CivilizationManager manager = managerFor(player);
        Civilization civilization = requireNearest(ctx, manager, player);
        if (civilization == null) {
            return 0;
        }

        int population = civilization.getPopulation();
        ResourceLedger stock = manager.getOrCreateResourceLedger(civilization.getId());
        Technology tier = manager.getTechnologyLedger(civilization.getId())
                .map(TechnologyLedger::getCurrentTier).orElse(Technology.PRIMITIVE);
        int buildingCount = manager.getBuildingsForCivilization(civilization.getId()).size();
        int roadCount = manager.getRoadSegmentsForCivilization(civilization.getId()).size();

        StringBuilder message = new StringBuilder();
        message.append(civilization.getName()).append(" — population ").append(population)
                .append(", technology: ").append(tier).append('\n');
        message.append("Buildings: ").append(buildingCount).append(", roads: ").append(roadCount).append('\n');
        message.append("Stock: ");
        for (ResourceType type : ResourceType.values()) {
            long amount = stock.getStock(type);
            if (amount > 0) {
                message.append(type).append('=').append(amount).append(' ');
            }
        }

        String output = message.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(output), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        CivilizationManager manager = SaveManager.getManager(overworld(level));
        Collection<Civilization> civilizations = manager.getCivilizationsInDimension(level.dimension());

        if (civilizations.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No civilizations in this dimension."), false);
            return 0;
        }

        StringBuilder message = new StringBuilder("Civilizations here: ");
        for (Civilization civilization : civilizations) {
            message.append(civilization.getName()).append(" (pop ").append(civilization.getPopulation()).append(") ");
        }
        String output = message.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(output), false);
        return civilizations.size();
    }

    private static int stats(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        CivilizationManager manager = SaveManager.getManager(overworld(level));
        Collection<Civilization> all = manager.getAllCivilizations();

        int totalPopulation = all.stream().mapToInt(Civilization::getPopulation).sum();
        int totalBuildings = manager.getAllBuildings().size();
        int totalRoads = manager.getAllRoadSegments().size();
        int incidentsOnRecord = manager.getAllIncidents().size();

        String message = String.format(
                "Civilizations: %d, total population: %d, buildings: %d, roads: %d, incidents on record: %d",
                all.size(), totalPopulation, totalBuildings, totalRoads, incidentsOnRecord);
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return all.size();
    }

    private static int reputation(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        CivilizationManager manager = managerFor(sender);
        Civilization civilization = requireNearest(ctx, manager, sender);
        if (civilization == null) {
            return 0;
        }

        int reputationValue = manager.getReputationLedger(civilization.getId())
                .map(ReputationLedger::getAllReputation)
                .map(map -> map.getOrDefault(target.getUUID(), 0))
                .orElse(0);

        String message = target.getGameProfile().getName() + "'s reputation with " + civilization.getName() + ": " + reputationValue;
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return reputationValue;
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CivilizationManager manager = managerFor(player);
        Civilization civilization = requireNearest(ctx, manager, player);
        if (civilization == null) {
            return 0;
        }

        StringBuilder message = new StringBuilder();
        message.append("=== ").append(civilization.getName()).append(" debug ===\n");

        List<NeedScore> needs = NEEDS_EVALUATOR.evaluate(civilization, manager);
        message.append("Needs: ");
        for (NeedScore score : needs) {
            message.append(score.type()).append('=').append(String.format("%.2f", score.priority())).append(' ');
        }
        message.append('\n');

        Map<Profession, Long> professionCounts = new EnumMap<>(Profession.class);
        for (UUID villagerId : civilization.getVillagerIds()) {
            manager.getProfile(villagerId).ifPresent(profile ->
                    professionCounts.merge(profile.getProfession(), 1L, Long::sum));
        }
        message.append("Jobs: ").append(professionCounts).append('\n');

        Collection<Incident> incidents = manager.getActiveIncidents(civilization.getId(), player.level().getGameTime());
        message.append("Active incidents: ");
        for (Incident incident : incidents) {
            message.append(incident.type()).append(' ');
        }
        message.append('\n');

        long activeSites = manager.getAllBuildingSites().values().stream()
                .filter((BuildingConstructionSite site) -> site.getCivilizationId().equals(civilization.getId()))
                .count();
        message.append("Active construction: ").append(activeSites).append('\n');

        Optional<EconomyLedger> economy = manager.getEconomyLedger(civilization.getId());
        if (economy.isPresent()) {
            message.append("Economy: ");
            for (ResourceType type : ResourceType.values()) {
                double multiplier = economy.get().getPriceMultiplier(type);
                if (Math.abs(multiplier - 1.0) > 0.01) {
                    message.append(type).append('=').append(String.format("%.2f", multiplier)).append(' ');
                }
            }
            message.append('\n');
        }

        message.append("Performance:\n").append(PerformanceProfiler.formatReport());

        String output = message.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(output), false);
        return 1;
    }

    private static int create(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = ctx.getSource().getLevel();
        CivilizationManager manager = SaveManager.getManager(overworld(level));

        GlobalPos origin = GlobalPos.of(level.dimension(), player.blockPosition());
        Civilization civilization = manager.createCivilization(name, origin, overworld(level).getGameTime());

        String output = "Founded " + civilization.getName() + " at " + player.blockPosition().toShortString();
        ctx.getSource().sendSuccess(() -> Component.literal(output), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CivilizationManager manager = managerFor(player);
        Civilization civilization = requireNearest(ctx, manager, player);
        if (civilization == null) {
            return 0;
        }

        String name = civilization.getName();
        manager.removeCivilization(civilization.getId());
        String output = "Removed " + name + ".";
        ctx.getSource().sendSuccess(() -> Component.literal(output), true);
        return 1;
    }

    private static int export(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CivilizationManager manager = managerFor(player);
        Civilization civilization = requireNearest(ctx, manager, player);
        if (civilization == null) {
            return 0;
        }

        try {
            Path file = EXPORT_SERVICE.export(ctx.getSource().getServer(), civilization);
            String output = "Exported " + civilization.getName() + " to " + file.getFileName();
            ctx.getSource().sendSuccess(() -> Component.literal(output), true);
            return 1;
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.literal("Export failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int importCivilization(CommandContext<CommandSourceStack> ctx, String fileName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = ctx.getSource().getLevel();

        Path directory = ctx.getSource().getServer().getWorldPath(LevelResource.ROOT).resolve("civilizationai_exports");
        Path file = directory.resolve(fileName.endsWith(".dat") ? fileName : fileName + ".dat");

        GlobalPos newOrigin = GlobalPos.of(level.dimension(), player.blockPosition());
        try {
            Civilization created = IMPORT_SERVICE.importFile(ctx.getSource().getServer(), file, newOrigin, overworld(level).getGameTime());
            String output = "Imported " + created.getName() + " at " + player.blockPosition().toShortString();
            ctx.getSource().sendSuccess(() -> Component.literal(output), true);
            return 1;
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.literal("Import failed: " + e.getMessage()));
            return 0;
        }
    }

    // --- Helpers ---

    private static CivilizationManager managerFor(ServerPlayer player) {
        return SaveManager.getManager(overworld((ServerLevel) player.level()));
    }

    private static ServerLevel overworld(ServerLevel anyLevel) {
        return anyLevel.getServer().getLevel(Level.OVERWORLD);
    }

    private static Civilization requireNearest(CommandContext<CommandSourceStack> ctx, CivilizationManager manager, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        Civilization nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Civilization candidate : manager.getCivilizationsInDimension(player.level().dimension())) {
            double distSq = pos.distSqr(candidate.getOrigin().pos());
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        if (nearest == null) {
            ctx.getSource().sendFailure(Component.literal("No civilization found in this dimension."));
        }
        return nearest;
    }
}
