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



        public int calculerScoreClassement(FeuilleDeMatch feuille, double coteMoyenne, int cleanSheets) {
            Joueur joueur = feuille.getJoueur();
            boolean isGardien = joueur instanceof Gardien;

            int pointsButsArreter = isGardien ? ((Gardien) joueur).getButArreter() * 5 : 0;
            int pointsButsEncaisser = isGardien ? -((Gardien) joueur).getButEncaisser() * 3 : 0;
            int pointsButs = feuille.getButs() * 5;
            int pointsPasses = feuille.getPasses() * 3;
            int pointsMinutes = (int) (feuille.getMinutesJouees() / 90) * 1;
            int pointsCote = (int) (coteMoyenne * 10);
            int pointsCleanSheets = cleanSheets * 7;

            return pointsButs + pointsPasses + pointsMinutes + pointsCote + pointsCleanSheets + pointsButsArreter + pointsButsEncaisser;
        }

}
