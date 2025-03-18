package com.genesis.api.statmanager.service;


import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.model.FeuilleDeMatch;
import com.genesis.api.statmanager.model.Gardien;
import com.genesis.api.statmanager.model.Joueur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiqueManager {





        public StatistiquesDTO calculerHommeDuMatch(List<StatistiquesDTO> statsJoueurs) {
            return statsJoueurs.stream()
                    .max(Comparator.comparingDouble(joueur ->
                            joueur.getCote() * 10 + joueur.getButs() * 7 + joueur.getPasses() * 4 + joueur.getTotalMinutesJouees() * 1.5))
                    .orElse(null);
        }



}
