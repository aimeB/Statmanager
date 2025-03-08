package com.genesis.api.statmanager.model.enumeration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TimePlay {

    MINUTES0(0.0),
    MINUTES10(0.11),
    MINUTES20(0.22),
    MINUTES30(0.33),
    MINUTES40(0.44),
    MINUTES45(0.50),
    MINUTES50(0.56),
    MINUTES60(0.67),
    MINUTES70(0.78),
    MINUTES80(0.89),
    MINUTES90(1.0);

    private final double percentage;

    // ✅ Map pour retrouver une valeur exacte
    private static final Map<Double, TimePlay> lookup = Arrays.stream(TimePlay.values())
            .collect(Collectors.toMap(TimePlay::getPercentage, e -> e));

    TimePlay(double percentage) {
        this.percentage = percentage;
    }

    @JsonValue
    public double getPercentage() {
        return percentage;
    }

    // ✅ Trouver `TimePlay` correspondant à un pourcentage exact

    @JsonCreator
    public static TimePlay fromPercentage(double percentage) {
        for (TimePlay value : values()) {
            if (Math.abs(value.getPercentage() - percentage) < 0.1) { // 🔥 Augmente la tolérance
                return value;
            }
        }
        return MINUTES0;
    }




    // ✅ Trouver `TimePlay` correspondant aux minutes
    public static TimePlay fromMinutes(int minutes) {
        double percentage = minutes / 100.0; // 🔥 Conversion directe (100 min = 1.0)
        return findClosest(percentage);
    }

    // ✅ Trouve la meilleure correspondance possible pour éviter les erreurs d'arrondi
    private static TimePlay findClosest(double value) {


        TimePlay closest = Arrays.stream(TimePlay.values())
                .min((a, b) -> Double.compare(
                        Math.abs(a.percentage - value),
                        Math.abs(b.percentage - value)))
                .orElse(MINUTES90);

        System.out.println("🔍 `findClosest` pour " + value + " → Trouvé : " + closest);
        return closest;
    }


    @Override
    public String toString() {
        return switch (this) {
            case MINUTES0 -> "0 minute";
            case MINUTES10 -> "10 minutes";
            case MINUTES20 -> "20 minutes";
            case MINUTES30 -> "30 minutes";
            case MINUTES40 -> "40 minutes";
            case MINUTES45 -> "45 minutes (mi-temps)";
            case MINUTES50 -> "50 minutes";
            case MINUTES60 -> "60 minutes";
            case MINUTES70 -> "70 minutes";
            case MINUTES80 -> "80 minutes";
            case MINUTES90 -> "90 minutes (fin du match)";
        };
    }
}
