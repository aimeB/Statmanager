package com.genesis.api.statmanager.dto.joueur;

import com.genesis.api.statmanager.model.Joueur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoueurLightDTO {
    private Long id;
    private String nom;
    private String poste;

    // Constructeur adapté à partir des valeurs de la projection
    public JoueurLightDTO(Long id, String nom, String poste) {
        this.id = id;
        this.nom = nom;
        this.poste = poste;
    }
}
