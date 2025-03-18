package com.genesis.api.statmanager.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * 📌 Représente les statistiques d'un joueur dans un championnat spécifique.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stat_championnat")
public class StatChampionnat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "joueur_id", nullable = false)
    private Long joueurId; // ✅ Utilisation de `joueurId` au lieu de `Joueur`

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "championnat_id", nullable = false)
    private Championnat championnat;

    private int butsChamp = 0; // ✅ Nombre de buts marqués dans le championnat
    private int passesChamp = 0; // ✅ Nombre de passes décisives
    private double minutesJoueesChamp = 0.0; // ✅ Temps de jeu cumulé
    private double moyenneCoteChamp = 5.0; // ✅ Moyenne de la cote du joueur
    private int butArreterChamp;
    private int butEncaisserChamp;


    @Column(name = "clean_sheet", nullable = false)
    private int cleanSheet = 0; // ✅ Nombre de clean sheets pour le joueur dans le championnat

    /**
     * 📌 Nouveau constructeur avec `joueurId` au lieu d'un objet `Joueur`
     */
    public StatChampionnat(Long id, Long joueurId, Championnat championnat, int butsChamp, int passesChamp, double minutesJoueesChamp, double moyenneCoteChamp) {
        this.id = id;
        this.joueurId = joueurId;
        this.championnat = championnat;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.moyenneCoteChamp = moyenneCoteChamp;
    }


    public StatChampionnat(Long id, Long joueurId, Championnat championnat, int butsChamp, int passesChamp,
                           double minutesJoueesChamp, double moyenneCoteChamp, int butArreter, int butEncaisser, int cleanSheet) {
        this.id = id;
        this.joueurId = joueurId;
        this.championnat = championnat;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.moyenneCoteChamp = moyenneCoteChamp;
        this.butArreterChamp = butArreter;
        this.butEncaisserChamp = butEncaisser;
        this.cleanSheet = cleanSheet; // ✅ Maintenant c'est un int
    }




}
