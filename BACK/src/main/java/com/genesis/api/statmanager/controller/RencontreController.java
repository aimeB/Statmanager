package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.*;
import com.genesis.api.statmanager.dto.statistique.StatCompositeDTO;
import com.genesis.api.statmanager.dto.statistique.StatCritereDTO;
import com.genesis.api.statmanager.dto.statistique.StatTopJoueurDTO;
import com.genesis.api.statmanager.service.RencontreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idRencontre}/historique")
    public ResponseEntity<List<EvenementMatchDTO>> getHistoriqueEvenements(@PathVariable Long idRencontre) {
        return ResponseEntity.ok(rencontreService.getHistoriqueEvenements(idRencontre));
    }

    @PostMapping("/validerSelection")
    public ResponseEntity<TerrainDTO> validerSelection(@RequestBody VestiaireDTO vestiaireDTO) {
        log.info("📌 Validation de la sélection des titulaires et remplaçants pour la rencontre ID={}", vestiaireDTO.getIdChampionnat());

        TerrainDTO terrainDTO = rencontreService.validerSelectionVestiaire(vestiaireDTO);

        return ResponseEntity.ok(terrainDTO); // ✅ Maintenant on retourne un TerrainDTO
    }


}
