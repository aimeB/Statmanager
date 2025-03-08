package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurLightDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import com.genesis.api.statmanager.dto.statistique.StatCompositeDTO;
import com.genesis.api.statmanager.dto.statistique.StatCritereDTO;
import com.genesis.api.statmanager.dto.statistique.StatTopJoueurDTO;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import com.genesis.api.statmanager.service.JoueurService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/joueur")
@RequiredArgsConstructor
public class JoueurController {

    private final JoueurService joueurService;
    private static final Logger log = LoggerFactory.getLogger(JoueurController.class); // ✅ Ajout du logger






    // -------------------------------------------------------------------------
    // 1) Récupère tous les joueurs (DTO complet)
    // -------------------------------------------------------------------------
    @GetMapping("/all")
    // ✅ Récupération de tous les joueurs avec leurs statistiques globales
    public ResponseEntity<List<StatistiquesDTO>> getAllJoueursStats() {
        List<StatistiquesDTO> statsJoueurs = joueurService.getAllJoueursStats();
        return ResponseEntity.ok(statsJoueurs);
    }

    // -------------------------------------------------------------------------
    // 2) Récupère un joueur par ID (DTO complet)
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<JoueurDTO> getJoueurById(@PathVariable Long id) {
        JoueurDTO joueurDTO = joueurService.findByIdAsDTO(id);
        return ResponseEntity.ok(joueurDTO);
    }

    // -------------------------------------------------------------------------
    // 3) Récupère la liste "light" des joueurs (ID, Nom, Poste)
    // -------------------------------------------------------------------------
    @GetMapping("/light")
    public ResponseEntity<List<JoueurLightDTO>> getAllJoueursLight() {
        List<JoueurLightDTO> listLight = joueurService.getJoueursLight();
        log.info("👥 controller getAllJoueursLight() : {} joueurs", listLight);
        return ResponseEntity.ok(listLight);
    }

    // -------------------------------------------------------------------------
    // 4) Récupère les stats globales d’un joueur (buts, passes, minutes, etc.)
    // -------------------------------------------------------------------------
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getJoueurStats(@PathVariable Long id) {
        System.out.println("📩 Requête reçue pour ID: " + id);

        try {
            JoueurStatGlobalDTO joueurStats = joueurService.findByIdWithGlobalStats(id);
            System.out.println("📊 Joueur trouvé: " + joueurStats);
            return ResponseEntity.ok(joueurStats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du joueur ID=" + id + " : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + e.getMessage());
        }
    }


    // -------------------------------------------------------------------------
    // 5) Récupère le top des joueurs selon un critère et un poste éventuel
    // -------------------------------------------------------------------------
    @GetMapping("/top")
    public ResponseEntity<List<?>> getTopJoueursGlobaux(@RequestParam String critere,
                                                        @RequestParam(required = false) String poste) {
        return ResponseEntity.ok(joueurService.getTopJoueursGlobaux(critere, poste));
    }


    @GetMapping("/top/critere")
    public ResponseEntity<StatCritereDTO> getJoueursParCritere(@RequestParam String critere) {
        return ResponseEntity.ok(joueurService.getJoueursParCritere(critere));
    }

    /**
     * 📌 Récupère les 5 meilleurs joueurs des dernières rencontres.
     */
    @GetMapping("/top/meilleurs")
    public ResponseEntity<StatTopJoueurDTO> getMeilleursJoueurs() {
        return ResponseEntity.ok(joueurService.getMeilleursJoueurs());
    }

    /**
     * 📌 Récupère les joueurs triés selon un score composite.
     */
    @GetMapping("/top/composite")
    public ResponseEntity<StatCompositeDTO> getJoueursParScoreComposite() {
        return ResponseEntity.ok(joueurService.getJoueursParScoreComposite());
    }

    // 📌 Nouveau Endpoint : récupérer un joueur par son nom
    @GetMapping("/nom/{nom}/stats")
    public ResponseEntity<JoueurStatGlobalDTO> getJoueurStatsByNom(@PathVariable String nom) {
        return ResponseEntity.ok(joueurService.findByNomWithGlobalStats(nom));
    }


}
