package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.dto.rencontre.*;

import com.genesis.api.statmanager.service.RencontreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 📌 Contrôleur exposant les services liés aux rencontres.
 */
@RestController
@RequestMapping("/api/rencontres")
@RequiredArgsConstructor
@Slf4j
public class RencontreController {

    private final RencontreService rencontreService;

    // 📌 1️⃣ GESTION DES RENCONTRES
    @DeleteMapping("/{idRencontre}")
    public ResponseEntity<Void> supprimerRencontre(@PathVariable Long idRencontre) {
        rencontreService.supprimerRencontre(idRencontre);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idChampionnat}/selection")
    public ResponseEntity<NouvelleRencontreSelectionDTO> getNouvelleRencontreSelection(@PathVariable Long idChampionnat) {
        log.info("📥 Récupération de la sélection des joueurs pour le championnat ID={}", idChampionnat);
        NouvelleRencontreSelectionDTO selectionDTO = rencontreService.getNouvelleRencontreSelection(idChampionnat);
        return ResponseEntity.ok(selectionDTO);
    }

    @GetMapping("/championnat/{idChampionnat}")
    public ResponseEntity<List<RencontreDTO>> findByChampionnat(@PathVariable Long idChampionnat) {
        return ResponseEntity.ok(rencontreService.findByChampionnat(idChampionnat));
    }

    @GetMapping("/terminees")
    public ResponseEntity<List<RencontreDTO>> findRencontresTerminees() {
        return ResponseEntity.ok(rencontreService.findRencontresTerminees());
    }

    @GetMapping("/{idRencontre}/details")
    public ResponseEntity<RencontreDetailDTO> getRencontreDetailsStat(@PathVariable Long idRencontre) {
        return ResponseEntity.ok(rencontreService.getRencontreDetailsStat(idRencontre));
    }

    // 📌 2️⃣ GESTION DU MATCH EN TEMPS RÉEL
    @GetMapping("/{idRencontre}/terrain")
    public ResponseEntity<TerrainDTO> getTerrain(@PathVariable Long idRencontre) {
        return ResponseEntity.ok(rencontreService.getTerrain(idRencontre));
    }

    @PostMapping("/{idRencontre}/update-stats")
    public ResponseEntity<TerrainDTO> updateStatsEnTempsReel(
            @PathVariable Long idRencontre,
            @RequestParam Long idJoueur,
            @RequestParam int buts,
            @RequestParam(required = false) Long idPasseur) {
        return ResponseEntity.ok(rencontreService.updateStatsEnTempsReel(idRencontre, idJoueur, buts, idPasseur));
    }

    @PostMapping("/{idRencontre}/remplacement")
    public ResponseEntity<TerrainDTO> effectuerRemplacement(@RequestBody RemplacementDTO remplacementDTO) {
        return ResponseEntity.ok(rencontreService.effectuerRemplacement(remplacementDTO));
    }

    // 📌 3️⃣ FIN DE MATCH ET HISTORIQUE
    @PostMapping("/{idRencontre}/cloturer")
    public ResponseEntity<Void> cloturerRencontre(@RequestBody ClotureRencontreDTO clotureDTO) {
        rencontreService.cloturerRencontre(clotureDTO);
        log.info("📡 Clôture rencontre ID={} avec les données : {}", clotureDTO.getIdRencontre(), clotureDTO);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idRencontre}/historique")
    public ResponseEntity<List<EvenementMatchDTO>> getHistoriqueEvenements(@PathVariable Long idRencontre) {
        log.info("📡 Requête reçue pour récupérer l'historique des événements de la rencontre ID={}", idRencontre);
        return ResponseEntity.ok(rencontreService.getHistoriqueEvenements(idRencontre));
    }


    @PostMapping("/validerSelection")
    public ResponseEntity<TerrainDTO> validerSelection(@RequestBody VestiaireDTO vestiaireDTO) {
        log.info("📌 Validation de la sélection des titulaires et remplaçants pour la rencontre ID={}", vestiaireDTO.getIdChampionnat());

        TerrainDTO terrainDTO = rencontreService.validerSelectionVestiaire(vestiaireDTO);

        return ResponseEntity.ok(terrainDTO); // ✅ Maintenant on retourne un TerrainDTO
    }

    @PostMapping("/{idRencontre}/construire")
    public ResponseEntity<TerrainDTO> construireTerrain(@PathVariable Long idRencontre) {
        log.info("📥 Préparation du terrain pour la rencontre ID={}", idRencontre);
        return ResponseEntity.ok(rencontreService.construireTerrain(idRencontre));
    }

    @PostMapping("/{idRencontre}/valider")
    public ResponseEntity<TerrainDTO> validerTerrain(@PathVariable Long idRencontre, @RequestBody TerrainDTO terrainDTO) {
        log.info("✅ Validation du terrain pour ID={}", idRencontre);
        return ResponseEntity.ok(rencontreService.initialiserTerrain(idRencontre, terrainDTO));
    }


    @PostMapping("/{idRencontre}/init")
    public ResponseEntity<TerrainDTO> initialiserTerrain(@PathVariable Long idRencontre, @RequestBody TerrainDTO terrainDTO) {
        log.info("✅ [BACKEND] Initialisation du terrain pour ID={}", idRencontre);
        TerrainDTO terrainMisAJour = rencontreService.initialiserTerrain(idRencontre, terrainDTO);
        log.info("✅ [BACKEND] Terrain mis à jour avec succès !");
        return ResponseEntity.ok(terrainMisAJour);
    }


    @PutMapping("/{id}/butAdversaire")
    public ResponseEntity<Void> mettreAJourButAdversaire(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        int nouveauScore = body.get("nouveauScore");
        rencontreService.mettreAJourButAdversaire(id, nouveauScore);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{idRencontre}/gardien/{idJoueur}")
    public ResponseEntity<Void> mettreAJourStatsGardien(
            @PathVariable Long idRencontre,
            @PathVariable Long idJoueur,
            @RequestBody Map<String, Integer> body) {

        log.info("📌 Mise à jour des stats du gardien - Rencontre ID={} | Joueur ID={}", idRencontre, idJoueur);

        // ✅ On ne récupère que `butArreter`, car `butEncaisser` est directement récupéré en base
        int butArreter = body.getOrDefault("butArreter", 0);

        // ✅ Appel du service (qui récupère lui-même `butEncaisser` en base)
        rencontreService.mettreAJourStatsGardien(idRencontre, idJoueur, butArreter);

        log.info("✅ Stats du gardien mises à jour : Arrêts={}", butArreter);

        return ResponseEntity.ok().build();
    }

    /**
     * 📌 Met à jour les cotes des joueurs après attribution en fin de match.
     */
    @PostMapping("/{idRencontre}/maj-cotes")
    public ResponseEntity<Void> mettreAJourCotes(
            @PathVariable Long idRencontre,
            @RequestBody Map<Long, Double> cotesJoueurs) { // ✅ Clé = joueurId, Valeur = nouvelle cote
        rencontreService.mettreAJourCotes(idRencontre, cotesJoueurs);
        return ResponseEntity.ok().build();
    }
}

