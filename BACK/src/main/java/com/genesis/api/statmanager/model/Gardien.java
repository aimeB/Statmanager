package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.enumeration.PointJoueur;
import com.genesis.api.statmanager.model.enumeration.Poste;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Gardien")
@NoArgsConstructor
public class Gardien extends Joueur {

    public Gardien(Long id, String nom, Poste poste) {
        super(id, nom, poste);
    }

    @Min(value = 0, message = "Les buts encaissés doivent être positifs.")
    @Column(name = "but_encaisser", nullable = false)
    private int butEncaisser;

    @Min(value = 0, message = "Les arrêts doivent être positifs.")
    @Column(name = "but_arreter", nullable = false)
    private int butArreter;

    // ✅ Constructeur avec arguments
    public Gardien(Long jid, String nom, Poste poste, int totalButs, int totalPasses,
                   double totalMinutesJouees, double moyenneCote, int butEncaisser, int butArreter, int point) {
        super(jid, nom, poste, totalButs, totalPasses, totalMinutesJouees, moyenneCote, point);
        this.butEncaisser = butEncaisser;
        this.butArreter = butArreter;
    }


    // 🔹 Ajout des getters et setters pour `butEncaisser`
    public int getButEncaisser() {
        return butEncaisser;
    }

    public void setButEncaisser(int butEncaisser) {
        this.butEncaisser = butEncaisser;
    }

    // 🔹 Ajout des getters et setters pour `butArreter`
    public int getButArreter() {
        return butArreter;
    }

    public void setButArreter(int butArreter) {
        this.butArreter = butArreter;
    }
}

