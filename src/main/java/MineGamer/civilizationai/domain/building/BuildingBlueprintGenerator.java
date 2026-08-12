package MineGamer.civilizationai.domain.building;

import MineGamer.civilizationai.domain.construction.BlockPlacement;
import MineGamer.civilizationai.domain.construction.ConstructionStage;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a building's block plan algorithmically from its
 * {@link BuildingType} and a terrain survey — there is no schematic file
 * anywhere in this mod. Every placement is relative to {@code (0, 0, 0)},
 * which the caller anchors at {@code (siteX, flattenHeight, siteZ)}.
 * <p>
 * Stage order matches the spec exactly: survey happens before this class is
 * even called (see {@code TerrainAnalyzer}); this produces FLATTEN,
 * FOUNDATION, WALLS, then — if the building type has them — ROOF, INTERIOR,
 * DECORATION, in that order. A building type can omit ROOF/INTERIOR/
 * DECORATION entirely (see {@link BuildingType#getRoofBlock()} etc. being
 * nullable) — FARM, for instance, has no roof.
 */
public final class BuildingBlueprintGenerator {

    private BuildingBlueprintGenerator() {
    }

    public static List<BlockPlacement> generate(BuildingType type, TerrainSurvey survey) {
        List<BlockPlacement> plan = new ArrayList<>();
        int width = type.getWidth();
        int depth = type.getDepth();
        int height = type.getHeight();

        appendFlatten(plan, survey, width, depth);
        appendFoundation(plan, type, width, depth);
        appendWalls(plan, type, width, depth, height);

        if (type.getRoofBlock() != null) {
            appendRoof(plan, type, width, depth, height);
        }
        if (type.getInteriorBlock() != null) {
            appendInterior(plan, type, width, depth);
        }
        if (type.getDecorationBlock() != null) {
            appendDecoration(plan, type, width, depth);
        }

        return plan;
    }

    private static void appendFlatten(List<BlockPlacement> plan, TerrainSurvey survey, int width, int depth) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int terrainY = survey.heightAt(dx, dz);
                int target = survey.flattenHeight();
                if (terrainY > target) {
                    for (int y = target + 1; y <= terrainY; y++) {
                        plan.add(new BlockPlacement(new BlockPos(dx, y - target, dz), "minecraft:air", ConstructionStage.FLATTEN));
                    }
                } else if (terrainY < target) {
                    for (int y = terrainY + 1; y <= target; y++) {
                        plan.add(new BlockPlacement(new BlockPos(dx, y - target, dz), "minecraft:dirt", ConstructionStage.FLATTEN));
                    }
                }
            }
        }
    }

    private static void appendFoundation(List<BlockPlacement> plan, BuildingType type, int width, int depth) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                plan.add(new BlockPlacement(new BlockPos(dx, 0, dz), type.getFoundationBlock(), ConstructionStage.FOUNDATION));
            }
        }
    }

    /** Perimeter walls with a single door-width gap centered on the south face (dz = 0). */
    private static void appendWalls(List<BlockPlacement> plan, BuildingType type, int width, int depth, int height) {
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int dx = 0; dx < width; dx++) {
                for (int dz = 0; dz < depth; dz++) {
                    boolean perimeter = dx == 0 || dx == width - 1 || dz == 0 || dz == depth - 1;
                    if (!perimeter) {
                        continue;
                    }
                    boolean isDoorway = dz == 0 && dx == doorX && y <= 2;
                    if (isDoorway) {
                        plan.add(new BlockPlacement(new BlockPos(dx, y, dz), "minecraft:air", ConstructionStage.WALLS));
                    } else {
                        plan.add(new BlockPlacement(new BlockPos(dx, y, dz), type.getWallBlock(), ConstructionStage.WALLS));
                    }
                }
            }
        }
    }

    private static void appendRoof(List<BlockPlacement> plan, BuildingType type, int width, int depth, int height) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                plan.add(new BlockPlacement(new BlockPos(dx, height + 1, dz), type.getRoofBlock(), ConstructionStage.ROOF));
            }
        }
    }

    private static void appendInterior(List<BlockPlacement> plan, BuildingType type, int width, int depth) {
        int centerX = width / 2;
        int centerZ = depth / 2;
        plan.add(new BlockPlacement(new BlockPos(centerX, 1, centerZ), type.getInteriorBlock(), ConstructionStage.INTERIOR));
    }

    private static void appendDecoration(List<BlockPlacement> plan, BuildingType type, int width, int depth) {
        int doorX = width / 2;
        // Just outside the doorway (dz = -1), so it never conflicts with a wall or floor placement.
        plan.add(new BlockPlacement(new BlockPos(doorX, 1, -1), type.getDecorationBlock(), ConstructionStage.DECORATION));
    }
}
