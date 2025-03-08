package com.genesis.api.statmanager.service.impl;

import com.genesis.api.statmanager.dto.StatChampionnatDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.model.Championnat;
import com.genesis.api.statmanager.model.StatChampionnat;
import com.genesis.api.statmanager.projection.JoueurProjection;
import com.genesis.api.statmanager.repository.ChampionnatRepository;
import com.genesis.api.statmanager.repository.JoueurRepository;
import com.genesis.api.statmanager.repository.StatChampionnatRepository;
import com.genesis.api.statmanager.service.StatChampionnatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 📌 Implémentation du service de gestion des statistiques des joueurs dans un championnat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatChampionnatServiceImpl implements StatChampionnatService {

    private final StatChampionnatRepository statChampionnatRepository;
    private final ChampionnatRepository championnatRepository;
    private final JoueurRepository joueurRepository;

    // =========================================================================
    // 🔹 SECTION 1 : RÉCUPÉRATION DES STATISTIQUES
    // =========================================================================

    @Override
    public List<StatChampionnatDTO> findAllAsDTO() {
        return statChampionnatRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatChampionnatDTO> findByJoueurAndChampionnatAsDTO(Long joueurId, Long championnatId) {
        Championnat championnat = championnatRepository.findById(championnatId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Championnat non trouvé avec ID : " + championnatId));

        return statChampionnatRepository.findByJoueurIdAndChampionnat(joueurId, championnat)
                .map(this::mapToDTO);
    }

    @Override
    public List<StatChampionnatDTO> findByJoueurAsDTO(Long joueurId) {
        return statChampionnatRepository.findByJoueurId(joueurId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StatChampionnatDTO> findByChampionnatAsDTO(Long championnatId) {
        Championnat championnat = championnatRepository.findById(championnatId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Championnat non trouvé avec ID : " + championnatId));

        return statChampionnatRepository.findByChampionnat(championnat)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 📌 Récupère le classement des meilleurs joueurs dans un championnat selon un critère spécifique.
     */
    @Override
    public List<StatChampionnatDTO> getTopJoueurs(Long idChampionnat, String critere) {
        Championnat championnat = championnatRepository.findById(idChampionnat)
                .orElseThrow(() -> new IllegalArgumentException("❌ Championnat non trouvé avec ID : " + idChampionnat));

        return statChampionnatRepository.findByChampionnat(championnat).stream()
                .sorted((a, b) -> switch (critere) {
                    case "buts" -> Integer.compare(b.getButsChamp(), a.getButsChamp());
                    case "passes" -> Integer.compare(b.getPassesChamp(), a.getPassesChamp());
                    case "cote" -> Double.compare(b.getMoyenneCoteChamp(), a.getMoyenneCoteChamp());
                    default -> 0;
                })
                .limit(10)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 🔹 SECTION 2 : MISE À JOUR DES STATISTIQUES
    // =========================================================================

    /**
     * 🔄 Met à jour les statistiques d'un joueur après une rencontre.
     */
    @Override
    @Transactional
    public void mettreAJourStatistiques(Long idChampionnat, List<StatistiquesRencontreDTO> statsRencontres) {
        Championnat championnat = championnatRepository.findById(idChampionnat)
                .orElseThrow(() -> new IllegalArgumentException("❌ Championnat non trouvé avec ID : " + idChampionnat));

        statsRencontres.forEach(stats -> {
            Long joueurId = stats.getJoueurId(); // ✅ Récupération directe de l'ID sans instancier `Joueur`

            // ✅ Mise à jour des statistiques avec `joueurId`
            StatChampionnat statChampionnat = statChampionnatRepository.findByJoueurIdAndChampionnat(joueurId, championnat)
                    .orElseGet(() -> new StatChampionnat(null, joueurId, championnat, 0, 0, 0.0, 5.0));

            // ✅ Mise à jour des statistiques cumulées
            statChampionnat.setButsChamp(statChampionnat.getButsChamp() + stats.getButs());
            statChampionnat.setPassesChamp(statChampionnat.getPassesChamp() + stats.getPasses());
            statChampionnat.setMinutesJoueesChamp(statChampionnat.getMinutesJoueesChamp() + stats.getTotalMinutesJouees());
            statChampionnat.setMoyenneCoteChamp((statChampionnat.getMoyenneCoteChamp() + stats.getCote()) / 2);

            statChampionnatRepository.saveAndFlush(statChampionnat);
        });
    }

    // =========================================================================
    // 🔹 SECTION 3 : CONVERSIONS ET MAPPERS
    // =========================================================================

    /**
     * 📌 Convertit une entité StatChampionnat en StatChampionnatDTO.
     */
    private StatChampionnatDTO mapToDTO(StatChampionnat stat) {
        JoueurProjection joueurProjection = joueurRepository.findJoueurProjectionById(stat.getJoueurId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Joueur introuvable pour ID=" + stat.getJoueurId()));

        return new StatChampionnatDTO(
                stat.getId(),
                stat.getJoueurId(),  // ✅ On utilise `joueurId`
                joueurProjection.getNom(),  // ✅ On récupère le nom via la projection
                stat.getChampionnat().getIdChamp(),
                stat.getButsChamp(),
                stat.getPassesChamp(),
                stat.getMinutesJoueesChamp(),
                stat.getMoyenneCoteChamp()
        );
    }
}
