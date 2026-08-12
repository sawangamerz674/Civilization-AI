package MineGamer.civilizationai.domain.construction;

/**
 * The stage a queued {@link BlockPlacement} belongs to. Present purely for
 * bookkeeping/inspection (e.g. a future HUD or command could report "this
 * house is on WALLS") — {@link MineGamer.civilizationai.world.ConstructionExecutor}
 * doesn't branch on it, it just places whatever's next in the queue in the
 * order {@link MineGamer.civilizationai.domain.building.BuildingBlueprintGenerator}
 * or {@link MineGamer.civilizationai.world.RoadPathGenerator} produced.
 */
public enum ConstructionStage {
    SURVEY,
    FLATTEN,
    FOUNDATION,
    WALLS,
    ROOF,
    INTERIOR,
    DECORATION,
    PAVING,
    COMPLETE
}
