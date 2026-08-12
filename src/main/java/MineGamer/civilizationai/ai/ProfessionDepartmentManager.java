package MineGamer.civilizationai.ai;

import MineGamer.civilizationai.domain.Civilization;
import MineGamer.civilizationai.domain.CivilizationManager;
import MineGamer.civilizationai.domain.JobAssignmentService;
import MineGamer.civilizationai.domain.Profession;
import MineGamer.civilizationai.domain.needs.NeedScore;
import net.minecraft.util.RandomSource;

/**
 * The Phase 3 default: every need maps to exactly one {@link Profession}
 * (see {@code NeedType.getProfession()}), so fulfilling it is just asking
 * {@link JobAssignmentService} to staff that profession from the pool of
 * currently unemployed villagers.
 */
public final class ProfessionDepartmentManager implements DepartmentManager {

    private final JobAssignmentService jobAssignmentService;

    public ProfessionDepartmentManager(JobAssignmentService jobAssignmentService) {
        this.jobAssignmentService = jobAssignmentService;
    }

    @Override
    public boolean fulfilNeed(NeedScore need, Civilization civilization, CivilizationManager manager,
                               long gameTime, RandomSource random) {
        Profession profession = need.type().getProfession();
        return jobAssignmentService.assignBestFit(civilization, manager, profession, random).isPresent();
    }
}
