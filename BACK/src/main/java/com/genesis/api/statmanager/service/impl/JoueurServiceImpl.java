package com.genesis.api.statmanager.service.impl;

import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurLightDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.joueur.PerformanceParDivision;
import com.genesis.api.statmanager.dto.statistique.StatCompositeDTO;
import com.genesis.api.statmanager.dto.statistique.StatCritereDTO;
import com.genesis.api.statmanager.dto.statistique.StatTopJoueurDTO;
import com.genesis.api.statmanager.projection.JoueurProjection;
import com.genesis.api.statmanager.repository.JoueurRepository;
import com.genesis.api.statmanager.service.JoueurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
@Slf4j
@RequiredArgsConstructor
public class JoueurServiceImpl implements JoueurService {

    private final JoueurRepository joueurRepository;

    // =========================================================================
    // 🔹 SECTION 1 : RÉCUPÉRATION DES DONNÉES (LECTURE)
    // =========================================================================

    /**
     * 📌 Récupère tous les joueurs sous forme de DTO via projection.
     */
    @Override
    public List<JoueurDTO> findAllAsDTO() {
        return joueurRepository.findAllJoueursAsProjection()
                .stream()
                .map(JoueurDTO::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * 📌 Récupère un joueur spécifique sous forme de DTO via projection.
     */
    @Override
    public JoueurDTO findByIdAsDTO(Long id) {
        return joueurRepository.findJoueurProjectionById(id)
                .map(JoueurDTO::fromProjection)
                .orElseThrow(() -> new IllegalArgumentException("❌ Joueur introuvable pour ID=" + id));
    }

    /**
     * 📌 Récupère une liste allégée des joueurs (ID, Nom, Poste) triés par poste.
     */
    @Override
    public List<JoueurLightDTO> getJoueursLight() {
        return joueurRepository.findAllOrderByPosteAsProjection()
                .stream()
                .map(joueur -> new JoueurLightDTO(joueur.getJid(), joueur.getNom(), joueur.getPoste())) // Utilisation des valeurs projetées
                .collect(Collectors.toList());
    }


    // =========================================================================
    // 🔹 SECTION 2 : STATISTIQUES & CLASSEMENT
    // =========================================================================



    public List<StatistiquesDTO> getAllJoueursStats() {
        return joueurRepository.findAllJoueursAsProjection().stream()
                .map(projection -> {
                    // Récupération des rencontres du joueur avec vérification
                    Page<StatistiquesRencontreDTO> statsPage =
                            joueurRepository.findDerniersMatchsByJoueur(projection.getJid(), PageRequest.of(0, 100));

                    List<StatistiquesRencontreDTO> statsRencontres = statsPage.hasContent() ? statsPage.getContent() : new ArrayList<>();

                    // Génération du DTO avec agrégation des stats
                    return StatistiquesDTO.fromRencontres(statsRencontres);
                })
                .collect(Collectors.toList());

    }











    /**
     * 📌 Récupère un joueur avec ses statistiques globales en utilisant une projection.
     */
    @Override
    public JoueurStatGlobalDTO findByIdWithGlobalStats(Long id) {
        return joueurRepository.findJoueurProjectionById(id)
                .map(projection -> {
                    // Récupération des performances par division
                    List<PerformanceParDivision> performancesList =
                            joueurRepository.findPerformancesParDivision(id);

                    // Transformation de la liste en un Map<String, PerformanceParDivision>
                    Map<String, PerformanceParDivision> performancesParDivision =
                            performancesList.stream()
                                    .collect(Collectors.toMap(PerformanceParDivision::getDivision, p -> p));

                    // PageRequest avec Spring Data Pageable
                    Pageable pageable = PageRequest.of(0, 10); // Page 0, 10 éléments par page

                    // Récupération de la page des derniers matchs
                    Page<StatistiquesRencontreDTO> derniersMatchsPage =
                            joueurRepository.findDerniersMatchsByJoueur(id, pageable);

                    // Extraction du contenu de la page en tant que liste
                    List<StatistiquesRencontreDTO> derniersMatchsList = derniersMatchsPage.getContent();

                    // Instanciation du DTO
                    JoueurStatGlobalDTO dto = new JoueurStatGlobalDTO(
                            projection.getJid(),
                            projection.getNom(),
                            projection.getTypeJoueur().getSimpleName() , // ✅ Convertir `Class<?>` en `String`
                            projection.getPoste(),
                            projection.getTotalButs(),
                            projection.getTotalPasses(),
                            projection.getTotalMinutesJouees(),
                            projection.getTotalMoyenneCote(),
                            performancesParDivision,
                            derniersMatchsList
                    );

                    // Calcul des scores globaux
                    dto.calculerScoresGlobal();
                    return dto;
                })
                .orElseThrow(() -> new IllegalArgumentException("❌ Joueur introuvable pour ID=" + id));
    }




    @Override
    public JoueurStatGlobalDTO findByNomWithGlobalStats(String nom) {
        return joueurRepository.findJoueurProjectionByNom(nom)
                .map(this::convertirProjectionEnDTO)
                .orElseThrow(() -> new IllegalArgumentException("❌ Joueur introuvable pour Nom=" + nom));
    }



    private JoueurStatGlobalDTO convertirProjectionEnDTO(JoueurProjection projection) {
        // 📌 Récupération des performances par division
        List<PerformanceParDivision> performancesList = joueurRepository.findPerformancesParDivision(projection.getJid());

        Map<String, PerformanceParDivision> performancesParDivision = performancesList.stream()
                .collect(Collectors.toMap(PerformanceParDivision::getDivision, p -> p));

        Pageable pageable = PageRequest.of(0, 10);
        Page<StatistiquesRencontreDTO> derniersMatchsPage = joueurRepository.findDerniersMatchsByJoueur(projection.getJid(), pageable);

        List<StatistiquesRencontreDTO> derniersMatchsList = derniersMatchsPage.getContent();

        JoueurStatGlobalDTO dto = new JoueurStatGlobalDTO(
                projection.getJid(),
                projection.getNom(),
                projection.getTypeJoueur().getSimpleName() ,
                projection.getPoste(),
                projection.getTotalButs(),
                projection.getTotalPasses(),
                projection.getTotalMinutesJouees(),
                projection.getTotalMoyenneCote(),
                performancesParDivision,
                derniersMatchsList
        );

        dto.calculerScoresGlobal();
        return dto;
    }


    /**
     * 📌 Retourne les joueurs triés selon un critère spécifique.
     */
    public StatCritereDTO getJoueursParCritere(String critere) {
        List<JoueurStatGlobalDTO> joueurs = joueurRepository.findAllJoueursAsProjection().stream()
                .map(JoueurStatGlobalDTO::fromProjection)
                .sorted((j1, j2) -> JoueurStatGlobalDTO.comparerParCritere(j1, j2, critere))
                .limit(10)
                .collect(Collectors.toList());
        return new StatCritereDTO(joueurs);
    }

    /**
     * 📌 Retourne les meilleurs joueurs des 5 dernières rencontres.
     */
    public StatTopJoueurDTO getMeilleursJoueurs() {
        List<JoueurStatGlobalDTO> meilleursJoueurs = joueurRepository.findAllJoueursAsProjection().stream()
                .map(JoueurStatGlobalDTO::fromProjection)
                .sorted((j1, j2) -> Double.compare(j2.getMoyenneCoteGlobal(), j1.getMoyenneCoteGlobal()))
                .limit(5)
                .collect(Collectors.toList());
        return new StatTopJoueurDTO(meilleursJoueurs);
    }

    /**
     * 📌 Retourne les joueurs triés selon un score composite.
     */
    public StatCompositeDTO getJoueursParScoreComposite() {
        List<JoueurStatGlobalDTO> joueurs = joueurRepository.findAllJoueursAsProjection().stream()
                .map(JoueurStatGlobalDTO::fromProjection)
                .sorted((j1, j2) -> Double.compare(j2.getScoreContributionGlobal(), j1.getScoreContributionGlobal()))
                .limit(10)
                .collect(Collectors.toList());
        return new StatCompositeDTO(joueurs);
    }



    /**
     * 📌 Récupère tous les joueurs avec leurs statistiques globales.
     */
    @Override
    public List<JoueurStatGlobalDTO> findAllWithGlobalStats() {
        return joueurRepository.findAllJoueursAsProjection().stream()
                .map(projection -> {
                    // Récupérer les performances par division et les 10 derniers matchs
                    Map<String, PerformanceParDivision> performancesParDivision =
                            joueurRepository.findPerformancesParDivision(projection.getJid()).stream()
                                    .collect(Collectors.toMap(PerformanceParDivision::getDivision, p -> p));

                    // PageRequest avec Spring Data Pageable
                    Pageable pageable = PageRequest.of(0, 10);

                    // Récupération de la page des derniers matchs
                    Page<StatistiquesRencontreDTO> derniersMatchsPage =
                            joueurRepository.findDerniersMatchsByJoueur(projection.getJid(), pageable);

                    // Extraction du contenu de la page en tant que liste
                    List<StatistiquesRencontreDTO> derniersMatchs = derniersMatchsPage.getContent();

                    // Instanciation du DTO avec tous les paramètres
                    JoueurStatGlobalDTO dto = new JoueurStatGlobalDTO(
                            projection.getJid(),
                            projection.getNom(),
                            projection.getTypeJoueur().getSimpleName() ,
                            projection.getPoste(),
                            projection.getTotalButs(),
                            projection.getTotalPasses(),
                            projection.getTotalMinutesJouees(),
                            projection.getTotalMoyenneCote(),
                            performancesParDivision,
                            derniersMatchs
                    );

                    // Calcul des scores
                    dto.calculerScoresGlobal();
                    return dto;
                })
                .collect(Collectors.toList());
    }



    /**
     * 📌 Classe et retourne les meilleurs joueurs selon un critère.
     */
    @Override
    public List<JoueurStatGlobalDTO> getTopJoueursGlobaux(String critere, String poste) {
        return findAllWithGlobalStats().stream()
                .filter(joueur -> poste == null || joueur.getPoste().equalsIgnoreCase(poste))
                .sorted((j1, j2) -> JoueurStatGlobalDTO.comparerParCritere(j1, j2, critere))
                .limit(10)
                .collect(Collectors.toList());
    }
}
