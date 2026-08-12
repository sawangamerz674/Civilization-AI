package MineGamer.civilizationai.domain.needs;

/**
 * One need's computed priority for a civilization at a point in time. Higher
 * {@code priority} means more urgent. A priority of 0 or less means the need
 * is currently satisfied and {@link NeedsEvaluator} implementations should
 * omit it from their result entirely rather than including a non-positive
 * entry.
 */
public record NeedScore(NeedType type, double priority) {
}
