package com.genesis.api.statmanager.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.Statut;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "championnat")
public class Championnat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_champ", nullable = false, unique = true)
    private Long idChamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut; // Statut du championnat (ENCOURS, PROMOTION, RELÉGATION)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Division division; // Division du championnat (DIV1, DIV2, etc.)

    @Column(name = "points_actuels", nullable = false)
    @Min(0) // Les points actuels ne peuvent pas être négatifs
    private int pointsActuels;

    @Column(name = "points_promotion", nullable = false)
    @Min(0) // Les points pour promotion ne peuvent pas être négatifs
    private int pointsPromotion;

    @Column(name = "points_relegation", nullable = false)
    @Min(0) // Les points pour éviter la relégation ne peuvent pas être négatifs
    private int pointsRelegation;


    @OneToMany(mappedBy = "championnat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Rencontre> rencontres = new ArrayList<>();








    /**
     * Ajoute une rencontre à ce championnat.
     * Met automatiquement à jour la relation bidirectionnelle.
     *
     * @param rencontre La rencontre à ajouter.
     */
    public void addRencontre(Rencontre rencontre) {
        rencontres.add(rencontre);
        rencontre.setChampionnat(this);
    }

    /**
     * Supprime une rencontre de ce championnat.
     * Met automatiquement à jour la relation bidirectionnelle.
     *
     * @param rencontre La rencontre à supprimer.
     */
    public void removeRencontre(Rencontre rencontre) {
        rencontres.remove(rencontre);
        rencontre.setChampionnat(null);
    }

    /**
     * Vérifie si le championnat est proche de la promotion.
     *
     * @return true si les points actuels sont proches des points nécessaires pour promotion.
     */
    public boolean isCloseToPromotion() {
        return pointsActuels >= pointsPromotion - 5 && pointsActuels < pointsPromotion;
    }

    /**
     * Vérifie si le championnat est proche de la relégation.
     *
     * @return true si les points actuels sont proches des points nécessaires pour éviter la relégation.
     */
    public boolean isCloseToRelegation() {
        return pointsActuels >= pointsRelegation - 5 && pointsActuels < pointsRelegation;
    }

    /**
     * Recalcule le statut du championnat en fonction des points actuels.
     */
    public void updateStatut() {
        if (pointsActuels >= pointsPromotion) {
            statut = Statut.PROMOTION;
        } else if (pointsActuels < pointsRelegation) {
            statut = Statut.RELEGATION;
        } else {
            statut = Statut.ENCOURS;
        }
    }

    public List<Joueur> getJoueurs() {
        return rencontres.stream()
                .flatMap(rencontre -> rencontre.getJoueurs().stream())
                .distinct()
                .collect(Collectors.toList());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Championnat that = (Championnat) o;
        return Objects.equals(idChamp, that.idChamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idChamp);
    }
}
