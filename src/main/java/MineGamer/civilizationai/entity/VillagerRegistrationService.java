package MineGamer.civilizationai.entity;

import MineGamer.civilizationai.api.event.CivilizationCreatedEvent;
import MineGamer.civilizationai.api.event.VillagerRegisteredEvent;
import MineGamer.civilizationai.config.ModConfig;
import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.VillagerProfile;
import MineGamer.civilizationai.memory.DeathMemory;
import MineGamer.civilizationai.save.SaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.UUID;

/**
 * The bridge between real Minecraft {@link Villager} entities and this
 * mod's data model — closes the gap every prior phase deliberately left
 * open (see the "no entity integration yet" notes in
 * {@code docs/ARCHITECTURE.md}). Called from
 * {@link MineGamer.civilizationai.event.VillagerLifecycleEventHandler}.
 */
public final class VillagerRegistrationService {

    /**
     * Called when any villager entity joins a level (spawned, bred, or
     * loaded from disk). If it isn't already assigned to a civilization,
     * finds the nearest one within {@code civilizationClaimRadius} that
     * isn't already at {@code populationCapPerVillage}, or founds a brand
     * new civilization anchored at the villager's position if none
     * qualifies — unless the server is already at {@code maxVillagesPerServer},
     * in which case the villager is left unclaimed rather than exceeding it.
     * Founding a civilization here is the literal moment one "emerges" for
     * the first time in a fresh world.
     */
    public void handleVillagerJoin(ServerLevel level, Villager villager) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        CivilizationManager manager = SaveManager.getManager(overworld);

        UUID villagerId = villager.getUUID();
        boolean alreadyAssigned = manager.getProfile(villagerId).map(VillagerProfile::isAssigned).orElse(false);
        if (alreadyAssigned) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        BlockPos pos = villager.blockPosition();
        long gameTime = overworld.getGameTime();
        int claimRadius = ModConfig.COMMON.civilizationClaimRadius.get();
        int populationCap = ModConfig.COMMON.populationCapPerVillage.get();

        Civilization target = null;
        double closestDistSq = Double.MAX_VALUE;
        for (Civilization candidate : manager.getCivilizationsInDimension(dimension)) {
            if (candidate.getPopulation() >= populationCap) {
                // At its population cap — not eligible to claim this villager, even if it's the nearest.
                continue;
            }
            double distSq = pos.distSqr(candidate.getOrigin().pos());
            if (distSq <= (double) claimRadius * claimRadius && distSq < closestDistSq) {
                closestDistSq = distSq;
                target = candidate;
            }
        }

        if (target == null) {
            if (manager.getAllCivilizations().size() >= ModConfig.COMMON.maxVillagesPerServer.get()) {
                // Server-wide civilization cap reached — leave this villager unclaimed rather than
                // founding one more. It's re-evaluated the next time it joins a level (e.g. chunk reload).
                return;
            }
            String name = CivilizationNameGenerator.generate(level.getRandom());
            target = manager.createCivilization(name, GlobalPos.of(dimension, pos), gameTime);
            MinecraftForge.EVENT_BUS.post(new CivilizationCreatedEvent(target.getId(), name, target.getOrigin()));
        }

        manager.registerVillager(villagerId, target.getId(), gameTime, level.getRandom());
        MinecraftForge.EVENT_BUS.post(new VillagerRegisteredEvent(target.getId(), villagerId));
    }

    /**
     * Called when a tracked villager dies. Records the death into every
     * civilization-mate's memory (bounded — see {@code VillagerMemory}) and
     * unregisters the villager from its civilization, keeping its profile
     * and memory data (matching Phase 2's design: a dead villager's history
     * isn't deleted, just no longer a member of anything).
     */
    public void handleVillagerDeath(ServerLevel level, Villager villager, DamageSource source) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        CivilizationManager manager = SaveManager.getManager(overworld);

        UUID villagerId = villager.getUUID();
        VillagerProfile profile = manager.getProfile(villagerId).orElse(null);
        if (profile == null || profile.getCivilizationId() == null) {
            return;
        }

        Civilization civilization = manager.getCivilization(profile.getCivilizationId()).orElse(null);
        long gameTime = overworld.getGameTime();
        GlobalPos deathPos = GlobalPos.of(level.dimension(), villager.blockPosition());
        String cause = source.getMsgId();

        if (civilization != null) {
            for (UUID otherId : civilization.getVillagerIds()) {
                if (otherId.equals(villagerId)) {
                    continue;
                }
                manager.getOrCreateMemory(otherId).rememberDeath(new DeathMemory(villagerId, cause, deathPos, gameTime));
            }
        }

        manager.unregisterVillager(villagerId);
        manager.markDirty();
    }
}
