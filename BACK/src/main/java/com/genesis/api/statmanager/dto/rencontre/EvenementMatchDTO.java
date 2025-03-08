package com.genesis.api.statmanager.dto.rencontre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 📌 DTO représentant un événement de match (but ou remplacement).
 */
@Getter
@Setter
@AllArgsConstructor
public class EvenementMatchDTO {
    private Long idRencontre; // ✅ ID de la rencontre
    private Long idJoueur;    // ✅ ID du joueur concerné
    private String nomJoueur; // ✅ Nom du joueur concerné
    private int minute;       // ✅ Minute de l'événement
    private String typeEvenement; // ✅ Type d'événement (BUT ou REMPLACEMENT)
    private Long idPasseur;   // ✅ ID du passeur (peut être null)
    private String nomPasseur; // ✅ Nom du passeur (peut être null ou "Sans passeur")
}
