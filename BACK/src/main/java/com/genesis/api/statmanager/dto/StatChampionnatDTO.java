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
    private int cleanSheet = 0;








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


    public StatChampionnatDTO(StatChampionnat stat, JoueurProjection joueurProjection) {
        if (stat != null) {
            this.id = stat.getId();
            this.joueurId = stat.getJoueurId();
            this.joueurNom = (joueurProjection != null) ? joueurProjection.getNom() : "Inconnu";
            this.championnatId = stat.getChampionnat().getIdChamp();
            this.butsChamp = stat.getButsChamp();
            this.passesChamp = stat.getPassesChamp();
            this.minutesJoueesChamp = stat.getMinutesJoueesChamp();
            this.moyenneCoteChamp = stat.getMoyenneCoteChamp();
            this.cleanSheet = stat.getCleanSheet();

            if (joueurProjection != null) {
                Poste posteEnum = Poste.valueOf(joueurProjection.getPoste());
                this.poste = posteEnum.name();
                this.categoriePoste = posteEnum.getCategoriePoste().name();
            } else {
                this.poste = "INCONNU";
                this.categoriePoste = "INCONNU";
            }
        }
    }


    public StatChampionnatDTO(StatChampionnat stat) {
        if (stat != null) {
            this.id = stat.getId();
            this.joueurId = stat.getJoueurId();
            this.joueurNom = "Inconnu"; // ⚠️ Sera mis à jour plus tard avec une projection si nécessaire
            this.championnatId = stat.getChampionnat().getIdChamp();
            this.butsChamp = stat.getButsChamp();
            this.passesChamp = stat.getPassesChamp();
            this.minutesJoueesChamp = stat.getMinutesJoueesChamp();
            this.moyenneCoteChamp = stat.getMoyenneCoteChamp();
            this.cleanSheet = stat.getCleanSheet();
            this.poste = "INCONNU";
            this.categoriePoste = "INCONNU";
        }
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
