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
import com.genesis.api.statmanager.projection.JoueurLightProjection;
import com.genesis.api.statmanager.projection.JoueurProjection;
import com.genesis.api.statmanager.repository.*;
import com.genesis.api.statmanager.service.ChampionnatService;
import com.genesis.api.statmanager.service.RencontreService;
import com.genesis.api.statmanager.service.StatistiqueManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;
import java.util.function.Function;
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
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Filtrer uniquement ceux qui ont joué
                .mapToInt(FeuilleDeMatch::getButs)
                .sum();

        List<StatistiquesRencontreDTO> statsJoueurs = rencontre.getFeuilleDeMatchs().stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Appliquer le même filtre
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
                            feuille.getJid(),
                            feuille.getJoueur().getNom(),
                            feuille.getJoueur().getClass().getSimpleName(),
                            feuille.getJoueur().getPoste().name(),
                            feuille.getButs(),
                            feuille.getButs() / Math.max(feuille.getMinutesJouees(), 1),
                            feuille.getPasses(),
                            feuille.getPasses() / Math.max(feuille.getMinutesJouees(), 1),
                            (feuille.getButs() + feuille.getPasses()) / 2.0,
                            feuille.getCote(),
                            (feuille.getButs() * 3) + feuille.getPasses(),
                            0,
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
                rencontre.getHommeDuMatch() != null ? rencontre.getHommeDuMatch().getJid() : null,
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

        Optional<Rencontre> derniereRencontreOpt = rencontreRepository.findDerniereRencontreJouee(idChampionnat);
        Long idDerniereRencontre = derniereRencontreOpt.map(Rencontre::getRid).orElse(null);

// 1️⃣ Récupération de tous les joueurs du championnat
        List<JoueurDTO> joueursChampionnat = statChampionnatRepository.findJoueursByChampionnatId(idChampionnat)
                .stream()
                .map(stat -> new JoueurDTO(stat.getId(), stat.getNom(), Poste.fromString(stat.getPoste())))
                .toList();

// 2️⃣ Récupération des titulaires du dernier match (si existant)
        List<JoueurDTO> joueursPrecedents = (idDerniereRencontre != null)
                ? feuilleDeMatchRepository.findByRencontreProjection(idDerniereRencontre).stream()
                .filter(FeuilleDeMatchProjection::isTitulaire)
                .map(feuille -> new JoueurDTO(
                        feuille.getJid(),
                        feuille.getNom(),
                        Poste.valueOf(feuille.getPoste()) // ✅ Conversion String → Enum
                ))
                .toList()
                : List.of();

        log.info("📥 PJoueur precedent ID={}", joueursPrecedents);

