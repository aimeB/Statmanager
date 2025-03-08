package com.genesis.api.statmanager.dto.joueur;

import com.genesis.api.statmanager.model.enumeration.Poste;
import com.genesis.api.statmanager.projection.JoueurProjection;
import lombok.*;

/**
 * 📌 DTO représentant un joueur avec ses statistiques et sa position.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoueurDTO {

    private Long id;
    private String nom;
    private String poste; // Poste est un String
    private String categoriePoste;
    private int buts = 0;
    private int passes = 0;
    private double minutesJouees = 0;
    private double moyenneCote = 5.0;
    private int point = 0;
    private String typeJoueur;

    // Constructeur avec Long joueurId, String nomInconnu et String poste
    public JoueurDTO(Long id, String nom, Poste poste) {
        this.id = id;
        this.nom = nom;
        this.poste = String.valueOf(Poste.fromString(String.valueOf(poste)));
    }




    /**
     * 📌 Constructeur spécifique utilisé pour mapper un joueur avec des détails personnalisés.
     */
    public JoueurDTO(Long joueurId, String nom, String poste, String categoriePoste, int point, String typeJoueur) {
        this.id = joueurId;
        this.nom = nom;
        this.poste = poste;
        this.categoriePoste = categoriePoste;
        this.point = point;
        this.typeJoueur = typeJoueur;
    }

    public static JoueurDTO fromProjection(JoueurProjection projection) {
        return JoueurDTO.builder()
                .id(projection.getJid())
                .nom(projection.getNom())
                .poste(projection.getPoste())
                .categoriePoste(Poste.fromString(projection.getPoste()).getCategoriePoste().name())
                .buts(projection.getTotalButs())
                .passes(projection.getTotalPasses())
                .minutesJouees(projection.getTotalMinutesJouees())
                .moyenneCote(projection.getTotalMoyenneCote())
                .typeJoueur( projection.getTypeJoueur().getSimpleName() ) // ✅ Convertir `Class<?>` en `String`
                .build();
    }
}
