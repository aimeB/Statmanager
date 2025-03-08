package com.genesis.api.statmanager.dto.rencontre;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.api.statmanager.model.enumeration.TimePlay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📌 DTO gérant les remplacements en temps réel dans une rencontre.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemplacementDTO {
    private Long idRencontre;            // ✅ ID de la rencontre concernée
    private Long idRemplacantEntrant;    // ✅ ID du joueur qui entre
    private Long idTitulaireSortant;     // ✅ ID du joueur qui sort
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.##")
    private TimePlay minuteEntree;

}