// 3️⃣ Filtrage des joueurs disponibles (ceux qui ne sont pas titulaires)
        List<JoueurDTO> joueursDisponibles = joueursChampionnat.stream()
                .filter(joueur -> joueursPrecedents.stream().noneMatch(titulaire -> titulaire.getId().equals(joueur.getId())))
                .toList();

        return new NouvelleRencontreSelectionDTO(
                idChampionnat,
                idDerniereRencontre,
                "",
                null,
                joueursPrecedents, // ✅ Liste des titulaires du dernier match
                joueursDisponibles // ✅ Liste des autres joueurs disponibles
        );
    }


    /**
     * Valide la sélection des joueurs avant le match.
     * Vérifie que le nombre de titulaires et remplaçants correspond aux exigences,
     * puis crée la rencontre.
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

        // 4️⃣ Déclaration de `joueurIds`
        List<Long> joueurIds = new ArrayList<>(titulairesMap.keySet());

        // 5️⃣ Récupération des joueurs sous forme de projection **sans instancier `Joueur`**
        List<JoueurProjection> joueurs = joueurRepository.findAllJoueurProjectionsByIds(joueurIds);
        Set<Long> joueursExistants = joueurs.stream().map(JoueurProjection::getJid).collect(Collectors.toSet());

        // 6️⃣ Vérification que tous les joueurs existent
        if (joueursExistants.size() != joueurIds.size()) {
            throw new IllegalStateException("❌ Certains joueurs sont introuvables dans la base de données.");
        }

        // 7️⃣ Création des feuilles de match **sans instancier `Joueur`**
        for (Long joueurId : joueurIds) {
            FeuilleDeMatch feuille = new FeuilleDeMatch();
            feuille.setRencontre(rencontre);
            feuille.setJid(joueurId);  // ✅ On stocke uniquement `joueurId`, pas l'entité `Joueur`
            feuille.setTitulaire(titulairesMap.getOrDefault(joueurId, false));
            feuille.setMinutesJouees(feuille.isTitulaire() ? TimePlay.MINUTES90.getPercentage() : TimePlay.MINUTES0.getPercentage());

            log.info("👤 Joueur ID={} - Titulaire: {} - Minutes attribuées: {} -  cote : {}",
                    joueurId, feuille.isTitulaire(), feuille.getMinutesJouees(), feuille.getCote());

            rencontre.addFeuilleDeMatch(feuille);
        }

        // 8️⃣ Sauvegarde en base
        rencontreRepository.saveAndFlush(rencontre);
        feuilleDeMatchRepository.flush();  // ✅ Force l'enregistrement immédiat

        log.info("✅ Rencontre ID={} créée avec {} joueurs", rencontre.getRid(), joueurIds.size());

        // 9️⃣ Initialisation du terrain après création de la rencontre
        return initialiserTerrain(rencontre.getRid(), getTerrain(rencontre.getRid()));
    }




    // -------------------------------------------------------------------------
    // 📌 3️⃣ GESTION DU TERRAIN ET MATCH EN COURS
    // -------------------------------------------------------------------------


    /**
     * ✅ Prépare un terrain modifiable AVANT validation.
     * Permet à l'utilisateur de placer les joueurs comme il le souhaite.
     */
    @Transactional
    public TerrainDTO construireTerrain(Long idRencontre) {
        log.info("📥 Préparation du terrain modifiable pour la rencontre ID={}", idRencontre);

        // 1️⃣ Vérification et récupération de la rencontre
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre non trouvée"));

        // 2️⃣ Récupération des feuilles de match associées
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre);
        if (projections.isEmpty()) {
            throw new IllegalStateException("❌ Aucune feuille de match trouvée pour la rencontre ID=" + idRencontre);
        }

        // 3️⃣ Transformation en DTO modifiable
        TerrainDTO terrainDTO = transformerEnTerrainDTO(rencontre, projections);

        // 4️⃣ Initialisation des joueurs **sans validation**
        Map<String, FeuilleDeMatchDTO> terrainJoueurs = new LinkedHashMap<>();
        List<String> postes = List.of("GB", "DC_GAUCHE", "DC_DROIT", "DC_CENTRAL", "DG", "DD", "MDF", "MO", "MR", "AIG", "AID", "AC", "SA", "AC_DROIT", "AC_GAUCHE");


        for (String poste : postes) {
            terrainJoueurs.put(poste, null);
        }

        for (FeuilleDeMatchDTO joueur : terrainDTO.getTitulaires()) {
            if (!terrainJoueurs.containsKey(joueur.getPoste()) || terrainJoueurs.get(joueur.getPoste()) == null) {
                terrainJoueurs.put(joueur.getPoste(), joueur);
                log.info("✅ Placement direct : {} → {}", joueur.getNom(), joueur.getPoste());
            } else {
                // 🔄 Vérifier les postes compatibles et trouver une place libre
                List<String> postesCompatibles = getPostesCompatibles(joueur.getPoste());
                for (String posteCompatible : postesCompatibles) {
                    if (terrainJoueurs.get(posteCompatible) == null) {
                        terrainJoueurs.put(posteCompatible, joueur);
                        log.info("⚡ Placement alternatif : {} → {}", joueur.getNom(), posteCompatible);
                        break;
                    }
                }
            }
        }


        // ✅ Retourne le terrain pour modification avant validation
        return TerrainDTO.builder()
                .idRencontre(terrainDTO.getIdRencontre())
                .idChampionnat(terrainDTO.getIdChampionnat())
                .nomAdversaire(terrainDTO.getNomAdversaire())
                .divisionAdversaire(terrainDTO.getDivisionAdversaire())
                .titulaires(terrainDTO.getTitulaires())
                .remplacants(terrainDTO.getRemplacants())
                .terrainJoueurs(terrainJoueurs) // Terrain modifiable
                .build();
    }


    /**
     * ✅ Valide le terrain après l'ajustement des positions avec logs détaillés.
     */
    @Transactional
    public TerrainDTO initialiserTerrain(Long idRencontre, TerrainDTO terrainDTO) {
        log.info("✅ Début - Initialisation du terrain pour la rencontre ID={}", idRencontre);
        // 📌 AFFICHAGE DU TERRAIN REÇU
        log.info("📥 Terrain reçu avant modification : {}", terrainDTO);
        // 1️⃣ Vérification de la rencontre
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> {
                    log.error("❌ Rencontre ID={} introuvable !", idRencontre);
                    return new IllegalArgumentException("❌ Rencontre non trouvée");
                });

        log.info("✅ Rencontre ID={} trouvée. Vérification des joueurs...", idRencontre);

        // 2️⃣ Charger l'état du terrain si nécessaire
        if (terrainDTO == null || terrainDTO.getTitulaires() == null || terrainDTO.getRemplacants() == null) {
            log.warn("⚠️ TerrainDTO vide. Récupération du terrain depuis la base...");
            terrainDTO = getTerrain(idRencontre);
        }

        // 3️⃣ Vérifier la validité des listes
        List<FeuilleDeMatchDTO> titulaires = Optional.ofNullable(terrainDTO.getTitulaires()).orElse(new ArrayList<>());
        List<FeuilleDeMatchDTO> remplacants = Optional.ofNullable(terrainDTO.getRemplacants()).orElse(new ArrayList<>());

        log.info("📌 Joueurs récupérés : {} titulaires et {} remplaçants.", titulaires.size(), remplacants.size());

        if (titulaires.size() != 11) {
            log.error("❌ Il faut exactement 11 titulaires, mais {} détectés !", titulaires.size());
            throw new IllegalArgumentException("❌ Il faut exactement 11 titulaires !");
        }
        if (remplacants.size() != 12) {
            log.error("❌ Il faut exactement 12 remplaçants, mais {} détectés !", remplacants.size());
            throw new IllegalArgumentException("❌ Il faut exactement 12 remplaçants !");
        }

        // 4️⃣ Mise à jour des feuilles de match (seulement `titulaire` et `minutesJouees`)
        for (FeuilleDeMatchDTO joueurDTO : titulaires) {
            FeuilleDeMatch feuille = feuilleDeMatchRepository.findByRencontreAndJoueurId(idRencontre, joueurDTO.getJid())
                    .stream().findFirst()
                    .orElseThrow(() -> {
                        log.error("❌ Feuille de match introuvable pour TITULAIRE {} (ID={}) !", joueurDTO.getNom(), joueurDTO.getJid());
                        return new IllegalStateException("❌ Feuille de match introuvable pour " + joueurDTO.getNom());
                    });

            log.info("🟢 TITULAIRE détecté : {} (ID={}) | Ancien statut: Titulaire={} | Minutes={}",
                    joueurDTO.getNom(), joueurDTO.getJid(), feuille.isTitulaire(), feuille.getMinutesJouees());

            feuille.setTitulaire(true);
            feuille.setMinutesJouees(TimePlay.MINUTES90.getPercentage()); // ✅ Minutes mises à jour
            feuilleDeMatchRepository.save(feuille);

            log.info("✅ Mise à jour OK - TITULAIRE {} (ID={}) | Nouveau statut: Titulaire=true | Minutes={}",
                    joueurDTO.getNom(), joueurDTO.getJid(), feuille.getMinutesJouees());
        }

        for (FeuilleDeMatchDTO joueurDTO : remplacants) {
            FeuilleDeMatch feuille = feuilleDeMatchRepository.findByRencontreAndJoueurId(idRencontre, joueurDTO.getJid())
                    .stream().findFirst()
                    .orElseThrow(() -> {
                        log.error("❌ Feuille de match introuvable pour REMPLAÇANT {} (ID={}) !", joueurDTO.getNom(), joueurDTO.getJid());
                        return new IllegalStateException("❌ Feuille de match introuvable pour " + joueurDTO.getNom());
                    });

            log.info("🟡 REMPLAÇANT détecté : {} (ID={}) | Ancien statut: Titulaire={} | Minutes={}",
                    joueurDTO.getNom(), joueurDTO.getJid(), feuille.isTitulaire(), feuille.getMinutesJouees());

            feuille.setTitulaire(false);
            feuille.setMinutesJouees(TimePlay.MINUTES0.getPercentage()); // ✅ Minutes mises à jour
            feuilleDeMatchRepository.save(feuille);

            log.info("✅ Mise à jour OK - REMPLAÇANT {} (ID={}) | Nouveau statut: Titulaire=false | Minutes={}",
                    joueurDTO.getNom(), joueurDTO.getJid(), feuille.getMinutesJouees());
        }

        feuilleDeMatchRepository.flush(); // ✅ Forcer l'enregistrement immédiat
        log.info("✅ Enregistrement en base complété.");

        // 5️⃣ Retourner `getTerrain()` pour que `transformerEnTerrainDTO` gère les autres transformations
        TerrainDTO terrainFinal = getTerrain(idRencontre);
        log.info("📤 Fin - Terrain initialisé pour la rencontre ID={}", idRencontre);

        return terrainFinal;
    }


    /**
     * ✅ Récupère l'état actuel du terrain.
     */
    @Transactional
    public TerrainDTO getTerrain(Long idRencontre) {
        log.info("📥 Récupération de l'état du terrain pour la rencontre ID={}", idRencontre);

        // 1️⃣ Récupération de la rencontre et chargement des feuilles de match AVANT de quitter la transaction
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre non trouvée"));


        // ⚠️ Chargement explicite des feuilles de match AVANT la fermeture de la session Hibernate
        rencontre.getFeuilleDeMatchs().size(); // ✅ Force Hibernate à initialiser la collection

        // 2️⃣ Rafraîchissement des données pour éviter le cache Hibernate
        feuilleDeMatchRepository.flush();
        entityManager.clear();

        // 3️⃣ Récupération des projections de feuilles de match
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre);
        if (projections.isEmpty()) {
            throw new IllegalStateException("❌ Aucune feuille de match trouvée pour la rencontre ID=" + idRencontre);
        }
        // 🔹 Ajout d'un log pour voir les valeurs des cotes avant transformation
        projections.forEach(feuille -> log.info("📌 [DEBUG] Projection Feuille ID={} | JID={} | Nom={} | Cote={}",
                feuille.getId(), feuille.getJid(), feuille.getNom(), feuille.getCote()));
        // 4️⃣ Transformation en `TerrainDTO`
        return transformerEnTerrainDTO(rencontre, projections);
    }


    @Transactional
    public TerrainDTO updateStatsEnTempsReel(Long idRencontre, Long idJoueur, int buts, Long idPasseur) {
        log.info("📢 Mise à jour des stats en temps réel - Rencontre ID={} | Joueur ID={} | Buts={} | Passeur={}",
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

        // 🔥 Récupérer les passes existantes AVANT de modifier le joueur
        Integer passesActuelles = feuilleDeMatchRepository.findPassesById(feuilleButeurId).orElse(0);

        // ✅ Mise à jour du joueur sans écraser ses passes
        updateButsTransactionnel(feuilleButeurId, buts, passesActuelles);

        // 3️⃣ Mise à jour des passes si un passeur est fourni
        Long feuillePasseurId = null;
        if (idPasseur != null && buts > 0) {
            Optional<Long> feuillePasseurIdOpt = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre).stream()
                    .filter(f -> f.getJid().equals(idPasseur))
                    .map(FeuilleDeMatchProjection::getId)
                    .findFirst();

            if (feuillePasseurIdOpt.isPresent()) {
                feuillePasseurId = feuillePasseurIdOpt.get();
                updatePassesTransactionnel(feuillePasseurId, 1);
                feuilleDeMatchRepository.addPasseurByIds(feuilleButeurId, idPasseur);
            }
        }

        // ✅ 4️⃣ Récupération des feuilles de match mises à jour
        List<FeuilleDeMatchProjection> projections = feuilleDeMatchRepository.findFeuillesDeMatchAsProjection(idRencontre);
        List<FeuilleDeMatchDTO> feuillesDeMatchDTO = projections.stream()
                .map(feuille -> new FeuilleDeMatchDTO(
                        feuille.getId(),
                        feuille.getRid(),
                        feuille.getJid(),
                        feuille.getNom(),
                        feuille.getPoste(),
                        feuille.getButs(),
                        feuille.getPasses(),
                        feuille.getCote(),
                        feuille.getMinutesJouees(),
                        feuille.isAjoue(),
                        feuille.isTitulaire(),
                        feuille.getButArreter(),
                        feuille.getButEncaisser(),
                        (feuille.getPasseursIds() != null && !feuille.getPasseursIds().isEmpty()) ?
                                Arrays.stream(feuille.getPasseursIds().split(",")).map(Long::parseLong).toList() : new ArrayList<>(),
                        (feuille.getNomsPasseurs() != null && !feuille.getNomsPasseurs().isEmpty()) ?
                                Arrays.asList(feuille.getNomsPasseurs().split(",")) : new ArrayList<>()
                ))
                .toList();

        // ✅ 5️⃣ Récupération complète du terrain et injection des nouvelles données
        TerrainDTO terrain = getTerrain(idRencontre);

        // ✅ 6️⃣ Ajout de l'historique des événements
        List<EvenementMatchDTO> historique = getHistoriqueEvenements(idRencontre);
        log.info("📌 Historique après mise à jour des stats en temps réel : {}", historique);

        // ✅ 7️⃣ Envoi des mises à jour WebSocket en temps réel
        messagingTemplate.convertAndSend("/topic/terrain/" + idRencontre, terrain);
        log.info("📡 WebSocket : Stats en temps réel mises à jour pour la rencontre ID={}", idRencontre);

        return terrain;
    }





    @Transactional
    public TerrainDTO effectuerRemplacement(RemplacementDTO remplacementDTO) {
        log.info("🔄 Remplacement - Rencontre ID={} | Sortant={} | Entrant={} | Minute={}",
                remplacementDTO.getIdRencontre(), remplacementDTO.getIdTitulaireSortant(),
                remplacementDTO.getIdRemplacantEntrant(), remplacementDTO.getMinuteEntree());

        // 1️⃣ Récupération des joueurs concernés directement via leur ID
        FeuilleDeMatch feuilleSortant = feuilleDeMatchRepository.findByRencontreAndJoueurId(
                        remplacementDTO.getIdRencontre(), remplacementDTO.getIdTitulaireSortant())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ Titulaire sortant introuvable"));

        FeuilleDeMatch feuilleRemplacant = feuilleDeMatchRepository.findByRencontreAndJoueurId(
                        remplacementDTO.getIdRencontre(), remplacementDTO.getIdRemplacantEntrant())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ Remplaçant introuvable"));

        // 2️⃣ Mise à jour des minutes jouées
        TimePlay timePlaySortant = remplacementDTO.getMinuteEntree();
        TimePlay timePlayEntrant = TimePlay.fromPercentage(1.0 - timePlaySortant.getPercentage());

        feuilleSortant.setMinutesJouees(timePlaySortant.getPercentage());
        feuilleRemplacant.setMinutesJouees(timePlayEntrant.getPercentage());

        // ✅ Mettre `aJoue = true` pour le remplaçant
        feuilleRemplacant.setAjoue(true);

        feuilleDeMatchRepository.saveAll(List.of(feuilleSortant, feuilleRemplacant));
        feuilleDeMatchRepository.flush();
        entityManager.clear();

        // ✅ 3️⃣ Maintenant, récupérer l'état mis à jour du terrain
        TerrainDTO terrainMisAJour = getTerrain(remplacementDTO.getIdRencontre());

        // ✅ 4️⃣ 🔥 Envoyer la mise à jour WebSocket **APRES** avoir le bon terrain
        messagingTemplate.convertAndSend("/topic/terrain/" + remplacementDTO.getIdRencontre(), terrainMisAJour);

        log.info("📡 WebSocket : Mise à jour envoyée pour la rencontre ID={}", remplacementDTO.getIdRencontre());

        return terrainMisAJour;
    }


    @Transactional
    public void mettreAJourButAdversaire(Long idRencontre, int butAdversaire) {
        Rencontre rencontre = rencontreRepository.findById(idRencontre)
                .orElseThrow(() -> new EntityNotFoundException("Rencontre non trouvée"));

        rencontre.setButAdversaire(butAdversaire);
        rencontreRepository.save(rencontre);

        // ✅ 1️⃣ Récupérer l'état mis à jour du terrain
        TerrainDTO terrainMisAJour = getTerrain(idRencontre);

        // ✅ 2️⃣ Envoyer la mise à jour en temps réel via WebSocket
        messagingTemplate.convertAndSend("/topic/terrain/" + idRencontre, terrainMisAJour);

        log.info("📡 WebSocket : Score adverse mis à jour pour la rencontre ID={}", idRencontre);
    }


    @Transactional
    public void mettreAJourStatsGardien(Long idRencontre, Long idJoueur, int butArreter) {
        Long feuilleId = feuilleDeMatchRepository.findFeuilleIdByRencontreAndJoueur(idRencontre, idJoueur)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Feuille de match non trouvée pour rencontre ID " + idRencontre + " et joueur ID " + idJoueur
                ));

        // 🔥 Récupération du vrai `butEncaisser` depuis `rencontre`
        int butEncaisser = rencontreRepository.findById(idRencontre)
                .map(Rencontre::getButAdversaire)
                .orElseThrow(() -> new EntityNotFoundException("Rencontre non trouvée pour ID " + idRencontre));

        // ✅ Mise à jour en base
        int updatedRows = feuilleDeMatchRepository.updateFeuilleGardienStats(feuilleId, butEncaisser, butArreter);

        if (updatedRows == 0) {
            throw new IllegalStateException("❌ La mise à jour des stats du gardien a échoué pour Feuille ID : " + feuilleId);
        }

        log.info("✅ Stats du gardien mises à jour : Feuille ID={} | Buts encaissés={} | Arrêts={}", feuilleId, butEncaisser, butArreter);

        // ✅ 1️⃣ Récupérer l'état mis à jour du terrain
        TerrainDTO terrainMisAJour = getTerrain(idRencontre);

        // ✅ 2️⃣ Envoyer la mise à jour en temps réel via WebSocket
        messagingTemplate.convertAndSend("/topic/terrain/" + idRencontre, terrainMisAJour);

        log.info("📡 WebSocket : Stats du gardien mises à jour pour la rencontre ID={}", idRencontre);
    }


    private TerrainDTO transformerEnTerrainDTO(Rencontre rencontre, List<FeuilleDeMatchProjection> projections) {
        log.info("📌 [DEBUG] Début transformation en TerrainDTO pour la rencontre ID={}", rencontre.getRid());

        // 1️⃣ Transformation des projections en DTOs
        List<FeuilleDeMatchDTO> feuillesDeMatchDTO = projections.stream()
                .map(feuille -> new FeuilleDeMatchDTO(
                        feuille.getId(),
                        feuille.getRid(),
                        feuille.getJid(),
                        feuille.getNom(),
                        String.valueOf(feuille.getPoste()),
                        feuille.getButs(),
                        feuille.getPasses(),
                        feuille.getCote(),
                        feuille.getMinutesJouees(),
                        feuille.isAjoue(),
                        feuille.isTitulaire(),
                        feuille.getButArreter(),
                        feuille.getButEncaisser(),
                        (feuille.getPasseursIds() != null && !feuille.getPasseursIds().isEmpty())
                                ? Arrays.stream(feuille.getPasseursIds().split(","))
                                .map(String::trim)
                                .filter(id -> !id.isEmpty())
                                .map(Long::parseLong)
                                .collect(Collectors.toList())
                                : new ArrayList<>(),
                        (feuille.getNomsPasseurs() != null && !feuille.getNomsPasseurs().isEmpty())
                                ? Arrays.stream(feuille.getNomsPasseurs().split(","))
                                .map(String::trim)
                                .filter(nom -> !nom.isEmpty())
                                .collect(Collectors.toList())
                                : new ArrayList<>()
                ))
                .toList();

        log.info("📌 [DEBUG] Feuilles de match récupérées ({}) :", feuillesDeMatchDTO.size());
        feuillesDeMatchDTO.forEach(joueur -> log.info(
                "📌 JID={} | Nom={} | Poste={} | Titulaire={} | Minutes={} | aJoue={}",
                joueur.getJid(), joueur.getNom(), joueur.getPoste(), joueur.isTitulaire(),
                joueur.getMinutesJouees(), joueur.isAjoue()
        ));

        // 2️⃣ Séparer les titulaires et remplaçants
        List<FeuilleDeMatchDTO> titulaires = feuillesDeMatchDTO.stream()
                .filter(FeuilleDeMatchDTO::isTitulaire)
                .toList();

        List<FeuilleDeMatchDTO> remplacants = feuillesDeMatchDTO.stream()
                .filter(joueur -> !joueur.isTitulaire())
                .toList();

        log.info("📌 [DEBUG] Titulaires identifiés : {}", titulaires.size());
        log.info("📌 [DEBUG] Remplaçants identifiés : {}", remplacants.size());

        // 3️⃣ Construire `terrainJoueurs`
        Map<String, FeuilleDeMatchDTO> terrainJoueurs = new LinkedHashMap<>();

        // 3.1️⃣ Ajouter les titulaires qui ont joué tout le match
        List<FeuilleDeMatchDTO> titulairesConflits = new ArrayList<>();

        titulaires.stream()
                .filter(joueur -> joueur.getMinutesJouees() == 1.0)
                .forEach(joueur -> {
                    if (!terrainJoueurs.containsKey(joueur.getPoste())) {
                        terrainJoueurs.put(joueur.getPoste(), joueur);
                        log.info("📌 [DEBUG] Ajout TITULAIRE {} en {} (minutes={})",
                                joueur.getNom(), joueur.getPoste(), joueur.getMinutesJouees());
                    } else {
                        // 🔄 Poste déjà occupé, tenter un poste compatible
                        List<String> postesCompatibles = getPostesCompatibles(joueur.getPoste());
                        boolean ajouté = false;

                        for (String posteCompatible : postesCompatibles) {
                            if (!terrainJoueurs.containsKey(posteCompatible)) {
                                terrainJoueurs.put(posteCompatible, joueur);
                                log.info("📌 [DEBUG] TITULAIRE {} placé en {} (poste compatible)",
                                        joueur.getNom(), posteCompatible);
                                ajouté = true;
                                break;
                            }
                        }

                        if (!ajouté) {
                            log.warn("⚠️ Conflit de poste : TITULAIRE {} (poste {} déjà pris)",
                                    joueur.getNom(), joueur.getPoste());
                            titulairesConflits.add(joueur);
                        }
                    }
                });

// (Optionnel) ➜ Gérer les titulaires restants en conflit ici, si besoin


        // 3.2️⃣ Ajouter les remplaçants qui ont joué
        remplacants.stream()
                .filter(joueur -> Boolean.TRUE.equals(joueur.isAjoue()))
                .forEach(joueur -> {
                    if (!terrainJoueurs.containsKey(joueur.getPoste())) {
                        terrainJoueurs.put(joueur.getPoste(), joueur);
                        log.info("📌 [DEBUG] Ajout REMPLAÇANT {} en {} (minutes={})",
                                joueur.getNom(), joueur.getPoste(), joueur.getMinutesJouees());
                    } else {
                        // 🔄 Gestion des postes compatibles
                        List<String> postesCompatibles = getPostesCompatibles(joueur.getPoste());
                        boolean ajouté = false;
                        for (String posteCompatible : postesCompatibles) {
                            if (!terrainJoueurs.containsKey(posteCompatible)) {
                                terrainJoueurs.put(posteCompatible, joueur);
                                log.info("📌 [DEBUG] Ajout REMPLAÇANT {} en {} (posté compatible)",
                                        joueur.getNom(), posteCompatible);
                                ajouté = true;
                                break;
                            }
                        }
                        if (!ajouté) {
                            log.warn("⚠️ Impossible d'ajouter le remplaçant {} à un poste libre !", joueur.getNom());
                        }
                    }
                });

        // 4️⃣ Vérification finale des joueurs sur le terrain
        log.info("📌 [BACKEND] Liste des joueurs réellement sur le terrain après transformation :");
        terrainJoueurs.forEach((poste, joueur) -> log.info(
                "📌 Poste={} | Joueur={} | Minutes={}", poste, joueur.getNom(), joueur.getMinutesJouees()
        ));

        // 5️⃣ Création du DTO final
        return TerrainDTO.builder()
                .idRencontre(rencontre.getRid())
                .idChampionnat(rencontre.getChampionnat().getIdChamp())
                .nomEquipe("HERSTAL FC")
                .nomAdversaire(rencontre.getNomAdversaire())
                .divisionAdversaire(rencontre.getDivisionAdversaire())
                .butEquipe(rencontre.getFeuilleDeMatchs().stream().mapToInt(FeuilleDeMatch::getButs).sum())
                .butAdversaire(rencontre.getButAdversaire())
                .titulaires(titulaires)
                .remplacants(remplacants)
                .terrainJoueurs(terrainJoueurs)
                .butsModifies(feuillesDeMatchDTO.stream()
                        .collect(Collectors.toMap(FeuilleDeMatchDTO::getJid, FeuilleDeMatchDTO::getButs)))
                .passesModifies(feuillesDeMatchDTO.stream()
                        .collect(Collectors.toMap(FeuilleDeMatchDTO::getJid, FeuilleDeMatchDTO::getPasses)))
                .minutesJouees(feuillesDeMatchDTO.stream()
                        .collect(Collectors.toMap(FeuilleDeMatchDTO::getJid, FeuilleDeMatchDTO::getMinutesJouees)))
                .build();
    }


    // -------------------------------------------------------------------------
    // 📌 4️⃣ FIN DE MATCH ET MISE À JOUR DES STATS
    // -------------------------------------------------------------------------

    @Transactional
    public FinMatchDTO cloturerRencontre(ClotureRencontreDTO clotureDTO) {
        log.info("📌 [Début] Clôture de la rencontre ID={}", clotureDTO.getIdRencontre());
        log.info("📌 Cotes reçues debut : {}", clotureDTO.getCotes());

        // 1️⃣ Récupération de la rencontre
        Rencontre rencontre = rencontreRepository.findById(clotureDTO.getIdRencontre())
                .orElseThrow(() -> new IllegalArgumentException("❌ Rencontre introuvable"));
        Championnat championnat = rencontre.getChampionnat();
        Long idChampionnat = championnat.getIdChamp();
        log.info("📌 Rencontre ID={} | Championnat ID={} | Division={} | Statut={}",
                rencontre.getRid(), idChampionnat, championnat.getDivision(), championnat.getStatut());

        // 2️⃣ Récupération et filtrage des feuilles de match (garder seulement les joueurs ayant joué)
        List<FeuilleDeMatchProjection> feuillesProjections = feuilleDeMatchRepository.findAllByRencontreId(clotureDTO.getIdRencontre())
                .stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Filtrage
                .toList();

        log.info("📌 Nombre de feuilles de match filtrées (joueurs ayant joué) : {}", feuillesProjections.size());
        log.info("📌 Cotes reçues juste avant maj : {}", clotureDTO.getCotes());

        // 3️⃣ Mise à jour des statistiques (uniquement pour les joueurs ayant joué)
        majStatsFeuilleDeMatch(feuillesProjections, clotureDTO);
        // ✅ Forcer la mise à jour en base
        feuilleDeMatchRepository.flush();
        entityManager.clear();
        // 🔄 Récupération des feuilles après mise à jour
        List<FeuilleDeMatchProjection> feuillesProjectionsApresMaj = feuilleDeMatchRepository.findAllByRencontreId(clotureDTO.getIdRencontre());


        majStatsChampionnat(feuillesProjectionsApresMaj, clotureDTO, rencontre);
        feuilleDeMatchRepository.flush();
        entityManager.clear();
        // 🔄 Récupération des feuilles après mise à jour
        List<FeuilleDeMatchProjection> feuillesProjectionsApresMaj2 = feuilleDeMatchRepository.findAllByRencontreId(clotureDTO.getIdRencontre());

        majStatsJoueur(feuillesProjectionsApresMaj2, clotureDTO);
        log.info("✅ Statistiques mises à jour.");

        // 4️⃣ Transformation des feuilles filtrées en DTO
        List<StatistiquesRencontreDTO> statsRencontres = feuillesProjections.stream()
                .map(StatistiquesRencontreDTO::fromFeuilleDeMatchProjection)
                .toList();

        // 5️⃣ Détermination de l’Homme du Match (parmi ceux qui ont joué)
        Long hommeDuMatchId = StatistiquesRencontreDTO.determinerHommeDuMatch(statsRencontres);
        if (hommeDuMatchId != null) {
            log.info("🔍 Mise à jour de l'Homme du Match en base | Rencontre ID={} | Joueur ID={}", rencontre.getRid(), hommeDuMatchId);

            rencontreRepository.updateHommeDuMatch(rencontre.getRid(), hommeDuMatchId);
            rencontreRepository.flush(); // ✅ Force Hibernate à exécuter immédiatement l'update SQL

            // ✅ Recharger `rencontre` pour s'assurer qu'elle est bien gérée
            rencontre = entityManager.merge(rencontre);

            // ✅ Maintenant `rencontre` est bien gérée, on peut appeler `refresh()`
            entityManager.refresh(rencontre);


        } else {
            log.warn("⚠️ Aucun Homme du Match trouvé.");
        }



        log.info("✅ HOM attribué : Joueur ID={}", rencontre.getHommeDuMatch());


        // 6️⃣ Mise à jour des points de la rencontre
        int pointsEquipe = calculerPointsRencontre(rencontre);
        championnat.setPointsActuels(championnat.getPointsActuels() + pointsEquipe);
        championnat.updateStatut();
        log.info("📌 Points mis à jour : {} | Total Championnat : {}", pointsEquipe, championnat.getPointsActuels());

        // 7️⃣ Mise à jour du statut et du score de la rencontre
        rencontre.setButEquipe(rencontre.getScoreEquipeLocale());
        rencontre.setStatutRencontre(StatutRencontre.TERMINE);
        rencontreRepository.save(rencontre);
        championnatRepository.save(championnat);
        log.info("✅ Rencontre et Championnat mis à jour en base.");

// ✅ FORCER LA SYNCHRONISATION EN BASE AVANT LA VÉRIFICATION DU STATUT DU CHAMPIONNAT
        rencontreRepository.flush();
        championnatRepository.flush();
        entityManager.clear();

        log.info("✅ Données rafraîchies avant la vérification du statut du championnat.");

// 8️⃣ Vérification du statut final du championnat
        championnatService.verifierStatutChampionnat(championnat);

// ✅ Vérifier que TOUS les matchs sont bien joués avant de clôturer
        long matchsJoues = rencontreRepository.countRencontresTerminees(idChampionnat);
        if (matchsJoues >= MAX_RENCONTRES) {
            if (championnat.getStatut() == Statut.PROMOTION || championnat.getStatut() == Statut.RELEGATION || championnat.getStatut() == Statut.MAINTIEN) {
                log.info("🏆 Fin du championnat ID={} après {} matchs terminés", idChampionnat, matchsJoues);
                championnatService.cloturerChampionnat(idChampionnat);
            }
        } else {
            log.info("⏳ Championnat encore en cours. Matchs restants à jouer : {}", MAX_RENCONTRES - matchsJoues);
        }



        // 9️⃣ Récupération de l’historique des événements
        List<EvenementMatchDTO> evenementsMatch = getHistoriqueEvenements(rencontre.getRid());
        log.info("✅ Nombre d’événements récupérés : {}", evenementsMatch.size());

        // 🔟 Retour du DTO FinMatchDTO
        return new FinMatchDTO(
                rencontre.getRid(),
                rencontre.getNomEquipe(),
                rencontre.getButEquipe(),
                rencontre.getNomAdversaire(),
                rencontre.getButAdversaire(),
                hommeDuMatchId != null
                        ? joueurRepository.findJoueurProjectionById(hommeDuMatchId)
                        .map(JoueurProjection::getNom)
                        .orElse("Inconnu")
                        : "Inconnu",
                statsRencontres,
                evenementsMatch
        );

    }


    /**
     * 📌 Met à jour les cotes des joueurs après attribution en fin de match.
     */
    @Transactional
    @Override
    public void mettreAJourCotes(Long idRencontre, Map<Long, Double> cotesJoueurs) {
        cotesJoueurs.forEach((joueurId, nouvelleCote) -> {
            feuilleDeMatchRepository.mettreAJourCoteJoueur(idRencontre, joueurId, nouvelleCote);
            log.info("📌 Cote mise à jour en base pour Joueur ID={} : {}", joueurId, nouvelleCote);
        });
    }


    /**
     * ✅ Met à jour les statistiques du match (FeuilleDeMatch).
     */
    private void majStatsFeuilleDeMatch(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO) {
        log.info("📌 [Début] Mise à jour des statistiques des feuilles de match. Nombre de projections : {}", feuillesProjections.size());
        log.info("📌 Cotes reçues pour mise à jour : {}", clotureDTO.getCotes());

        feuillesProjections.forEach(feuilleProjection -> {
            log.info("📌 Feuille de match traitée - ID={} | JID={} | Nom={} | aJoue={} | Titulaire={} | Poste={}",
                    feuilleProjection.getId(), feuilleProjection.getJid(), feuilleProjection.getNom(),
                    feuilleProjection.isAjoue(), feuilleProjection.isTitulaire(), feuilleProjection.getPoste());

            // Conversion de la projection en DTO
            FeuilleDeMatchDTO feuilleDTO = new FeuilleDeMatchDTO(
                    feuilleProjection.getId(),
                    feuilleProjection.getRid(),
                    feuilleProjection.getJid(),
                    feuilleProjection.getNom(),
                    feuilleProjection.getPoste(),
                    feuilleProjection.getButs(),
                    feuilleProjection.getPasses(),
                    feuilleProjection.getCote(),  // Valeur initiale, sera écrasée par clotureDTO.getCotes()
                    feuilleProjection.getMinutesJouees(),
                    feuilleProjection.isAjoue(),
                    feuilleProjection.isTitulaire(),
                    feuilleProjection.getButArreter(),
                    feuilleProjection.getButEncaisser(),
                    parsePasseurs(feuilleProjection.getPasseursIds()),
                    parseNomsPasseurs(feuilleProjection.getNomsPasseurs())
            );

            // Conversion vers StatistiquesRencontreDTO
            StatistiquesRencontreDTO stats = StatistiquesRencontreDTO.fromFeuilleDeMatch(feuilleDTO);

            // ✅ Vérification spécifique pour les gardiens
            boolean estGardien = Poste.fromString(feuilleDTO.getPoste()).isGardien();
            int butEncaisserGardien = estGardien ? clotureDTO.getButAdversaire() : 0;
            int butArreterGardien = estGardien ? clotureDTO.getButsArretes().getOrDefault(feuilleDTO.getJid(), 0) : 0;



            // ✅ Mise à jour en base avec la cote correctement attribuée
            feuilleDeMatchRepository.majStatsFeuilleDeMatch(
                    feuilleDTO.getFeuilleId(),  // Correction ici : Utilisation de getId()
                    stats.getButs(),
                    stats.getPasses(),
                    stats.getCote(),  // Cote bien prise en compte
                    stats.getMinutesJouees(),
                    estGardien,
                    butEncaisserGardien,
                    butArreterGardien
            );


        });

        log.info("✅ [Fin] Mise à jour des feuilles de match complétée.");
    }







    private void majStatsChampionnat(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO, Rencontre rencontre) {
        log.info("📌 [Début] Mise à jour des statistiques du championnat pour le championnat ID={}", rencontre.getChampionnat().getIdChamp());

        // 📌 Ne garder que les joueurs qui ont joué
        Map<Long, List<StatistiquesRencontreDTO>> statsParJoueur = feuillesProjections.stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Exclure ceux qui n'ont pas joué
                .map(StatistiquesRencontreDTO::fromFeuilleDeMatchProjection)
                .collect(Collectors.groupingBy(StatistiquesRencontreDTO::getJoueurId));

        Championnat championnat = rencontre.getChampionnat();
        Map<Long, StatChampionnat> statsMap = statChampionnatRepository.findByChampionnat(championnat)
                .stream().collect(Collectors.toMap(StatChampionnat::getJoueurId, stat -> stat));

        List<Long> joueursIds = statsParJoueur.keySet().stream().toList();
        Map<Long, JoueurProjection> joueursProjectionMap = joueurRepository.findAllJoueurProjectionsByIds(joueursIds)
                .stream().collect(Collectors.toMap(JoueurProjection::getJid, p -> p));

        for (Map.Entry<Long, List<StatistiquesRencontreDTO>> entry : statsParJoueur.entrySet()) {
            Long joueurId = entry.getKey();
            List<StatistiquesRencontreDTO> statsJoueur = entry.getValue();

            StatistiquesChampionnatDTO statsChampionnat = StatistiquesChampionnatDTO.fromChampionnat(statsJoueur, championnat.getIdChamp());

            // 📌 Récupération ou création de l'entrée StatChampionnat
            StatChampionnat statChampionnat = statsMap.getOrDefault(joueurId, new StatChampionnat(
                    null, joueurId, championnat, 0, 0, 0.0, 5.0, 0, 0, 0 // Ajout des nouvelles valeurs
            ));

            // 📌 Logs des valeurs en base avant mise à jour
            log.info("🔍 [AVANT] Stats en base pour Joueur ID={} | Buts={} | Passes={} | Minutes={} | Cote={} | Buts encaissés={} | Arrêts={} | Clean Sheets={}",
                    joueurId, statChampionnat.getButsChamp(), statChampionnat.getPassesChamp(),
                    statChampionnat.getMinutesJoueesChamp(), statChampionnat.getMoyenneCoteChamp(),
                    statChampionnat.getButEncaisserChamp(), statChampionnat.getButArreterChamp(),
                    statChampionnat.getCleanSheet());

// 📌 Mise à jour des statistiques en conservant les anciennes valeurs
            statChampionnat.setButsChamp(statChampionnat.getButsChamp() + statsChampionnat.getButsChamp());
            statChampionnat.setPassesChamp(statChampionnat.getPassesChamp() + statsChampionnat.getPassesChamp());
            statChampionnat.setMinutesJoueesChamp(statChampionnat.getMinutesJoueesChamp() + statsChampionnat.getMinutesJoueesChamp());

// 📌 Calcul de la moyenne pondérée de la cote
            double totalMinutes = statChampionnat.getMinutesJoueesChamp() + statsChampionnat.getMinutesJoueesChamp();
            statChampionnat.setMoyenneCoteChamp(
                    (totalMinutes > 0) ?
                            ((statChampionnat.getMoyenneCoteChamp() * statChampionnat.getMinutesJoueesChamp()) +
                                    (statsChampionnat.getCoteChamp() * statsChampionnat.getMinutesJoueesChamp())) / totalMinutes
                            : 5.0
            );

// 📌 Gestion des stats spécifiques aux gardiens
            if (Poste.fromString(joueursProjectionMap.get(joueurId).getPoste()).isGardien()) {
                int butsEncaisser = clotureDTO.getButAdversaire();
                int butsArretes = clotureDTO.getButsArretes().getOrDefault(joueurId, 0);

                // ✅ Additionner les nouvelles valeurs aux anciennes au lieu de les écraser
                statChampionnat.setButEncaisserChamp(statChampionnat.getButEncaisserChamp() + butsEncaisser);
                statChampionnat.setButArreterChamp(statChampionnat.getButArreterChamp() + butsArretes);

                if (butsEncaisser == 0) {
                    statChampionnat.setCleanSheet(statChampionnat.getCleanSheet() + 1); // ✅ Incrémentation des clean sheets
                }
            }

// 📌 Logs des valeurs en base après mise à jour
            log.info("✅ [APRES] Stats mises à jour en base pour Joueur ID={} | Buts={} | Passes={} | Minutes={} | Cote={} | Buts encaissés={} | Arrêts={} | Clean Sheets={}",
                    joueurId, statChampionnat.getButsChamp(), statChampionnat.getPassesChamp(),
                    statChampionnat.getMinutesJoueesChamp(), statChampionnat.getMoyenneCoteChamp(),
                    statChampionnat.getButEncaisserChamp(), statChampionnat.getButArreterChamp(),
                    statChampionnat.getCleanSheet());



            messagingTemplate.convertAndSend("/topic/stats/championnat/" + rencontre.getChampionnat().getIdChamp(), statsChampionnat);
        }

        log.info("📡 WebSocket : Statistiques du championnat envoyées pour le championnat ID={}", rencontre.getChampionnat().getIdChamp());
        log.info("✅ [Fin] Mise à jour des statistiques du championnat complétée.");
    }









    private void majStatsJoueur(List<FeuilleDeMatchProjection> feuillesProjections, ClotureRencontreDTO clotureDTO) {
        log.info("📌 [Début] Mise à jour des statistiques globales des joueurs.");

        Map<Long, List<StatistiquesRencontreDTO>> statsParJoueur = feuillesProjections.stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Exclure ceux qui n'ont pas joué
                .map(StatistiquesRencontreDTO::fromFeuilleDeMatchProjection)
                .collect(Collectors.groupingBy(StatistiquesRencontreDTO::getJoueurId));


        List<StatistiquesDTO> statsJoueurs = new ArrayList<>();

        // 🔹 Conversion et mise à jour des stats en une seule boucle
        feuillesProjections.stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Exclure ceux qui n'ont pas joué
                .forEach(feuilleProjection -> {
                    log.info("📌 Vérification de la feuille de match - ID={} | JID={} | Nom={} | Poste={} | Buts={} | Passes={} | Minutes={} | Cote={} | Buts Arrêtés={} | Buts Encaissés={}",
                            feuilleProjection.getId(), feuilleProjection.getJid(), feuilleProjection.getNom(),
                            feuilleProjection.getPoste(), feuilleProjection.getButs(), feuilleProjection.getPasses(),
                            feuilleProjection.getMinutesJouees(), feuilleProjection.getCote(),
                            feuilleProjection.getButArreter(), feuilleProjection.getButEncaisser());

                    StatistiquesRencontreDTO statsRencontre = StatistiquesRencontreDTO.fromFeuilleDeMatchProjection(feuilleProjection);
                    StatistiquesDTO statsJoueur = StatistiquesDTO.fromRencontres(Collections.singletonList(statsRencontre));

                    // ✅ Logs avant mise à jour
                    log.info("🔍 [AVANT] Stats Joueur ID={} | Buts={} | Passes={} | Minutes={} | cote={}",
                            statsJoueur.getJoueurId(), statsJoueur.getButs(), statsJoueur.getPasses(), statsJoueur.getTotalMinutesJouees(), statsJoueur.getCote());

// ✅ Mise à jour des stats générales du joueur
                    joueurRepository.majStatsJoueur(
                            statsJoueur.getJoueurId(),
                            statsJoueur.getButs(),
                            statsJoueur.getPasses(),
                            statsJoueur.getTotalMinutesJouees(),
                            statsJoueur.getCote()
                    );




                    // ✅ Logs avant mise à jour gardien
                    if ("GB".equals(statsRencontre.getPoste())) {
                        log.info("🔍 [AVANT     STAT GLOBAL] Stats Gardien ID={} | Buts arrêtés={} | Buts encaissés={} | Clean Sheets={}",
                                statsJoueur.getJoueurId(), statsJoueur.getButArreter(), statsJoueur.getButEncaisser(), statsJoueur.getCleanSheet());

                        // ✅ Mise à jour spécifique pour les gardiens
                        joueurRepository.majStatsGardien(
                                statsJoueur.getJoueurId(),
                                statsJoueur.getButArreter(),
                                statsJoueur.getButEncaisser(),
                                statsJoueur.getCleanSheet()
                        );

                        // ✅ Logs après mise à jour
                        log.info("✅ [APRES] Stats Gardien cote={} mises à jour en base.", statsJoueur.getCote());
                    }
                    // ✅ Logs avant mise à jour
                    log.info("🔍 [APRES   STAT GLOBAL] Stats Joueur ID={} | Buts={} | Passes={} | Minutes={} | cote={}",
                            statsJoueur.getJoueurId(), statsJoueur.getButs(), statsJoueur.getPasses(), statsJoueur.getTotalMinutesJouees(), statsJoueur.getCote());


                    statsJoueurs.add(statsJoueur);
                });



        messagingTemplate.convertAndSend("/topic/stats/joueurs", statsJoueurs);
        log.info("✅ [Fin] Mise à jour des statistiques des joueurs complétée.");
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
        log.info("📌 Calcul des points pour la rencontre ID={} | Score Équipe Locale={} | Score Adversaire={}",
                rencontre.getRid(), rencontre.getScoreEquipeLocale(), rencontre.getButAdversaire());

        int points;

        if (rencontre.getScoreEquipeLocale() > rencontre.getButAdversaire()) {
            points = 3; // ✅ Victoire → 3 points
            log.info("🏆 Victoire détectée → 3 points attribués.");
        } else if (rencontre.getScoreEquipeLocale() == rencontre.getButAdversaire()) {
            points = 1; // 🏳️ Match nul → 1 point
            log.info("🤝 Match nul détecté → 1 point attribué.");
        } else {
            points = 0; // ❌ Défaite → 0 point
            log.info("❌ Défaite détectée → 0 point attribué.");
        }

        log.info("📌 Points attribués pour la rencontre ID={} : {}", rencontre.getRid(), points);
        return points;
    }










    /**
     * Récupère l'historique des buts et des remplacements d'une rencontre
     */
    @Transactional(readOnly = true)
    public List<EvenementMatchDTO> getHistoriqueEvenements(Long idRencontre) {
        log.info("📥 Récupération de l'historique des buts et des remplacements pour la rencontre ID={}", idRencontre);

        TerrainDTO terrainDTO = getTerrain(idRencontre);
        List<EvenementMatchDTO> historique = new ArrayList<>();

        // 📌 Ajout des buts avec format FIFA 🎮
        terrainDTO.getButsModifies().forEach((joueurId, nbButs) -> {
            FeuilleDeMatchDTO joueur = terrainDTO.getTitulaires().stream()
                    .filter(j -> j.getJid().equals(joueurId))
                    .findFirst()
                    .or(() -> terrainDTO.getRemplacants().stream()
                            .filter(j -> j.getJid().equals(joueurId))
                            .findFirst())
                    .orElse(null);

            if (joueur != null) {
                for (int i = 0; i < nbButs; i++) {
                    // ✅ Récupération correcte des passeurs depuis la projection
                    List<Long> passeursIds = joueur.getPasseursId() != null ? joueur.getPasseursId() : new ArrayList<>();
                    List<String> nomsPasseurs = joueur.getNomsPasseurs() != null ? joueur.getNomsPasseurs() : new ArrayList<>();

                    // ✅ Vérification si un passeur est enregistré
                    String iconePasseur = passeursIds.isEmpty() ? "" : "🎯 " + nomsPasseurs.get(0);
                    Long idPasseur = passeursIds.isEmpty() ? null : passeursIds.get(0);

                    historique.add(new EvenementMatchDTO(
                            idRencontre,
                            joueurId,
                            "⚽ " + joueur.getNom(),
                            terrainDTO.getMinutesJouees().getOrDefault(joueurId, 0.0).intValue(), // ✅ Cast explicite en int

                            "BUT",
                            idPasseur,
                            iconePasseur
                    ));
                }
            }
        });

        // 📌 Ajout des remplacements en format timeline 🔄
        for (FeuilleDeMatchDTO remplacant : terrainDTO.getRemplacants()) {
            if (remplacant.isAjoue()) {
                Long idEntrant = remplacant.getJid();
                Double minuteJoueeEntrant = remplacant.getMinutesJouees();
                Double minuteEntree = 1.0 - minuteJoueeEntrant; // 🔥 Correction pour la vraie minute d’entrée

                // 🔥 Trouver le joueur sortant (celui qui a joué avant cette minute)
                terrainDTO.getTitulaires().stream()
                        .filter(j -> Math.abs(j.getMinutesJouees() - minuteEntree) < 0.001)
                        .findFirst()
                        .ifPresent(joueurSortant -> {
                            historique.add(new EvenementMatchDTO(
                                    idRencontre,
                                    joueurSortant.getJid(),
                                    joueurSortant.getNom(),
                                    (int) (minuteEntree * 90),
                                    "SORTIE",
                                    null,
                                    "🟥⬅️" // Icône pour sortie
                            ));
                        });

                // ✅ Ajout du joueur entrant à la même minute
                historique.add(new EvenementMatchDTO(
                        idRencontre,
                        idEntrant,
                        remplacant.getNom(),
                        (int) (minuteEntree * 90),
                        "ENTRÉE",
                        null,
                        "🟩➡️" // Icône pour entrée
                ));
            }
        }

        // 📌 Trier les événements chronologiquement
        historique.sort(Comparator.comparingInt(EvenementMatchDTO::getMinute));

        log.info("✅ {} événements trouvés pour la rencontre ID={} ", historique.size(), idRencontre);

        // ✅ Mettre à jour en temps réel via WebSocket (si utilisé)
        messagingTemplate.convertAndSend("/topic/historique/" + idRencontre, historique);

        log.info("📡 WebSocket : Historique des événements mis à jour pour la rencontre ID={}", idRencontre);

        return historique;
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
                            feuille.getJid(),
                            feuille.getJoueur().getNom(),
                            feuille.getJoueur().getPoste().name(),
                            feuille.getButs() != null ? feuille.getButs() : 0, // ✅ Cast en `int`
                            feuille.getPasses() != null ? feuille.getPasses() : 0, // ✅ Cast en `int`
                            feuille.getCote() != null ? feuille.getCote() : 5.0, // ✅ Cast en `double`
                            feuille.getMinutesJouees() != null ? feuille.getMinutesJouees() : 0.0, // ✅ Cast en `double`
                            feuille.isAjoue(),
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
                rencontre.getHommeDuMatch().getJid(),
                rencontre.getNomHommeDuMatch(),
                rencontre.getStatutRencontre(),
                feuillesDeMatchDTO // ✅ Liste correcte des `FeuilleDeMatchDTO`
        );
    }


    /**
     * ✅ Retourne la liste des postes compatibles avec un poste donné.
     */
    /**
     * 📌 Retourne la liste des postes compatibles avec un poste donné (alignée sur le front-end).
     */
    private List<String> getPostesCompatibles(String posteKey) {
        Map<String, List<String>> mappingFlexible = Map.ofEntries(
                Map.entry("GB", List.of("GB")),
                Map.entry("DC_CENTRAL", List.of("DC_DROIT", "DC_GAUCHE", "DC_CENTRAL", "DD", "DG", "MDF")),
                Map.entry("DC_DROIT", List.of("DC_DROIT", "DC_CENTRAL", "DD", "DG", "DC_GAUCHE", "MDF")),
                Map.entry("DC_GAUCHE", List.of("DC_DROIT", "DC_GAUCHE", "DC_CENTRAL", "DG", "DD", "MDF")),
                Map.entry("DD", List.of("DD", "DC_DROIT", "MDF", "DC_CENTRAL", "DG", "DC_GAUCHE")),
                Map.entry("DG", List.of("DG", "DC_GAUCHE", "MDF", "DC_CENTRAL", "DD", "DC_DROIT")),
                Map.entry("MDF", List.of("MDF", "MR", "MO", "DG", "DD", "DC_CENTRAL", "DC_GAUCHE", "DC_DROIT")),
                Map.entry("MR", List.of("MR", "MO", "MDF")),
                Map.entry("MO", List.of("MO", "MR", "MDF", "AIG", "AID", "SA", "AC_DROIT", "AC_GAUCHE")),
                Map.entry("AIG", List.of("AIG", "AID", "AC", "SA", "AC_GAUCHE", "AC_DROIT")),
                Map.entry("AID", List.of("AIG", "AID", "AC", "SA", "AC_GAUCHE", "AC_DROIT")),
                Map.entry("AC", List.of("AIG", "AID", "AC", "AC_DROIT", "AC_GAUCHE", "SA")),
                Map.entry("SA", List.of("AIG", "AID", "AC", "SA", "AC_DROIT", "AC_GAUCHE", "MO")),
                Map.entry("AC_DROIT", List.of("AIG", "AID", "AC", "SA", "AC_DROIT", "AC_GAUCHE", "MO")),
                Map.entry("AC_GAUCHE", List.of("AIG", "AID", "AC", "SA", "AC_DROIT", "AC_GAUCHE", "MO"))
        );

        return mappingFlexible.getOrDefault(posteKey, List.of(posteKey));
    }

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

        List<StatistiquesRencontreDTO> statsRencontre = rencontre.getFeuilleDeMatchs().stream()
                .filter(fm -> fm.isTitulaire() || fm.isAjoue()) // ✅ Filtrage joueurs ayant joué
                .map(feuille -> new StatistiquesRencontreDTO(
                        feuille.getJid(),
                        feuille.getJoueur().getNom(),
                        feuille.getJoueur().getClass().getSimpleName(),
                        feuille.getJoueur().getPoste().name(),
                        feuille.getButs(),
                        feuille.getButs() / Math.max(feuille.getMinutesJouees(), 1),
                        feuille.getPasses(),
                        feuille.getPasses() / Math.max(feuille.getMinutesJouees(), 1),
                        (feuille.getButs() + feuille.getPasses()) / 2.0,
                        feuille.getCote(),
                        (feuille.getButs() * 3) + feuille.getPasses(),
                        0,
                        feuille.getMinutesJouees(),
                        feuille.getButArreter(),  // ✅ Ajout des buts arrêtés
                        feuille.getButEncaisser(), // ✅ Ajout des buts encaissés
                        rencontre.getRid(),
                        feuille.getPasseurs().stream().map(Joueur::getJid).toList(),
                        feuille.getPasseurs().stream().map(Joueur::getNom).toList()
                ))
                .toList();

        messagingTemplate.convertAndSend("/topic/stats/" + idRencontre, statsRencontre);

        log.info("📡 WebSocket : Statistiques mises à jour pour la rencontre ID={}", idRencontre);

        return statsRencontre;
    }




}
