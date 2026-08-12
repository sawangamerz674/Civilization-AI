package MineGamer.civilizationai.domain.building;

/**
 * The result of surveying a candidate building footprint's terrain: the
 * measured surface height of every column, and the single "flatten to"
 * height {@link MineGamer.civilizationai.world.TerrainAnalyzer} computed from
 * them.
 * <p>
 * Lives in {@code domain.building} rather than {@code world} deliberately —
 * {@link BuildingBlueprintGenerator} (pure, world-independent) needs this
 * shape, and {@code world} already depends on {@code domain}, not the other
 * way around. {@code TerrainAnalyzer} is simply the one place that knows how
 * to build one of these from a real {@code ServerLevel}.
 */
public final class TerrainSurvey {

    private final int width;
    private final int depth;
    private final int[][] heights;
    private final int flattenHeight;

    public TerrainSurvey(int width, int depth, int[][] heights, int flattenHeight) {
        this.width = width;
        this.depth = depth;
        this.heights = heights;
        this.flattenHeight = flattenHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int heightAt(int dx, int dz) {
        return heights[dx][dz];
    }

    public int flattenHeight() {
        return flattenHeight;
    }
}
