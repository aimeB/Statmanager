package com.genesis.api.statmanager.dto;

import com.genesis.api.statmanager.model.Joueur;
import com.genesis.api.statmanager.model.StatChampionnat;
import com.genesis.api.statmanager.model.enumeration.Poste;
import com.genesis.api.statmanager.projection.JoueurProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatChampionnatDTO {

    private Long id;
    private Long joueurId;
    private String joueurNom;
    private String poste; // ✅ Poste du joueur
    private String categoriePoste; // ✅ Catégorie du poste (Défenseur, Milieu, etc.)
    private Long championnatId;

    private int butsChamp = 0; // ✅ Nombre de buts marqués dans le championnat
    private int passesChamp = 0; // ✅ Nombre de passes décisives
    private double minutesJoueesChamp = 0.0; // ✅ Temps de jeu cumulé
    private double moyenneCoteChamp = 5.0; // ✅ Moyenne de la cote du joueur
    // Calcul des points
    private int pointsChamp;
    /**
     * ✅ Constructeur à partir de l'entité `StatChampionnat` sans instancier `Joueur`.
     */
    public StatChampionnatDTO(StatChampionnat stat, JoueurProjection joueurProjection) {
        if (stat != null) {
            this.id = stat.getId();
            this.joueurId = stat.getJoueurId(); // ✅ Utilisation directe de `joueurId`
            this.joueurNom = joueurProjection.getNom(); // ✅ Récupération du nom via projection

            // ✅ Convertir String en Enum Poste
            Poste posteEnum = (joueurProjection.getPoste() != null) ? Poste.valueOf(joueurProjection.getPoste()) : null;

            // ✅ Stocker en `String` pour affichage
            this.poste = (posteEnum != null) ? posteEnum.name() : "INCONNU";

            // ✅ Obtenir la catégorie du poste (si conversion OK)
            this.categoriePoste = (posteEnum != null) ? posteEnum.getCategoriePoste().name() : "INCONNU";

            this.championnatId = stat.getChampionnat().getIdChamp();
            this.butsChamp = stat.getButsChamp();
            this.passesChamp = stat.getPassesChamp();
            this.minutesJoueesChamp = stat.getMinutesJoueesChamp();
            this.moyenneCoteChamp = stat.getMoyenneCoteChamp();
        }
    }





    public StatChampionnatDTO(Long id, Long joueurId, String joueurNom, Long championnatId,
                              int butsChamp, int passesChamp, double minutesJoueesChamp, double moyenneCoteChamp) {
        this.id = id;
        this.joueurId = joueurId;
        this.joueurNom = joueurNom;
        this.championnatId = championnatId;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.moyenneCoteChamp = moyenneCoteChamp;

        // ✅ Par défaut, définir le poste et la catégorie comme inconnus
        this.poste = "INCONNU";
        this.categoriePoste = "INCONNU";
    }


    public StatChampionnatDTO(Long id, Long joueurId, String joueurNom, Poste poste, Long championnatId,
                              int butsChamp, int passesChamp, double minutesJoueesChamp, double moyenneCoteChamp) {
        this.id = id;
        this.joueurId = joueurId;
        this.joueurNom = joueurNom;
        this.poste = poste != null ? poste.name() : "INCONNU";
        this.categoriePoste = poste != null ? poste.getCategoriePoste().name() : "INCONNU";
        this.championnatId = championnatId;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.moyenneCoteChamp = moyenneCoteChamp;
    }






    /**
     * ✅ Convertit en entité Joueur avec le poste approprié.
     */
    public Joueur getJoueur() {
        Poste posteEnum = Poste.fromString(this.poste); // ✅ Conversion correcte du poste
        return new Joueur() {
            {
                setJid(joueurId);
                setNom(joueurNom);
                setPoste(posteEnum); // ✅ Affectation correcte du poste
            }
        };
    }
}
