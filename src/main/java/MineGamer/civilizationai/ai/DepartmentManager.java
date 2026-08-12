package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.needs.NeedScore;
import net.minecraft.util.RandomSource;

/**
 * Distributes work in response to a single prioritized need. This is the
 * "Department Managers" layer from the AI hierarchy: the governor decides
 * *which* need matters most, a department manager decides *how* to act on
 * it. Splitting this out (rather than having {@link VillageGovernor} act
 * directly) means a later phase can give one department (say, Military)
 * fundamentally different logic — training existing guards, building walls
 * — without touching how other departments work or how the governor picks
 * priorities.
 */
public interface DepartmentManager {

    /**
     * @return true if the need was acted on (a villager was assigned),
     *         false if nothing could be done this cycle (e.g. no eligible
     *         villagers available).
     */
    boolean fulfilNeed(NeedScore need, Civilization civilization, CivilizationManager manager,
                        long gameTime, RandomSource random);
}
