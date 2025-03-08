package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.repository.FeuilleDeMatchRepository;
import com.genesis.api.statmanager.service.FeuilleDeMatchService;
import com.genesis.api.statmanager.service.impl.FeuilleDeMatchServiceImpl;
import com.genesis.api.statmanager.service.impl.RencontreServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feuille-de-match")
@RequiredArgsConstructor
public class FeuilleDeMatchController {


        private final FeuilleDeMatchService feuilleDeMatchService;

        // -------------------------------------------------------------------------
        // 1) Récupère toutes les feuilles de match d’une rencontre
        // -------------------------------------------------------------------------
        @GetMapping("/rencontre/{rencontreId}")
        public ResponseEntity<List<FeuilleDeMatchDTO>> getFeuillesByRencontre(@PathVariable Long rencontreId) {
            List<FeuilleDeMatchDTO> feuilles = feuilleDeMatchService.findByRencontre(rencontreId);
            return ResponseEntity.ok(feuilles);
        }

        // -------------------------------------------------------------------------
        // 2) Récupère la feuille de match d’un joueur spécifique dans une rencontre
        // -------------------------------------------------------------------------
        @GetMapping("/rencontre/{rencontreId}/joueur/{joueurId}")
        public ResponseEntity<FeuilleDeMatchDTO> getFeuilleByRencontreAndJoueur(@PathVariable Long rencontreId,
                                                                                @PathVariable Long joueurId) {
            FeuilleDeMatchDTO feuille = feuilleDeMatchService.findByRencontreAndJoueur(rencontreId, joueurId);
            return ResponseEntity.ok(feuille);
        }



    }

