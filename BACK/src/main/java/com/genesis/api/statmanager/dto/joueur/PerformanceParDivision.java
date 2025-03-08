package com.genesis.api.statmanager.dto.joueur;

import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceParDivision {
    private String division; // Convertir `Enum Division` en `String`
    private int buts; // Convertir `Long` en `int`
    private int passes; // Convertir `Long` en `int`
    private double minutesJouees; // Convertir `Double` correctement
    private double moyenneCote; // Gérer comme `Double`
    private int points; // Convertir `Long` en `int`

    public PerformanceParDivision(Division division, Long buts, Long passes, Double minutesJouees, Double moyenneCote, Long points) {
        this.division = division != null ? division.name() : ""; // Convertit `Enum` en `String`, gère les cas null
        this.buts = (buts != null) ? buts.intValue() : 0; // Conversion `Long` → `int`
        this.passes = (passes != null) ? passes.intValue() : 0;
        this.minutesJouees = (minutesJouees != null) ? minutesJouees : 0.0;
        this.moyenneCote = (moyenneCote != null) ? moyenneCote : 0.0;
        this.points = (points != null) ? points.intValue() : 0;
    }
}
