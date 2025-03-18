package com.genesis.api.statmanager.service.impl;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.dto.championnat.*;
import com.genesis.api.statmanager.dto.global.StatistiquesChampionnatDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.rencontre.ClotureRencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.RencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.RencontreDetailDTO;
import com.genesis.api.statmanager.model.*;
import com.genesis.api.statmanager.model.enumeration.*;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import com.genesis.api.statmanager.projection.JoueurLightProjection;
import com.genesis.api.statmanager.projection.JoueurProjection;
import com.genesis.api.statmanager.repository.*;
import com.genesis.api.statmanager.service.ChampionnatService;
import com.genesis.api.statmanager.service.StatistiqueManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 📌 Service pour la gestion des championnats et leurs statistiques.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionnatServiceImpl implements ChampionnatService {


    private final JoueurRepository joueurRepository;
    private final RencontreRepository rencontreRepository;
    private final ChampionnatRepository championnatRepository;
    private final StatChampionnatRepository statChampionnatRepository;
    private final FeuilleDeMatchRepository feuilleDeMatchRepository;
    private final StatistiqueManager statistiqueManager;

    @PersistenceContext
    private EntityManager entityManager;


    private static final int MAX_RENCONTRES = 10;
    // =========================================================================
    // 🔹 SECTION 1 : GESTION DES CHAMPIONNATS
    // =========================================================================

    /**
     * 📌 Crée un championnat avec 23 joueurs et initialise leurs statistiques.
     */
    @Override
    @Transactional
    public ChampionnatDTO creerChampionnat(Division division, List<Long> joueursIds) {
        log.info("📥 Création d'un championnat en division {} avec {} joueurs", division, joueursIds.size());

        // ✅ 1️⃣ Récupération des joueurs sous forme de projection
        List<JoueurProjection> joueursProjections = joueurRepository.findAllJoueurProjectionsByIds(joueursIds);

        // ✅ Vérification que le championnat contient exactement 23 joueurs
        if (joueursProjections.size() != 23) {
            throw new IllegalArgumentException("❌ Un championnat doit contenir exactement 23 joueurs.");
        }

        // ✅ 2️⃣ Conversion des projections en `JoueurDTO`
        List<JoueurDTO> joueurs = joueursProjections.stream()
                .map(JoueurDTO::fromProjection)
                .toList();

        // ✅ 3️⃣ Création du championnat
        Championnat championnat = new Championnat();
        championnat.setDivision(division);
        championnat.setStatut(Statut.ENCOURS);
        configurerReglesDivision(championnat);

        // ✅ Sauvegarde immédiate du championnat pour lier les statistiques des joueurs
        championnat = championnatRepository.saveAndFlush(championnat);
        final Championnat championnatFinal = championnat; // ✅ Rend le championnat immuable pour éviter les erreurs de transaction

        // ✅ 4️⃣ Initialisation des statistiques des joueurs avec `jid` (et non `joueurId`)
        for (int i = 0; i < joueurs.size(); i++) {
            StatChampionnat stat = new StatChampionnat(
                    null,
                    joueursProjections.get(i).getJid(), // ✅ Utilisation de `jid`
                    championnatFinal,
                    0, 0, 0.0, 5.0
            );
            statChampionnatRepository.save(stat);
            log.info("✅ Stat créée avec succès pour JoueurID={}", joueursProjections.get(i).getJid());
        }

        log.info("✅ Championnat ID={} créé avec succès.", championnat.getIdChamp());
        return mapToDTO(championnat);
    }


    @Override
    @Transactional
    public void cloturerChampionnat(Long idChampionnat) {
        log.info("🏆 Clôture du championnat ID={}...", idChampionnat);

        Championnat championnat = championnatRepository.findById(idChampionnat)
                .orElseThrow(() -> new IllegalArgumentException("❌ Championnat non trouvé"));

        // 1️⃣ Vérifier que toutes les rencontres sont jouées
        if (championnat.getRencontres().stream().noneMatch(r -> r.getStatutRencontre() == StatutRencontre.TERMINE)) {
            throw new IllegalStateException("❌ Toutes les rencontres doivent être terminées avant de clôturer le championnat.");
        }

        // 2️⃣ Récupérer toutes les feuilles de match du championnat
        List<FeuilleDeMatch> feuillesDeMatch = feuilleDeMatchRepository.findByChampionnatId(idChampionnat);

        // 3️⃣ Récupérer tous les postes des joueurs en une seule requête via la projection
        List<JoueurProjection> joueursProjection = joueurRepository.findAllJoueurProjectionsByIds(
                new ArrayList<>(feuillesDeMatch.stream()
                        .map(FeuilleDeMatch::getJid)
                        .collect(Collectors.toSet())) // ✅ Convertit Set → List
        );


        // 4️⃣ Mapper les joueurs (ID → Poste) en évitant d'instancier `Joueur`
        Map<Long, Poste> postesParJoueur = joueursProjection.stream()
                .collect(Collectors.toMap(JoueurProjection::getJid, jp -> Poste.valueOf(jp.getPoste())));

        // 5️⃣ Transformer les feuilles en `StatistiquesRencontreDTO` avec le bon poste
        List<StatistiquesRencontreDTO> statsRencontres = feuillesDeMatch.stream()
                .map(feuille -> StatistiquesRencontreDTO.fromFeuilleDeMatchEntity(
                        feuille, postesParJoueur.getOrDefault(feuille.getJid(), Poste.INCONNU)))
                .toList();

        // 6️⃣ Regrouper les statistiques par joueur
        Map<Long, List<StatistiquesRencontreDTO>> statsParJoueur = statsRencontres.stream()
                .collect(Collectors.groupingBy(StatistiquesRencontreDTO::getJoueurId));

        // 7️⃣ Agréger les statistiques des joueurs sur tout le championnat
        List<StatistiquesChampionnatDTO> statsChampionnat = new ArrayList<>();
        for (Map.Entry<Long, List<StatistiquesRencontreDTO>> entry : statsParJoueur.entrySet()) {
            StatistiquesChampionnatDTO statsChamp = StatistiquesChampionnatDTO.fromChampionnat(entry.getValue(), idChampionnat);
            statsChampionnat.add(statsChamp);
        }

        // 8️⃣ Attribuer les points finaux selon le classement
        StatistiquesChampionnatDTO.attribuerPointsFinalChampionnat(statsChampionnat);

        // 9️⃣ Mise à jour des points en base sans instancier `Joueur`
        for (StatistiquesChampionnatDTO stats : statsChampionnat) {
            joueurRepository.updatePointsForPlayer(stats.getJoueurId(), stats.getPointsChamp());
        }

        // 🔟 Mettre à jour le statut du championnat
        championnat.setStatut(Statut.ENCOURS);
        championnatRepository.save(championnat);

        log.info("✅ Championnat ID={} clôturé avec succès et points attribués.", idChampionnat);
    }



    /**
     * 📌 Vérifie et met à jour le statut du championnat **avec promotion/relégation**.
     */
    private void verifierStatutChampionnatAvecObjectifs(Championnat championnat) {
        // 🔄 Rafraîchir les données du championnat avant de compter les matchs
        championnat = championnatRepository.findById(championnat.getIdChamp()).orElseThrow();

        int nombreRencontresJouees = (int) rencontreRepository.countRencontresTerminees(championnat.getIdChamp());
        int pointsActuels = championnat.getPointsActuels();
        int pointsRestantsPossibles = (MAX_RENCONTRES - nombreRencontresJouees) * 3; // 3 points max par match restant

        log.info("🔍 [Statut Championnat] ID={} | Matchs joués: {} | Points Actuels: {} | Points Restants Possibles: {}",
                championnat.getIdChamp(), nombreRencontresJouees, pointsActuels, pointsRestantsPossibles);

        // ✅ 1️⃣ Si toutes les rencontres sont jouées, on détermine le statut final
        if (nombreRencontresJouees >= MAX_RENCONTRES) {
            log.info("🏁 **Championnat terminé après {} rencontres.**", MAX_RENCONTRES);
            if (pointsActuels >= championnat.getPointsPromotion()) {
                championnat.setStatut(Statut.PROMOTION);
                log.info("🥇 **Promotion validée après {} matchs !**", MAX_RENCONTRES);
            } else if (pointsActuels < championnat.getPointsRelegation()) {
                championnat.setStatut(Statut.RELEGATION);
                log.info("📉 **Relégation confirmée après {} matchs !**", MAX_RENCONTRES);
            } else {
                championnat.setStatut(Statut.MAINTIEN);
                log.info("✅ **Championnat terminé, l'équipe reste dans sa division.**");
            }
        }
        // ✅ 2️⃣ Si la montée est **mathématiquement impossible**
        else if (pointsActuels + pointsRestantsPossibles < championnat.getPointsPromotion()) {
            championnat.setStatut(Statut.MAINTIEN);
            log.info("❌ **Même en gagnant tous les matchs restants, la promotion est impossible. Championnat terminé.**");
        }
        // ✅ 3️⃣ Si la relégation est **déjà actée**
        else if (pointsActuels < championnat.getPointsRelegation() && pointsActuels + pointsRestantsPossibles < championnat.getPointsRelegation()) {
            championnat.setStatut(Statut.RELEGATION);
            log.info("📉 **Même en gagnant tous les matchs restants, l’équipe descend. Relégation confirmée.**");
        }
        // ✅ 4️⃣ Si rien n’est encore décidé, le championnat continue
        else {
            championnat.setStatut(Statut.ENCOURS);
            log.info("⏳ **Championnat toujours en cours...**");
        }

        championnatRepository.saveAndFlush(championnat);
    }

    /**
     * 📌 Vérifie et met à jour le statut du championnat **sans promotion/relégation** (obligation de jouer 10 matchs).
     */
    private void verifierStatutChampionnatSansObjectifs(Championnat championnat) {
        // 🔄 Rafraîchir les données du championnat avant de compter les matchs
        championnat = championnatRepository.findById(championnat.getIdChamp()).orElseThrow();

        int nombreRencontresJouees = (int) rencontreRepository.countRencontresTerminees(championnat.getIdChamp());

        log.info("🔍 [Statut Championnat Sans Objectifs] ID={} | Matchs joués: {}", championnat.getIdChamp(), nombreRencontresJouees);

        if (nombreRencontresJouees >= MAX_RENCONTRES) {
            championnat.setStatut(Statut.MAINTIEN);
            log.info("🏆 **Championnat terminé après {} rencontres, maintien confirmé.**", MAX_RENCONTRES);
        } else {
            championnat.setStatut(Statut.ENCOURS);
            log.info("⏳ **Championnat en cours, {} matchs restants à jouer.**", MAX_RENCONTRES - nombreRencontresJouees);
        }

        championnatRepository.saveAndFlush(championnat);
    }

    /**
     * 📌 Vérifie et met à jour le statut du championnat **en fonction de son type**.
     */
    public void verifierStatutChampionnat(Championnat championnat) {
        log.info("📌 Vérification du statut du championnat ID={} | Points Actuels={} | Points Promotion={} | Points Relégation={}",
                championnat.getIdChamp(), championnat.getPointsActuels(), championnat.getPointsPromotion(), championnat.getPointsRelegation());

        if (championnat.getPointsPromotion() == 0 && championnat.getPointsRelegation() == 0) {
            log.info("⚠️ Championnat ID={} sans promotion ni relégation. Vérification sans objectifs en cours...", championnat.getIdChamp());
            verifierStatutChampionnatSansObjectifs(championnat);
        } else {
            log.info("🔍 Championnat ID={} avec objectifs de promotion/relégation. Vérification avec objectifs en cours...", championnat.getIdChamp());
            verifierStatutChampionnatAvecObjectifs(championnat);
        }

        log.info("✅ Vérification du statut du championnat terminée pour ID={}", championnat.getIdChamp());
    }


    /**
     * 📌 Supprime un championnat.
     */
    @Override
    @Transactional
    public void supprimerChampionnat(Long idChamp) {
        championnatRepository.deleteById(idChamp);
        log.info("🗑️ Championnat ID={} supprimé.", idChamp);
    }


    // =========================================================================
    // 🔹 SECTION 2 : RÉCUPÉRATION DES DONNÉES
    // =========================================================================


    /**
     * 📌 Récupère les 10 derniers championnats.
     */


    @Override
    public List<ChampionnatLightDTO> findTop10ByOrderByIdChampDesc() {
        return championnatRepository.findTop10ChampionnatLight();
    }


    /**
     * 📌 Récupère un championnat avec ses rencontres.
     */

    @Transactional
    @Override
    public ChampionnatDetailWithRencontresDTO findChampionnatWithRencontres(Long idChamp) {
        Championnat championnat = championnatRepository.findById(idChamp)
                .orElseThrow(() -> new IllegalArgumentException("Championnat non trouvé"));

        // ✅ Correction : Utiliser `mapRencontreDTOToDetailDTO` pour convertir chaque `RencontreDTO` en `RencontreDetailDTO`
        List<RencontreDetailDTO> rencontres = rencontreRepository.findRencontresByChampionnat(idChamp).stream()
                .map(rencontre -> mapRencontreDTOToDetailDTO(rencontre)) // ✅ Correction avec `mapRencontreDTOToDetailDTO`
                .collect(Collectors.toList());


        return new ChampionnatDetailWithRencontresDTO(
                championnat.getIdChamp(),
                championnat.getDivision(),
                championnat.getStatut(),
                championnat.getPointsActuels(),
                championnat.getPointsPromotion(),
                championnat.getPointsRelegation(),
                championnat.getRencontres().size(),
                rencontres
        );
    }




    @Override
    public ChampionnatOverviewDTO getChampionnatOverview(Long idChamp ) {
        List<ChampionnatLightDTO> derniersChampionnats = findTop10ByOrderByIdChampDesc();
        ChampionnatDetailWithRencontresDTO detailChampionnat = idChamp != null ? findChampionnatWithRencontres(idChamp) : null;
        return new ChampionnatOverviewDTO(derniersChampionnats, detailChampionnat);
    }


    // =========================================================================
    // 🔹 SECTION 3 : STATISTIQUES ET CLASSEMENTS
    // =========================================================================


    /**
     * 📌 Récupère les statistiques d'un championnat.
     */

    public List<StatistiquesChampionnatDTO> getStatistiquesChampionnat(Long idChampionnat) {
        Championnat championnat = championnatRepository.findById(idChampionnat)
                .orElseThrow(() -> new IllegalArgumentException("Championnat non trouvé"));

        return statChampionnatRepository.findByChampionnat(championnat)
                .stream()
                .map(this::mapToStatChampionnatDTO)
                .collect(Collectors.toList());
    }


    // =========================================================================
    // 🔹 SECTION 4 : MAPPERS
    // =========================================================================


    /**
     * 📌 Configure les règles spécifiques à chaque division (promotion & relégation).
     */
    private void configurerReglesDivision(Championnat championnat) {
        switch (championnat.getDivision()) {
            case DIV5, DIV4 -> {  // ✅ Seules DIV4 et DIV5 ont promotion/relegation
                championnat.setPointsPromotion(championnat.getDivision() == Division.DIV5 ? 12 : 14);
                championnat.setPointsRelegation(championnat.getDivision() == Division.DIV5 ? 10 : 12);
            }
            default -> {  // ✅ Les autres divisions ne gèrent pas la promotion/relegation
                championnat.setPointsPromotion(0);
                championnat.setPointsRelegation(0);
            }
        }
    }


    private ChampionnatDTO mapToDTO(Championnat championnat) {
        return new ChampionnatDTO(championnat.getIdChamp(), championnat.getDivision(), championnat.getStatut(),
                championnat.getPointsActuels(), championnat.getPointsPromotion(), championnat.getPointsRelegation(),
                championnat.getRencontres().size());
    }





    /**
     * 📌 Convertit un `StatChampionnat` en `StatistiquesChampionnatDTO` sans instancier `Joueur`.
     */
    private StatistiquesChampionnatDTO mapToStatChampionnatDTO(StatChampionnat stat) {
        // Récupération de la projection du joueur
        JoueurProjection joueurProjection = joueurRepository.findJoueurProjectionById(stat.getJoueurId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Joueur introuvable pour ID=" + stat.getJoueurId()));

        // Détection si le joueur est un gardien
        boolean estGardien = Poste.fromString(joueurProjection.getPoste()).isGardien();

        // Calcul des points en prenant en compte les clean sheets pour les gardiens
        int points = stat.getButsChamp() * 3 + stat.getPassesChamp();
        if (estGardien) {
            points += stat.getCleanSheet() * 2; // ✅ 2 points par clean sheet
        }

        // 📌 Construction du DTO avec toutes les statistiques nécessaires
        return new StatistiquesChampionnatDTO(
                stat.getChampionnat().getIdChamp(),  // ID du championnat
                stat.getJoueurId(),  // ID du joueur
                joueurProjection.getNom(),  // Nom du joueur via projection
                joueurProjection.getPoste(),  // Poste du joueur via projection
                stat.getButsChamp(),  // Buts marqués dans ce championnat
                stat.getPassesChamp(),  // Passes dans ce championnat
                stat.getMoyenneCoteChamp(),  // Moyenne de la cote dans ce championnat
                stat.getMinutesJoueesChamp(),  // Total des minutes jouées dans ce championnat
                points,  // ✅ Calcul des points avec clean sheets
                stat.getButArreterChamp(),  // 🥅 Nombre d'arrêts pour les gardiens
                stat.getButEncaisserChamp(),  // 🚨 Nombre de buts encaissés
                stat.getCleanSheet()  // 🏆 Nombre de clean sheets
        );
    }







    private RencontreDetailDTO mapRencontreDTOToDetailDTO(RencontreDTO dto) {
        // 🔍 Récupération des projections de feuilles de match
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(dto.getRid())
                .stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Garder uniquement ceux qui ont joué
                .toList();

        // 🔹 Convertir les projections en `FeuilleDeMatchDTO`
        List<FeuilleDeMatchDTO> feuilles = projections.stream()
                .map(FeuilleDeMatchDTO::fromProjection)
                .toList();

        // 🔹 Récupérer les joueurs et leur poste via `joueurRepository`
        Map<Long, Poste> postesParJoueur = joueurRepository.findPostesByJoueurIds(
                feuilles.stream().map(FeuilleDeMatchDTO::getJid).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
                JoueurLightProjection::getJid,
                projection -> Poste.valueOf(projection.getPoste()) // ✅ Conversion `String -> Poste`
        ));



        // 🔹 Correction : Passer `clotureDTO` explicitement
        List<StatistiquesRencontreDTO> statsJoueurs = feuilles.stream()
                .map(feuille -> StatistiquesRencontreDTO.fromFeuilleDeMatch(feuille)) // ✅ Correction avec lambda
                .collect(Collectors.toList());

        return new RencontreDetailDTO(
                dto.getRid(),
                dto.getIdChamp(),
                "HERSTAL FC",
                dto.getButEquipe(),
                dto.getNomAdversaire(),
                dto.getButAdversaire(),
                Optional.ofNullable(dto.getDivisionAdversaire()).map(Division::getDescription).orElse("INCONNU"),
                dto.getHommeDuMatchId(),
                dto.getHommeDuMatchNom(),
                dto.getStatutRencontre(),
                statsJoueurs
        );
    }




}

