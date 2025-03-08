package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.enumeration.Poste;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_joueur", discriminatorType = DiscriminatorType.STRING)
@Table(name = "joueur")
public abstract class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jid", nullable = false)
    private Long jid;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_joueur", insertable = false, updatable = false)
    private String typeJoueur;


    @Enumerated(EnumType.STRING)
    @Column(name = "poste", nullable = false)
    private Poste poste;

    private int totalButs;
    private int totalPasses;
    private double totalMinutesJouees;
    private double totalMoyenneCote;

    @Column(name = "point", nullable = false)
    private int point = 0; // ✅ Initialisé à 0

    public Joueur(Long jid, String nom, Poste poste) {
        this.jid = jid;
        this.nom = nom;
        this.poste = poste;
    }

    public Joueur(Long jid, String nom, Poste poste, int totalButs, int totalPasses, double totalMinutesJouees, double moyenneCote, int point) {
        this.jid = jid;
        this.nom = nom;
        this.poste = poste;
        this.totalButs = totalButs;
        this.totalPasses = totalPasses;
        this.totalMinutesJouees = totalMinutesJouees;
        this.totalMoyenneCote = moyenneCote;
        this.point = point; // ✅ Stocke bien les points cumulés
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Joueur joueur = (Joueur) o;
        return Objects.equals(jid, joueur.jid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jid);
    }


    public String getCategoriePoste() {
        return (this.poste != null) ? this.poste.getCategoriePoste().name() : "INCONNU";
    }

}
