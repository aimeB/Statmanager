package com.genesis.api.statmanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.genesis.api.statmanager.model.enumeration.TimePlay;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.genesis.api.statmanager.model.enumeration.TimePlay.MINUTES90;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Entity
@Table(name = "feuille_de_match")
public class FeuilleDeMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feuille_id", nullable = false, unique = true)
    private Long feuilleId;

    // Relation avec Rencontre (BIGINT)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rencontre_id", referencedColumnName = "rid", nullable = false)
    @JsonBackReference
    @NotNull(message = "La rencontre associée est obligatoire.")
    private Rencontre rencontre;
    // Relation avec Joueur (BIGINT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", insertable = false, updatable = false)  // ✅ Empêche Hibernate de gérer deux fois la colonne `joueur_id`
    @JsonIgnore
    private Joueur joueur;

    // ID du joueur (BIGINT)
    @Column(name = "joueur_id", nullable = false)
    @NotNull(message = "L id du joueur est obligatoire.")
    private Long jid;

    // Statistiques générales
    @Column(name = "buts", nullable = false)
    @Min(value = 0, message = "Le nombre de buts doit être positif.")
    private Integer buts = 0;

    @Column(name = "passes", nullable = false)
    @Min(value = 0, message = "Le nombre de passes doit être positif.")
    private Integer passes = 0;

    @Column(name = "cote", nullable = false)
    @Min(value = 0, message = "La moyenne de la cote doit être positive.")
    private Double cote = 0.0;

    @Column(name = "minutes_jouees", nullable = false)
    @Min(value = 0, message = "Les minutes jouées doivent être positives.")
    private Double minutesJouees = 0.0;

    // Statistiques de gardien (BIT)
    @Column(name = "but_arreter", nullable = false)
    private int butArreter;

    @Column(name = "but_encaisser", nullable = false)
    private int butEncaisser;




    // Statut du joueur (a joué et titulaire) (BIT)
    @Column(name = "a_joue", nullable = false)
    private boolean ajoue; // Déclare comme `false` par défaut

    @Column(name = "titulaire", nullable = false)
    private boolean titulaire; // Nouveau champ : `true` = titulaire, `false` = remplaçant








    @ManyToMany
    @JoinTable(
            name = "passeurs_feuille_match",
            joinColumns = @JoinColumn(name = "feuille_id"),
            inverseJoinColumns = @JoinColumn(name = "passeur_id")
    )
    private List<Joueur> passeurs = new ArrayList<>(); // ✅ Liste des passeurs


    // ✅ Méthode pour charger `Joueur` uniquement quand nécessaire
    public Joueur getJoueurSafe(EntityManager entityManager) {
        if (joueur == null) {
            return entityManager.find(Joueur.class, this.jid);
        }
        return joueur;
    }

    public void ajouterPasseur(Joueur passeur, FeuilleDeMatch feuillePasseur, int nombreDeButs) {
        // ✅ Vérification stricte : un joueur ne peut pas être compté deux fois pour le même but
        long occurencesPasseur = this.passeurs.stream().filter(p -> p.getJid().equals(passeur.getJid())).count();

        if (occurencesPasseur < nombreDeButs) {
            this.passeurs.add(passeur);
            log.info("✅ Passeur ajouté : {} (JID={}) à la feuille ID={}", passeur.getNom(), passeur.getJid(), this.feuilleId);

            // ✅ Mise à jour des passes sur la feuille du passeur, et non du buteur
            feuillePasseur.setPasses(feuillePasseur.getPasses() + 1);
        } else {
            log.warn("⚠️ Tentative d'ajouter plusieurs fois le passeur {} pour le même but ! Interdit.", passeur.getNom());
        }
    }
    public void setMinutesJoueesEnum(TimePlay minutes) {
        if (minutes != null) {
            this.minutesJouees = minutes.getPercentage(); // ✅ Stocker en `double`
            log.info("✅ [DEBUG] Stockage en base : TimePlay={} → Double={}", minutes, this.minutesJouees);
        } else {
            this.minutesJouees = 0.0;
        }
    }

    public TimePlay getMinutesJoueesEnum() {
        return TimePlay.fromPercentage(this.minutesJouees); // ✅ Retourne `TimePlay` au lieu d'un `double`
    }



    public static double convertTimePlayToDouble(TimePlay timePlay) {
        return timePlay.getPercentage() * 90.0; // ✅ Convertit `TimePlay` en minutes réelles
    }

    public static TimePlay convertDoubleToTimePlay(double minutes) {
        return TimePlay.fromPercentage(minutes / 90.0); // ✅ Convertit `Double` en `TimePlay`
    }






    // ✅ Ajout pour forcer le chargement correct de `Joueur`
    @PostLoad
    private void ensureCorrectJoueurSubclass() {
    }



    // Méthodes equals et hashCode basées sur feuilleId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeuilleDeMatch that = (FeuilleDeMatch) o;
        return Objects.equals(feuilleId, that.feuilleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feuilleId);
    }
}
