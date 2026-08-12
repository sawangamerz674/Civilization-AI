package MineGamer.civilizationai.entity;

import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Generates a civilization name when {@link VillagerRegistrationService}
 * auto-founds one — a prefix + suffix combination rather than a fixed
 * list, so repeats are unlikely without needing a huge word list.
 */
final class CivilizationNameGenerator {

    private static final List<String> PREFIXES = List.of(
            "Oak", "River", "Stone", "Hollow", "Green", "Iron", "Silver", "Amber",
            "Wolf", "Fox", "Elm", "Birch", "Winter", "Summer", "North", "South",
            "High", "Low", "Old", "New"
    );

    private static final List<String> SUFFIXES = List.of(
            "stead", "mere", "haven", "ford", "vale", "shire", "wick", "burg",
            "hollow", "field", "brook", "crest", "watch", "hold", "reach"
    );

    private CivilizationNameGenerator() {
    }

    static String generate(RandomSource random) {
        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        return prefix + suffix;
    }
}
