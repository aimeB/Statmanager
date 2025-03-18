package com.genesis.api.statmanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.StatutRencontre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "rencontre")
public class Rencontre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rid", nullable = false, unique = true)
    private Long rid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_champ", nullable = false)
    @JsonBackReference
    private Championnat championnat;


    @Transient
    private String nomEquipe = "HERSTAL FC";

    @Transient // ✅ Ce champ ne sera pas stocké en base
    private int butEquipe;

    @Column(name = "nom_adversaire", nullable = false)
    @NotBlank(message = "Le nom de l'adversaire est obligatoire.")
    private String nomAdversaire = "";

    @Column(name = "but_adversaire")
    @NotNull(message = "Les buts de l'adversaire est obligatoire.")
    private int butAdversaire=0;

    @Enumerated(EnumType.STRING)
    @Column(name = "division_adversaire", nullable = false)
    private Division divisionAdversaire;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homme_du_match_id", nullable = true)
    private Joueur hommeDuMatch;

    public String getNomHommeDuMatch() {
        return hommeDuMatch != null ? hommeDuMatch.getNom() : "Aucun";
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_rencontre", nullable = false)
    private StatutRencontre statutRencontre = StatutRencontre.EN_ATTENTE;

    @OneToMany(mappedBy = "rencontre", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore // ✅ Hibernate ne chargera pas cette relation par défaut
    @BatchSize(size = 10)  // ✅ Évite `MultipleBagFetchException`
    private List<FeuilleDeMatch> feuilleDeMatchs = new ArrayList<>();




    public void setHommeDuMatchId(Long joueurId) {
        if (joueurId != null) {
            this.hommeDuMatch = (Joueur) new JoueurProxy(joueurId); // ✅ Utilisation d’un proxy factice
        } else {
            this.hommeDuMatch = null;
        }
    }


    public int getScoreEquipeLocale() {
        return this.feuilleDeMatchs.stream()
                .mapToInt(FeuilleDeMatch::getButs)
                .sum();
    }





    public void addFeuilleDeMatch(FeuilleDeMatch feuilleDeMatch) {
        feuilleDeMatchs.add(feuilleDeMatch);
        feuilleDeMatch.setRencontre(this);
    }


    public void removeFeuilleDeMatch(FeuilleDeMatch feuilleDeMatch) {
        feuilleDeMatchs.remove(feuilleDeMatch);
        feuilleDeMatch.setRencontre(null);
    }



    public List<Joueur> getJoueurs() {
        return feuilleDeMatchs.stream()
                .map(FeuilleDeMatch::getJoueur)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rencontre rencontre = (Rencontre) o;
        return Objects.equals(rid, rencontre.rid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rid);
    }
}

