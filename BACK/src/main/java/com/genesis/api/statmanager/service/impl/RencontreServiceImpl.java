package com.genesis.api.statmanager.service.impl;

import com.genesis.api.statmanager.dto.*;
import com.genesis.api.statmanager.dto.global.StatistiquesChampionnatDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import com.genesis.api.statmanager.dto.rencontre.*;
import com.genesis.api.statmanager.dto.statistique.StatCompositeDTO;
import com.genesis.api.statmanager.dto.statistique.StatCritereDTO;
import com.genesis.api.statmanager.dto.statistique.StatTopJoueurDTO;
import com.genesis.api.statmanager.model.*;
import com.genesis.api.statmanager.model.enumeration.*;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import com.genesis.api.statmanager.projection.JoueurProjection;
import com.genesis.api.statmanager.repository.*;
import com.genesis.api.statmanager.service.ChampionnatService;
import com.genesis.api.statmanager.service.RencontreService;
import com.genesis.api.statmanager.service.StatistiqueManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des rencontres.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RencontreServiceImpl implements RencontreService {

    private final RencontreRepository rencontreRepository;
    private final FeuilleDeMatchRepository feuilleDeMatchRepository;
    private final ChampionnatRepository championnatRepository;
    private final ChampionnatService championnatService;
    private final JoueurRepository joueurRepository;
    private final StatChampionnatRepository statChampionnatRepository;
    private final StatistiqueManager statistiqueManager;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MAX_RENCONTRES = 10;

    @PersistenceContext
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // 📌 1️⃣ GESTION DES RENCONTRES
    // -------------------------------------------------------------------------

    /**
     * ✅ Supprime une rencontre par son ID.
     */
    @Transactional
    public void supprimerRencontre(Long idRencontre) {
        log.info("🗑 Suppression de la rencontre ID={}", idRencontre);

        if (!rencontreRepository.existsById(idRencontre)) {
            throw new IllegalArgumentException("❌ Rencontre non trouvée.");
        }

        rencontreRepository.deleteById(idRencontre);
        log.info("✅ Rencontre ID={} supprimée avec succès.", idRencontre);
    }

    /**
     * ✅ Récupère toutes les rencontres d'un championnat sous forme de DTO.
     */
    public List<RencontreDTO> findByChampionnat(Long idChampionnat) {
        if (!championnatRepository.existsById(idChampionnat)) {
            throw new IllegalArgumentException("❌ Championnat non trouvé");
        }

        return rencontreRepository.findRencontresByChampionnat(idChampionnat);
    }

    /**
     * ✅ Récupère toutes les rencontres terminées.
     */
    public List<RencontreDTO> findRencontresTerminees() {
        return rencontreRepository.findByStatutRencontre(StatutRencontre.TERMINE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Récupère les détails statistiques d'une rencontre.
     */
    public RencontreDetailDTO getRencontreDetailsStat(Long idRencontre) {
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre introuvable (ID=" + idRencontre + ")"));

        int scoreEquipeLocale = rencontre.getFeuilleDeMatchs().stream()
                .mapToInt(FeuilleDeMatch::getButs)
                .sum();

        List<StatistiquesRencontreDTO> statsJoueurs = rencontre.getFeuilleDeMatchs().stream()
                .map(feuille -> {
                    List<Long> idPasseurs = feuille.getPasseurs().stream()
                            .map(Joueur::getJid)
                            .toList();

                    List<String> nomPasseurs = feuille.getPasseurs().stream()
                            .map(Joueur::getNom)
                            .toList();

                    if (nomPasseurs.isEmpty()) {
                        nomPasseurs = List.of("Aucun passeur");
                    }

                    return new StatistiquesRencontreDTO(
                            feuille.getJoueurId(),
                            feuille.getJoueur().getNom(),
                            feuille.getJoueur().getClass().getSimpleName(),
                            feuille.getJoueur().getPoste().name(),
                            feuille.getButs(),
                            feuille.getButs() / Math.max(feuille.getMinutesJouees(), 1),
                            feuille.getPasses(),
                            feuille.getPasses() / Math.max(feuille.getMinutesJouees(), 1),
                            (feuille.getButs() + feuille.getPasses()) / 2.0,
                            feuille.getMoyenneCote(),
                            (feuille.getButs() * 3) + feuille.getPasses(),
                            0, // pointsParMinute si besoin
                            feuille.getMinutesJouees(),
                            rencontre.getRid(),
                            idPasseurs,
                            nomPasseurs
                    );
                })
                .toList();

        String hommeDuMatchNom = (rencontre.getHommeDuMatch() != null)
                ? rencontre.getHommeDuMatch().getNom()
                : "Aucun homme du match";

        return new RencontreDetailDTO(
                rencontre.getRid(),
                rencontre.getChampionnat().getIdChamp(),
                "HERSTAL FC",
                scoreEquipeLocale,
                rencontre.getNomAdversaire(),
                rencontre.getButAdversaire(),
                Division.fromString(String.valueOf(rencontre.getDivisionAdversaire())).getDescription(),
                hommeDuMatchNom,
                rencontre.getStatutRencontre(),
                statsJoueurs
        );
    }

    /**
     * ✅ Calcule le bilan des rencontres d'un championnat.
     */
    @Transactional(readOnly = true)
    public BilanRencontreDTO getBilanRencontresParChampionnat(Long idChampionnat) {
        log.info("📊 Calcul du bilan des rencontres pour le championnat ID={}", idChampionnat);

        List<Rencontre> rencontres = rencontreRepository.findAllRencontresByChampionnat(idChampionnat);

        long totalRencontres = rencontres.size();
        long totalButsMarques = rencontres.stream().mapToLong(Rencontre::getScoreEquipeLocale).sum();
        long totalButsAdversaires = rencontres.stream().mapToLong(Rencontre::getButAdversaire).sum();
        long totalVictoires = rencontres.stream().filter(r -> r.getScoreEquipeLocale() > r.getButAdversaire()).count();
        long totalDefaites = rencontres.stream().filter(r -> r.getScoreEquipeLocale() < r.getButAdversaire()).count();
        long totalMatchsNuls = totalRencontres - (totalVictoires + totalDefaites);

        return new BilanRencontreDTO(
                totalRencontres, totalButsMarques, totalButsAdversaires,
                totalVictoires, totalDefaites, totalMatchsNuls
        );
    }

    // -------------------------------------------------------------------------
    // 📌 2️⃣ SÉLECTION ET PRÉPARATION DES RENCONTRES
    // -------------------------------------------------------------------------

    /**
     * Prépare la sélection pour une nouvelle rencontre.
     * Récupère la dernière rencontre jouée pour le championnat,
     * la liste des joueurs du championnat et ceux ayant joué la dernière rencontre.
     */
    @Transactional
    public NouvelleRencontreSelectionDTO getNouvelleRencontreSelection(Long idChampionnat) {
        log.info("📥 Préparation de la nouvelle rencontre pour le championnat ID={}", idChampionnat);

        // 1️⃣ Récupération de la dernière rencontre jouée
        Long idDerniereRencontre = rencontreRepository.findDerniereRencontreJouee(idChampionnat)
                .map(Rencontre::getRid)
                .orElse(null);

        // 2️⃣ Récupération des joueurs du championnat sous forme de DTO
        List<JoueurDTO> joueursChampionnat = statChampionnatRepository.findJoueursByChampionnatId(idChampionnat)
                .stream()
                .map(stat -> new JoueurDTO(stat.getId(), stat.getNom(), Poste.fromString(stat.getPoste()))) // Utilisation de toString pour le poste
                .toList();


        // 3️⃣ Récupération des joueurs ayant joué la dernière rencontre, sans instancier Joueur
        List<JoueurDTO> joueursPrecedents = (idDerniereRencontre != null)
                ? feuilleDeMatchRepository.findByRencontre(idDerniereRencontre)
                .stream()
                .map(feuille -> new JoueurDTO(
                        feuille.getJoueurId(),
                        feuille.getJoueur().getNom(),  // ✅ Récupération du vrai nom du joueur
                        feuille.getJoueur().getPoste().toString(), // ✅ Récupération du poste correct
                        feuille.getJoueur().getCategoriePoste(), // ✅ Ajout de la catégorie du poste
                        feuille.getJoueur().getPoint(), // ✅ Ajout des points
                        feuille.getJoueur().getTypeJoueur() // ✅ Ajout du type de joueur
                ))
                .toList()
                : List.of();






        log.info("👥 Joueurs ayant joué la dernière rencontre : {}", joueursPrecedents.size());

        // 4️⃣ Retourne l'objet NouvelleRencontreSelectionDTO
        return new NouvelleRencontreSelectionDTO(
                idChampionnat,
                idDerniereRencontre,
                "",            // Nom de l'adversaire (vide pour l'instant)
                null,          // Division de l'adversaire (à définir plus tard)
                joueursPrecedents,
                joueursChampionnat
        );
    }

    /**
     * Valide la sélection des joueurs avant le match.
     * Vérifie que le nombre de titulaires et remplaçants correspond aux exigences,
     * puis crée la rencontre.
     */
    /**
     * ✅ Valide la sélection des joueurs dans le vestiaire et crée la rencontre.
     */
    @Transactional
    public TerrainDTO validerSelectionVestiaire(VestiaireDTO vestiaireDTO) {
        log.info("✅ Validation de la sélection des joueurs pour la rencontre...");

        // 1️⃣ Vérification du nombre exact de joueurs
        if (vestiaireDTO.getTitulaires().size() != 11) {
            throw new IllegalArgumentException("❌ Il faut exactement 11 titulaires !");
        }
        if (vestiaireDTO.getRemplacants().size() != 12) {
            throw new IllegalArgumentException("❌ Il faut exactement 12 remplaçants !");
        }

        log.info("📌 Sélection validée : {} titulaires et {} remplaçants",
                vestiaireDTO.getTitulaires().size(), vestiaireDTO.getRemplacants().size());

        // 2️⃣ Création de la rencontre et récupération du terrain
        TerrainDTO terrainDTO = creerNouvelleRencontre(vestiaireDTO);
        log.info("✅ Rencontre ID={} créée après validation", terrainDTO.getIdRencontre());

        return terrainDTO;
    }



    /**
     * ✅ Crée une nouvelle rencontre et initialise directement le terrain.
     */
    @Transactional
    public TerrainDTO creerNouvelleRencontre(VestiaireDTO vestiaireDTO) {
        log.info("📥 Création d'une nouvelle rencontre pour le championnat ID={}...", vestiaireDTO.getIdChampionnat());

        // 1️⃣ Récupération du championnat
        Championnat championnat = championnatRepository.findByIdWithRencontres(vestiaireDTO.getIdChampionnat());

        // 2️⃣ Création de la rencontre
        Rencontre rencontre = new Rencontre();
        rencontre.setChampionnat(championnat);
        rencontre.setNomAdversaire(vestiaireDTO.getNomAdversaire());
        rencontre.setDivisionAdversaire(vestiaireDTO.getDivisionAdversaire());
        rencontre.setStatutRencontre(StatutRencontre.EN_ATTENTE);

        // 3️⃣ Construction des maps pour identifier les titulaires et remplaçants
        Map<Long, Boolean> titulairesMap = new HashMap<>();
        vestiaireDTO.getTitulaires().forEach(joueur -> titulairesMap.put(joueur.getId(), true));
        vestiaireDTO.getRemplacants().forEach(joueur -> titulairesMap.put(joueur.getId(), false));

        // 4️⃣ Récupération des joueurs en base
        List<Long> joueurIds = new ArrayList<>(titulairesMap.keySet());
        List<Joueur> joueurs = joueurRepository.findAllById(joueurIds);
        Map<Long, Joueur> joueurMap = joueurs.stream().collect(Collectors.toMap(Joueur::getJid, j -> j));

        // 5️⃣ Vérification que tous les joueurs existent
        if (joueurMap.size() != joueurIds.size()) {
            throw new IllegalStateException("❌ Certains joueurs sont introuvables dans la base de données.");
        }

        // 6️⃣ Création des feuilles de match
        for (Long joueurId : joueurIds) {
            FeuilleDeMatch feuille = new FeuilleDeMatch();
            feuille.setRencontre(rencontre);
            feuille.setJoueurId(joueurId);
            feuille.setJoueur(joueurMap.get(joueurId)); // ✅ Assigne l’objet `Joueur`
            feuille.setTitulaire(titulairesMap.getOrDefault(joueurId, false));
            feuille.setMinutesJouees(feuille.isTitulaire() ? TimePlay.MINUTES90.getPercentage() : TimePlay.MINUTES0.getPercentage());

            log.info("👤 Joueur ID={} - Titulaire: {} - Minutes attribuées: {}",
                    joueurId, feuille.isTitulaire(), feuille.getMinutesJouees());

            rencontre.addFeuilleDeMatch(feuille);
        }

        // 7️⃣ Sauvegarde en base
        rencontreRepository.saveAndFlush(rencontre);

        log.info("✅ Rencontre ID={} créée avec {} joueurs", rencontre.getRid(), joueurIds.size());

        // 8️⃣ Initialisation du terrain après création de la rencontre
        return initialiserTerrain(rencontre.getRid()); // ✅ Envoie directement l'ID de la rencontre
    }





    // -------------------------------------------------------------------------
        // 📌 3️⃣ GESTION DU TERRAIN ET MATCH EN COURS
        // -------------------------------------------------------------------------

    /**
     * ✅ Initialise le terrain avant le match avec les joueurs validés.
     */
    @Transactional
    public TerrainDTO initialiserTerrain(Long idRencontre) {
        log.info("📥 Construction initiale du terrain pour la rencontre ID={}", idRencontre);

        // 1️⃣ Vérification de l'existence de la rencontre
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre non trouvée"));

        // 2️⃣ Récupération des projections de feuilles de match
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre);
        if (projections.isEmpty()) {
            throw new IllegalStateException("❌ Aucune feuille de match trouvée pour la rencontre ID=" + idRencontre);
        }

        log.info("📄 Feuilles de match récupérées : {}", projections.size());

        // 3️⃣ Transformation des projections en DTOs via `transformerEnTerrainDTO`
        TerrainDTO terrainDTO = transformerEnTerrainDTO(rencontre, projections);

        // ✅ 4️⃣ Mise à jour spécifique de la disposition des joueurs (`terrainJoueurs`)
        Map<String, FeuilleDeMatchDTO> terrainJoueurs = new LinkedHashMap<>();

        // ✅ Initialisation des postes sur le terrain
        List<String> postes = List.of("GB", "DC_GAUCHE", "DC_DROIT", "DG", "DD", "MDF", "MO", "MR", "AIG", "AID", "AC");
        for (String poste : postes) {
            terrainJoueurs.put(poste, null);
        }

        int dcCount = 0;
        for (FeuilleDeMatchDTO joueur : terrainDTO.getTitulaires()) {
            if ("DC".equals(joueur.getPoste())) {
                if (dcCount == 0) {
                    terrainJoueurs.put("DC_GAUCHE", joueur);
                    dcCount++;
                } else if (dcCount == 1) {
                    terrainJoueurs.put("DC_DROIT", joueur);
                    dcCount++;
                }
            } else if (terrainJoueurs.containsKey(joueur.getPoste())) {
                terrainJoueurs.putIfAbsent(joueur.getPoste(), joueur);
            }
        }

        // ✅ 5️⃣ Reconstruction du `TerrainDTO` avec `terrainJoueurs` mis à jour
        TerrainDTO terrainFinal = TerrainDTO.builder()
                .idRencontre(terrainDTO.getIdRencontre())
                .idChampionnat(terrainDTO.getIdChampionnat())
                .nomAdversaire(rencontre.getNomAdversaire())
                .divisionAdversaire(rencontre.getDivisionAdversaire())
                .butEquipe(terrainDTO.getButEquipe())
                .butAdversaire(terrainDTO.getButAdversaire())
                .titulaires(terrainDTO.getTitulaires())
                .remplacants(terrainDTO.getRemplacants())
                .terrainJoueurs(terrainJoueurs) // ✅ Mise à jour spécifique de la disposition
                .build();

        log.info("✅ Terrain initialisé avec {} titulaires et {} remplaçants pour la rencontre ID={}",
                terrainFinal.getTitulaires().size(), terrainFinal.getRemplacants().size(), idRencontre);

        return terrainFinal;
    }






    /**
         * ✅ Récupère l'état actuel du terrain.
         */
        @Transactional
        public TerrainDTO getTerrain(Long idRencontre) {
            log.info("📥 Récupération de l'état du terrain pour la rencontre ID={}", idRencontre);

            // 1️⃣ Vérification de l'existence de la rencontre
            Rencontre rencontre = rencontreRepository.findById(idRencontre)
                    .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre non trouvée"));

        // 2️⃣ Rafraîchissement des données pour éviter le cache Hibernate
        feuilleDeMatchRepository.flush();
        entityManager.clear();

        // 3️⃣ Récupération des feuilles de match sous forme de projection
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre);
        if (projections.isEmpty()) {
            throw new IllegalStateException("❌ Aucune feuille de match trouvée pour la rencontre ID=" + idRencontre);
        }

        // 4️⃣ Transformation en `TerrainDTO` via la méthode utilitaire
        return transformerEnTerrainDTO(rencontre, projections);
    }


    /**
     * ✅ Met à jour les statistiques en temps réel (buts et passes) avec transactions distinctes.
     */
    @Transactional
    public TerrainDTO updateStatsEnTempsReel(Long idRencontre, Long idJoueur, int buts, Long idPasseur) {
        log.info("📢 [Début] Mise à jour des stats en temps réel - Rencontre ID={} | Joueur ID={} | Buts={} | Passeur={}",
                idRencontre, idJoueur, buts, idPasseur);

        // 1️⃣ Vérification et récupération de la feuille de match du buteur
        Optional<Long> feuilleButeurIdOpt = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre).stream()
                .filter(f -> f.getJid().equals(idJoueur))
                .map(FeuilleDeMatchProjection::getId)
                .findFirst();

        if (feuilleButeurIdOpt.isEmpty()) {
            throw new IllegalArgumentException("❌ Feuille de match introuvable pour le joueur ID=" + idJoueur);
        }

        Long feuilleButeurId = feuilleButeurIdOpt.get();

        // 2️⃣ Mise à jour des buts avec transaction distincte
        updateButsTransactionnel(feuilleButeurId, buts, 0); // On ne met pas à jour les passes ici

        // 3️⃣ Mise à jour des passes si un passeur est fourni
        if (idPasseur != null && buts > 0) {
            Optional<Long> feuillePasseurIdOpt = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre).stream()
                    .filter(f -> f.getJid().equals(idPasseur))
                    .map(FeuilleDeMatchProjection::getId)
                    .findFirst();

            if (feuillePasseurIdOpt.isPresent()) {
                Long feuillePasseurId = feuillePasseurIdOpt.get();
                updatePassesTransactionnel(feuillePasseurId, 1);
                feuilleDeMatchRepository.addPasseurByIds(feuilleButeurId, idPasseur);
            }
        }

        // 4️⃣ Récupération et retour du terrain mis à jour
        log.info("📥 [Fin] Récupération du terrain mis à jour pour la rencontre ID={}", idRencontre);
        return getTerrain(idRencontre);
    }



    /**
     * ✅ Modifie la disposition des joueurs après un remplacement.
     */
    @Transactional
    public TerrainDTO effectuerRemplacement(RemplacementDTO remplacementDTO) {
        log.info("🔄 Remplacement - Rencontre ID={} | Sortant={} | Entrant={} | Minute={}",
                remplacementDTO.getIdRencontre(), remplacementDTO.getIdTitulaireSortant(),
                remplacementDTO.getIdRemplacantEntrant(), remplacementDTO.getMinuteEntree());

        // 1️⃣ Récupération des feuilles de match des joueurs concernés
        FeuilleDeMatch feuilleSortant = feuilleDeMatchRepository.findByRencontreAndJoueurId(
                        remplacementDTO.getIdRencontre(), remplacementDTO.getIdTitulaireSortant())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ Titulaire sortant introuvable"));

        FeuilleDeMatch feuilleRemplacant = feuilleDeMatchRepository.findByRencontreAndJoueurId(
                        remplacementDTO.getIdRencontre(), remplacementDTO.getIdRemplacantEntrant())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ Remplaçant introuvable"));

        // 2️⃣ Mise à jour des statuts et des minutes jouées
        double minutesSortant = remplacementDTO.getMinuteEntree().getPercentage();
        double minutesEntrant = 1.0 - minutesSortant;

        // ✅ Correction de la logique demandée
        feuilleSortant.setMinutesJouees(minutesSortant);
        feuilleSortant.setTitulaire(true);  // ✅ Il reste titulaire
        feuilleSortant.setAJoue(false);  // ✅ Il ne joue plus après remplacement

        feuilleRemplacant.setMinutesJouees(minutesEntrant);
        feuilleRemplacant.setTitulaire(false);  // ✅ Il reste remplaçant
        feuilleRemplacant.setAJoue(true);  // ✅ Il commence à jouer

        feuilleDeMatchRepository.saveAll(List.of(feuilleSortant, feuilleRemplacant));

        // 3️⃣ Récupération des feuilles mises à jour
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(remplacementDTO.getIdRencontre());

        // 4️⃣ Mise à jour des titulaires et remplaçants
        List<FeuilleDeMatchDTO> titulaires = new ArrayList<>();
        List<FeuilleDeMatchDTO> remplacants = new ArrayList<>();
        Map<String, FeuilleDeMatchDTO> terrainJoueurs = new LinkedHashMap<>();

        // ✅ Préparation des postes pour le terrain
        List<String> postes = List.of("GB", "DC_GAUCHE", "DC_DROIT", "DG", "DD", "MDF", "MO", "MR", "AIG", "AID", "AC");
        for (String poste : postes) {
            terrainJoueurs.put(poste, null);
        }

        // ✅ Réorganisation des joueurs après remplacement
        for (FeuilleDeMatchProjection feuille : projections) {
            FeuilleDeMatchDTO dto = new FeuilleDeMatchDTO(
                    feuille.getId(),
                    feuille.getRid(),
                    feuille.getJid(),
                    feuille.getNom(),
                    feuille.getPoste(), // ✅ Convertir String en Enum Poste
                    feuille.getButs(),
                    feuille.getPasses(),
                    feuille.getMoyenneCote(),
                    feuille.getMinutesJouees(),
                    feuille.getAJoue(),
                    feuille.isTitulaire(),
                    feuille.getButArreter(),
                    feuille.getButEncaisser(),
                    (feuille.getPasseursIds() != null && !feuille.getPasseursIds().isEmpty()) ?
                            Arrays.stream(feuille.getPasseursIds().split(","))
                                    .filter(id -> !id.trim().isEmpty())
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList()) :
                            new ArrayList<>(),   // ⚠️ Ici se produit l'erreur (mauvaise inférence de type)
                    (feuille.getNomsPasseurs() != null && !feuille.getNomsPasseurs().isEmpty()) ?
                            Arrays.asList(feuille.getNomsPasseurs().split(",")) :
                            new ArrayList<>()   // ⚠️ Ici aussi, potentielle erreur de type
            );


            if (feuille.getJid().equals(remplacementDTO.getIdTitulaireSortant())) {
                remplacants.add(dto);  // ✅ Ajoute le joueur sortant aux remplaçants
            } else if (feuille.getJid().equals(remplacementDTO.getIdRemplacantEntrant())) {
                titulaires.add(dto);  // ✅ Ajoute le joueur entrant aux titulaires
            } else {
                if (feuille.isTitulaire()) {
                    titulaires.add(dto);
                } else {
                    remplacants.add(dto);
                }
            }

            if (terrainJoueurs.containsKey(feuille.getPoste())) {
                terrainJoueurs.putIfAbsent(feuille.getPoste(), dto);
            }
        }

        log.info("✅ Après remplacement - Titulaires: {}, Remplaçants: {}", titulaires.size(), remplacants.size());

        // ✅ Retourne le `TerrainDTO` mis à jour
        return TerrainDTO.builder()
                .idRencontre(remplacementDTO.getIdRencontre())
                .idChampionnat(feuilleSortant.getRencontre().getChampionnat().getIdChamp())
                .nomAdversaire(feuilleSortant.getRencontre().getNomAdversaire())
                .butEquipe(titulaires.stream().mapToInt(FeuilleDeMatchDTO::getButs).sum())
                .butAdversaire(feuilleSortant.getRencontre().getButAdversaire())
                .titulaires(titulaires)
                .remplacants(remplacants)
                .terrainJoueurs(terrainJoueurs)
                .build();
    }


    /**
     * ✅ Transforme une liste de feuilles de match en `TerrainDTO`.
     */
    private TerrainDTO transformerEnTerrainDTO(Rencontre rencontre, List<FeuilleDeMatchProjection> projections) {
        List<FeuilleDeMatchDTO> feuillesDeMatchDTO = projections.stream()
                .map(feuille -> new FeuilleDeMatchDTO(
                        feuille.getId(),
                        feuille.getRid(),
                        feuille.getJid(),
                        feuille.getNom(),
                        String.valueOf(feuille.getPoste()), // ✅ Convertir String en Enum Poste
                        feuille.getButs(),
                        feuille.getPasses(),
                        feuille.getMoyenneCote(),
                        feuille.getMinutesJouees(),
                        feuille.getAJoue(),
                        feuille.isTitulaire(),
                        feuille.getButArreter(),
                        feuille.getButEncaisser(),
                        (feuille.getPasseursIds() != null && !feuille.getPasseursIds().isEmpty())
                                ? Arrays.stream(feuille.getPasseursIds().split(","))
                                .map(String::trim) // ✅ Supprime les espaces autour
                                .filter(id -> !id.isEmpty()) // ✅ Ignore les valeurs vides
                                .map(Long::parseLong) // ✅ Convertit uniquement les valeurs valides
                                .collect(Collectors.toList())
                                : new ArrayList<>(),
                        (feuille.getNomsPasseurs() != null && !feuille.getNomsPasseurs().isEmpty())
                                ? Arrays.stream(feuille.getNomsPasseurs().split(","))
                                .map(String::trim) // ✅ Supprime les espaces autour
                                .filter(nom -> !nom.isEmpty()) // ✅ Ignore les valeurs vides
                                .collect(Collectors.toList())
                                : new ArrayList<>()
                ))
                .toList();

        return TerrainDTO.builder()
                .idRencontre(rencontre.getRid())
                .idChampionnat(rencontre.getChampionnat().getIdChamp())
                .nomAdversaire(rencontre.getNomAdversaire())
                .butEquipe(rencontre.getFeuilleDeMatchs().stream().mapToInt(FeuilleDeMatch::getButs).sum())
                .butAdversaire(rencontre.getButAdversaire())
                .titulaires(feuillesDeMatchDTO.stream().filter(FeuilleDeMatchDTO::isTitulaire).toList())
                .remplacants(feuillesDeMatchDTO.stream().filter(f -> !f.isTitulaire()).toList())
                .build();
    }

    // -------------------------------------------------------------------------
    // 📌 4️⃣ FIN DE MATCH ET MISE À JOUR DES STATS
    // -------------------------------------------------------------------------

    @Transactional
    public FinMatchDTO cloturerRencontre(ClotureRencontreDTO clotureDTO) {
        log.info("📌 Clôture de la rencontre ID={}", clotureDTO.getIdRencontre());

        // 1️⃣ Récupération des données de la rencontre
        Rencontre rencontre = rencontreRepository.findById(clotureDTO.getIdRencontre())
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre introuvable"));

        List<FeuilleDeMatchProjection> feuillesProjections = feuilleDeMatchRepository.findAllByRencontreId(clotureDTO.getIdRencontre());
        Championnat championnat = rencontre.getChampionnat();
        Long idChampionnat = championnat.getIdChamp();

        // 2️⃣ Mise à jour des statistiques
        majStatsFeuilleDeMatch(feuillesProjections, clotureDTO);
        majStatsChampionnat(feuillesProjections, clotureDTO, rencontre);
        majStatsJoueur(feuillesProjections, clotureDTO);

        // 3️⃣ Transformation des feuilles de match en StatistiquesRencontreDTO
        List<StatistiquesRencontreDTO> statsRencontres = feuillesProjections.stream()
                .map(StatistiquesRencontreDTO::fromFeuilleDeMatchProjection)
                .collect(Collectors.toList());

        // 4️⃣ Déterminer l’Homme du Match
        Long hommeDuMatchId = StatistiquesRencontreDTO.determinerHommeDuMatch(statsRencontres);
        rencontre.setHommeDuMatch(hommeDuMatchId != null ? joueurRepository.findById(hommeDuMatchId).orElse(null) : null);
        rencontreRepository.save(rencontre);

        // 5️⃣ Mise à jour des Points de la Rencontre
        int pointsEquipe = calculerPointsRencontre(rencontre);
        championnat.setPointsActuels(championnat.getPointsActuels() + pointsEquipe);
        championnat.updateStatut();

        // 6️⃣ Mise à jour du score et du statut de la rencontre
        rencontre.setButEquipe(rencontre.getScoreEquipeLocale());
        rencontre.setStatutRencontre(StatutRencontre.TERMINE);
        rencontreRepository.save(rencontre);
        championnatRepository.save(championnat);

        championnatService.verifierStatutChampionnat(championnat);

        if (championnat.getStatut() == Statut.PROMOTION || championnat.getStatut() == Statut.RELEGATION || championnat.getStatut() == Statut.MAINTIEN) {
            log.info("🏁 **Fin du championnat ID={} selon les règles de promotion/relégation.**", idChampionnat);
            championnatService.cloturerChampionnat(idChampionnat);
        }

        // 7️⃣ Récupération des événements du match via `getHistoriqueEvenements`
        List<EvenementMatchDTO> evenementsMatch = getHistoriqueEvenements(rencontre.getRid());

        // 8️⃣ Construction et retour du DTO FinMatchDTO
        return new FinMatchDTO(
                rencontre.getRid(),
                rencontre.getNomEquipe(),
                rencontre.getButEquipe(),
                rencontre.getNomAdversaire(),
                rencontre.getButAdversaire(),
                hommeDuMatchId != null ? joueurRepository.findById(hommeDuMatchId).map(Joueur::getNom).orElse("Inconnu") : "Inconnu",
                statsRencontres,
                evenementsMatch
        );
    }



    /** Récupère l'historique des buts d'une rencontre. */
    @Transactional(readOnly = true)
    public List<EvenementMatchDTO> getHistoriqueEvenements(Long idRencontre) {
        log.info("📥 Récupération de l'historique des buts et des remplacements pour la rencontre ID={}", idRencontre);

        TerrainDTO terrainDTO = getTerrain(idRencontre);
        List<EvenementMatchDTO> historique = new ArrayList<>();

        // 📌 Ajout des buts marqués
        terrainDTO.getButsModifies().forEach((joueurId, nbButs) -> {
            FeuilleDeMatchDTO joueur = terrainDTO.getTerrainJoueurs().values().stream()
                    .filter(j -> j.getJid().equals(joueurId))
                    .findFirst()
                    .orElse(null);

            if (joueur != null) {
                for (int i = 0; i < nbButs; i++) {
                    historique.add(new EvenementMatchDTO(
                            idRencontre,
                            joueurId,
                            joueur.getNom(),
                            terrainDTO.getMinutesJouees().get(joueurId).intValue(),
                            "BUT",
                            joueur.getPasseursId().isEmpty() ? null : joueur.getPasseursId().get(0),
                            joueur.getNomsPasseurs().isEmpty() ? "Sans passeur" : joueur.getNomsPasseurs().get(0)
                    ));
                }
            }
        });

        // 📌 Ajout des remplacements
        for (FeuilleDeMatchDTO remplacant : terrainDTO.getRemplacants()) {
            Long idEntrant = remplacant.getJid();
            Double minuteEntree = terrainDTO.getMinutesJouees().get(idEntrant);
            if (minuteEntree != null) {
                historique.add(new EvenementMatchDTO(
                        idRencontre,
                        idEntrant,
                        remplacant.getNom(),
                        minuteEntree.intValue(),
                        "REMPLACEMENT",
                        null,
                        null
                ));
            }
        }

        historique.sort(Comparator.comparingInt(EvenementMatchDTO::getMinute));

        log.info("✅ {} événements trouvés pour la rencontre ID={} ", historique.size(), idRencontre);
        return historique;
    }


    // -------------------------------------------------------------------------
    // 📌 5️⃣ VÉRIFICATIONS ET MISES À JOUR TRANSACTIONNELLES
    // -------------------------------------------------------------------------

    /**
     * ✅ Met à jour les buts et passes d'un joueur dans une transaction distincte.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateButsTransactionnel(Long idFeuilleMatch, int buts, int passes) {
        log.info("🔄 Mise à jour transactionnelle des buts - Feuille ID={} | Buts={} | Passes={}",
                idFeuilleMatch, buts, passes);

        int butsActuels = feuilleDeMatchRepository.getButsFromDatabase(idFeuilleMatch);

        feuilleDeMatchRepository.updateFeuilleStats(idFeuilleMatch, butsActuels + buts, passes);
        feuilleDeMatchRepository.clearCache(); // ✅ Assure que la mise à jour est bien prise en compte

        log.info("✅ Mise à jour effectuée : Feuille ID={} | Nouveaux Buts={} | Passes={}",
                idFeuilleMatch, butsActuels + buts, passes);
    }

    /**
     * ✅ Met à jour uniquement les passes d'un joueur dans une transaction distincte.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePassesTransactionnel(Long idFeuilleMatch, int passes) {
        log.info("🔄 Mise à jour transactionnelle des passes - Feuille ID={} | Passes={}",
                idFeuilleMatch, passes);

        feuilleDeMatchRepository.updatePasses(idFeuilleMatch, passes);
        feuilleDeMatchRepository.clearCache(); // ✅ Assure que la mise à jour est bien prise en compte

        log.info("✅ Mise à jour effectuée : Feuille ID={} | Passes={}", idFeuilleMatch, passes);
    }



    /**
     * ✅ Récupère les statistiques des joueurs pour une rencontre donnée.
     */
    @Transactional(readOnly = true)
    public List<StatistiquesRencontreDTO> getStatistiquesRencontre(Long idRencontre) {
        log.info("📊 Récupération des statistiques de la rencontre ID={}", idRencontre);

        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre introuvable (ID=" + idRencontre + ")"));

        return rencontre.getFeuilleDeMatchs().stream()
                .map(feuille -> new StatistiquesRencontreDTO(
                        feuille.getJoueurId(),
                        feuille.getJoueur().getNom(),
                        feuille.getJoueur().getClass().getSimpleName(),
                        feuille.getJoueur().getPoste().name(),
                        feuille.getButs(),
                        feuille.getButs() / Math.max(feuille.getMinutesJouees(), 1),
                        feuille.getPasses(),
                        feuille.getPasses() / Math.max(feuille.getMinutesJouees(), 1),
                        (feuille.getButs() + feuille.getPasses()) / 2.0,
                        feuille.getMoyenneCote(),
                        (feuille.getButs() * 3) + feuille.getPasses(),
                        0, // pointsParMinute si besoin
                        feuille.getMinutesJouees(),
                        rencontre.getRid(),
                        feuille.getPasseurs().stream().map(Joueur::getJid).toList(),
                        feuille.getPasseurs().stream().map(Joueur::getNom).toList()
                ))
                .toList();
    }


    /**
     * ✅ Met à jour les statistiques du match (FeuilleDeMatch).
     */
    private void majStatsFeuilleDeMatch(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO) {
        feuillesProjections.forEach(feuilleProjection -> {
            // Convertir FeuilleDeMatchProjection en FeuilleDeMatchDTO
            FeuilleDeMatchDTO feuilleDTO = new FeuilleDeMatchDTO(feuilleProjection.getId(),
                    feuilleProjection.getRid(),
                    feuilleProjection.getJid(),
                    feuilleProjection.getNom(),
                    feuilleProjection.getPoste(),
                    feuilleProjection.getButs(),
                    feuilleProjection.getPasses(),
                    feuilleProjection.getMoyenneCote(),
                    feuilleProjection.getMinutesJouees(),
                    feuilleProjection.getAJoue(),
                    feuilleProjection.isTitulaire(),
                    feuilleProjection.getButArreter(),
                    feuilleProjection.getButEncaisser(),
                    parsePasseurs(feuilleProjection.getPasseursIds()),  // Assumer que parsePasseurs est une méthode pour convertir en List<Long>
                    parseNomsPasseurs(feuilleProjection.getNomsPasseurs())); // Pareil pour nomsPasseurs

            // Convertir la FeuilleDeMatchDTO en StatistiquesRencontreDTO
            StatistiquesRencontreDTO stats = StatistiquesRencontreDTO.fromFeuilleDeMatch(feuilleDTO);

            // Effectuer la mise à jour dans la base de données
            feuilleDeMatchRepository.majStatsFeuilleDeMatch(
                    feuilleDTO.getId(),
                    stats.getButs(),
                    stats.getPasses(),
                    clotureDTO.getCotes().getOrDefault(feuilleDTO.getJid(), 6.0),
                    stats.getMinutesJouees(),
                    Poste.fromString(feuilleDTO.getPoste()).isGardien(),
                    Poste.fromString(feuilleDTO.getPoste()).isGardien() ? clotureDTO.getButAdversaire() : 0,
                    Poste.fromString(feuilleDTO.getPoste()).isGardien() ? clotureDTO.getButsArretes().getOrDefault(feuilleDTO.getJid(), 0) : 0
            );
        });

    }



    private void majStatsChampionnat(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO, Rencontre rencontre) {
        // Convertir les projections en StatistiquesRencontreDTO
        List<StatistiquesRencontreDTO> statsRencontres = feuillesProjections.stream()
                .map(feuilleProjection -> StatistiquesRencontreDTO.fromFeuilleDeMatchProjection(feuilleProjection))
                .collect(Collectors.toList());

        // Utiliser le DTO StatistiquesChampionnatDTO pour la mise à jour des stats du championnat
        StatistiquesChampionnatDTO statsChampionnat = StatistiquesChampionnatDTO.fromChampionnat(statsRencontres, rencontre.getChampionnat().getIdChamp());

        // Mettre à jour les statistiques du championnat dans la base de données
        statChampionnatRepository.majStatsChampionnat(
                statsChampionnat.getJoueurId(),
                statsChampionnat.getChampionnatId(),
                statsChampionnat.getButsChamp(),
                statsChampionnat.getPassesChamp(),
                statsChampionnat.getMinutesJoueesChamp(),
                statsChampionnat.getCoteChamp(),
                statsChampionnat.getPointsChamp()
        );
    }





    private void majStatsJoueur(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO) {
        // Convertir les projections en FeuilleDeMatchDTO
        feuillesProjections.forEach(feuilleProjection -> {
            // Convertir chaque projection en DTO FeuilleDeMatchDTO
            FeuilleDeMatchDTO feuilleDeMatchDTO = new FeuilleDeMatchDTO(
                    feuilleProjection.getId(),
                    feuilleProjection.getRid(),
                    feuilleProjection.getJid(),
                    feuilleProjection.getNom(),
                    feuilleProjection.getPoste(),
                    feuilleProjection.getButs(),
                    feuilleProjection.getPasses(),
                    feuilleProjection.getMoyenneCote(),
                    feuilleProjection.getMinutesJouees(),
                    feuilleProjection.getAJoue(),
                    feuilleProjection.isTitulaire(),
                    feuilleProjection.getButArreter(),
                    feuilleProjection.getButEncaisser(),
                    parsePasseurs(feuilleProjection.getPasseursIds()),  // Conversion du champ String en Liste<Long>
                    parseNomsPasseurs(feuilleProjection.getNomsPasseurs()) // Conversion du champ String en Liste<String>
            );

            // Utiliser le DTO StatistiquesDTO pour la mise à jour des stats globales du joueur
            StatistiquesDTO statsJoueur = StatistiquesDTO.fromRencontres(Collections.singletonList(StatistiquesRencontreDTO.fromFeuilleDeMatch(feuilleDeMatchDTO)));

            // Mettre à jour les statistiques globales du joueur dans la base de données
            joueurRepository.majStatsJoueur(
                    statsJoueur.getJoueurId(),
                    statsJoueur.getButs(),
                    statsJoueur.getPasses(),
                    statsJoueur.getTotalMinutesJouees(),
                    statsJoueur.getCote()
            );
        });
    }








    private List<Long> parsePasseurs(String passeursIds) {
        if (passeursIds != null && !passeursIds.isEmpty()) {
            return Arrays.stream(passeursIds.split(","))
                    .map(Long::parseLong) // Convertit chaque ID en Long
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }


    private List<String> parseNomsPasseurs(String nomsPasseurs) {
        if (nomsPasseurs != null && !nomsPasseurs.isEmpty()) {
            return Arrays.asList(nomsPasseurs.split(",")); // Convertit directement en liste de String
        } else {
            return new ArrayList<>();
        }
    }



    private int calculerPointsRencontre(Rencontre rencontre) {
        if (rencontre.getScoreEquipeLocale() > rencontre.getButAdversaire()) {
            return 3; // ✅ Victoire → 3 points
        } else if (rencontre.getScoreEquipeLocale() == rencontre.getButAdversaire()) {
            return 1; // 🏳️ Match nul → 1 point
        } else {
            return 0; // ❌ Défaite → 0 point
        }
    }

    private RencontreDTO mapToDTO(Rencontre rencontre) {
        if (rencontre == null) {
            return null;
        }

        // Conversion des feuilles de match en DTOs
        List<FeuilleDeMatchDTO> feuillesDeMatchDTO = rencontre.getFeuilleDeMatchs().stream()
                .map(feuille -> {
                    // 1️⃣ Création explicite des listes
                    List<Long> passeursId = feuille.getPasseurs() != null
                            ? feuille.getPasseurs().stream().map(Joueur::getJid).collect(Collectors.toList())
                            : new ArrayList<>();

                    List<String> nomsPasseurs = feuille.getPasseurs() != null
                            ? feuille.getPasseurs().stream().map(Joueur::getNom).collect(Collectors.toList())
                            : new ArrayList<>();

                    return new FeuilleDeMatchDTO(
                            feuille.getFeuilleId(),
                            rencontre.getRid(),
                            feuille.getJoueurId(),
                            feuille.getJoueur().getNom(),
                            feuille.getJoueur().getPoste().name(),
                            feuille.getButs() != null ? feuille.getButs() : 0, // ✅ Cast en `int`
                            feuille.getPasses() != null ? feuille.getPasses() : 0, // ✅ Cast en `int`
                            feuille.getMoyenneCote() != null ? feuille.getMoyenneCote() : 5.0, // ✅ Cast en `double`
                            feuille.getMinutesJouees() != null ? feuille.getMinutesJouees() : 0.0, // ✅ Cast en `double`
                            feuille.getAJoue() != null ? feuille.getAJoue() : false, // ✅ Éviter `null`
                            feuille.isTitulaire(),
                            feuille.getButArreter(),
                            feuille.getButEncaisser(),
                            passeursId,  // ✅ Passer `List<Long>` directement
                            nomsPasseurs // ✅ Passer `List<String>` directement
                    );
                })
                .collect(Collectors.toList());

        return new RencontreDTO(
                rencontre.getRid(),
                rencontre.getChampionnat().getIdChamp(),
                rencontre.getScoreEquipeLocale(),
                rencontre.getNomAdversaire(),
                rencontre.getButAdversaire(),
                rencontre.getDivisionAdversaire(), // ✅ Correctement mappé
                rencontre.getNomHommeDuMatch(),
                rencontre.getStatutRencontre(),
                feuillesDeMatchDTO // ✅ Liste correcte des `FeuilleDeMatchDTO`
        );
    }



}
