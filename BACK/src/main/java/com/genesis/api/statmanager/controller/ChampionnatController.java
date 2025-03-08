package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.dto.championnat.ChampionnatDTO;
import com.genesis.api.statmanager.dto.championnat.*;
import com.genesis.api.statmanager.dto.global.StatistiquesChampionnatDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.RencontreDetailDTO;
import com.genesis.api.statmanager.service.ChampionnatService;
import com.genesis.api.statmanager.service.RencontreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/championnats")
@RequiredArgsConstructor
public class ChampionnatController {

    private final ChampionnatService championnatService;

    @Autowired
    private final RencontreService rencontreService;






                          /**SECTION 1 : GESTION DES CHAMPIONNATS*









    /**
     * Crée un nouveau championnat avec les joueurs sélectionnés.
     */
    @PostMapping("/create")
    public ResponseEntity<ChampionnatDTO> creerChampionnat(@RequestBody ChampionnatCreationDTO dto) {
        if (dto.getJoueursIds() == null || dto.getJoueursIds().size() != 23) {
            return ResponseEntity.badRequest().body(null);
        }
        if (dto.getDivision() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        System.out.println("📥 Championnat reçu : " + dto.getDivision() + " avec joueurs : " + dto.getJoueursIds());

        ChampionnatDTO championnat = championnatService.creerChampionnat(dto.getDivision(), dto.getJoueursIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(championnat);
    }


    /**
     * 📌 Récupère les statistiques des joueurs pour une rencontre donnée.
     */
    @GetMapping("/rencontres/{idRencontre}/stats")
    public ResponseEntity<List<StatistiquesRencontreDTO>> getStatistiquesRencontre(@PathVariable Long idRencontre) {
        return ResponseEntity.ok(rencontreService.getStatistiquesRencontre(idRencontre));
    }





                         /**SECTION 2 : RÉCUPÉRATION DES DONNÉES**






    /**
     * Endpoint pour récupérer les détails d'un championnat avec toutes ses rencontres.
     */
    @GetMapping("/{idChamp}/details")
    public ResponseEntity<ChampionnatDetailWithRencontresDTO> getChampionnatWithRencontres(@PathVariable Long idChamp) {
        return ResponseEntity.ok(championnatService.findChampionnatWithRencontres(idChamp));
    }




    /**
     * 📌 Récupère la liste des 10 derniers championnats et les détails d'un championnat sélectionné.
     */
    @GetMapping({"/overview", "/overview/{idChamp}"}) // ✅ Permet d'appeler avec ou sans ID
    public ResponseEntity<ChampionnatOverviewDTO> getChampionnatOverview(@PathVariable(required = false) Long idChamp) {
        ChampionnatOverviewDTO overviewDTO = championnatService.getChampionnatOverview(idChamp);
        return ResponseEntity.ok(overviewDTO);
    }


    /**
     * 📌 Récupère la liste des 10 derniers championnats.
     */
    @GetMapping("/last10")
    public ResponseEntity<List<ChampionnatLightDTO>> getLast10Championnats() {
        List<ChampionnatLightDTO> championnats = championnatService.findTop10ByOrderByIdChampDesc();
        return ResponseEntity.ok(championnats);
    }






                           /** **SECTION 3 : STATISTIQUES & RENCONTRES****





    /**
     * 📌 Récupère les statistiques des joueurs dans un championnat.
     */
    @GetMapping("/{idChamp}/statistiques")
    public ResponseEntity<List<StatistiquesChampionnatDTO>> getStatistiquesChampionnat(@PathVariable Long idChamp) {
        List<StatistiquesChampionnatDTO> stats = championnatService.getStatistiquesChampionnat(idChamp);
        return ResponseEntity.ok(stats);
    }




    @GetMapping("/{idRencontre}/detailsStat")
    public ResponseEntity<RencontreDetailDTO> getRencontreDetailsStat(@PathVariable Long idRencontre) {
        RencontreDetailDTO rencontre = rencontreService.getRencontreDetailsStat(idRencontre);
        return ResponseEntity.ok(rencontre);
    }




}


