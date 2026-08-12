package MineGamer.civilizationai.domain.building;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Groups a civilization's buildings into {@link District}s: same
 * {@link DistrictType}, within {@code clusterRadius} blocks of another
 * member of the same cluster (single-link / greedy clustering — simple by
 * design, since a village's building count stays small enough that
 * clustering quality doesn't need anything more sophisticated).
 * <p>
 * This is how "districts naturally emerge" is implemented: nothing ever
 * explicitly creates or names a district. Two houses built near each other
 * are a residential district purely because {@link #cluster} says so when
 * asked, not because anything decided to found one.
 */
public final class DistrictClusterer {

    private DistrictClusterer() {
    }

    public static List<District> cluster(Collection<Building> buildings, double clusterRadius) {
        Map<DistrictType, List<Building>> byType = new EnumMap<>(DistrictType.class);
        for (Building building : buildings) {
            byType.computeIfAbsent(building.type().getDistrictType(), key -> new ArrayList<>()).add(building);
        }

        List<District> districts = new ArrayList<>();
        for (Map.Entry<DistrictType, List<Building>> entry : byType.entrySet()) {
            districts.addAll(clusterOneType(entry.getKey(), entry.getValue(), clusterRadius));
        }
        return districts;
    }

    private static List<District> clusterOneType(DistrictType type, List<Building> buildings, double clusterRadius) {
        double radiusSq = clusterRadius * clusterRadius;
        List<List<Building>> clusters = new ArrayList<>();

        outer:
        for (Building building : buildings) {
            for (List<Building> cluster : clusters) {
                Building representative = cluster.get(0);
                boolean sameDimension = representative.origin().dimension().equals(building.origin().dimension());
                if (sameDimension && representative.origin().pos().distSqr(building.origin().pos()) <= radiusSq) {
                    cluster.add(building);
                    continue outer;
                }
            }
            List<Building> newCluster = new ArrayList<>();
            newCluster.add(building);
            clusters.add(newCluster);
        }

        List<District> districts = new ArrayList<>();
        for (List<Building> cluster : clusters) {
            List<java.util.UUID> ids = cluster.stream().map(Building::id).toList();
            districts.add(new District(type, computeCenter(cluster), ids));
        }
        return districts;
    }

    private static GlobalPos computeCenter(List<Building> cluster) {
        Building first = cluster.get(0);
        long sumX = 0;
        long sumY = 0;
        long sumZ = 0;
        for (Building building : cluster) {
            BlockPos pos = building.origin().pos();
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }
        int count = cluster.size();
        BlockPos average = new BlockPos((int) (sumX / count), (int) (sumY / count), (int) (sumZ / count));
        return GlobalPos.of(first.origin().dimension(), average);
    }
}
