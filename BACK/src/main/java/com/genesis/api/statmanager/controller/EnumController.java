package com.genesis.api.statmanager.controller;

import com.genesis.api.statmanager.model.enumeration.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    // ✅ Récupérer toutes les divisions
    @GetMapping("/divisions")
    public List<String> getAllDivisions() {
        return Arrays.stream(Division.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // ✅ Récupérer toutes les valeurs de PointMatch
    @GetMapping("/points-match")
    public Map<String, Integer> getPointMatch() {
        return Stream.of(PointMatch.values())
                .collect(Collectors.toMap(Enum::name, PointMatch::getValue));
    }

    // ✅ Récupérer toutes les valeurs de PointJoueur
    @GetMapping("/points-joueur")
    public Map<String, Integer> getPointJoueur() {
        return Stream.of(PointJoueur.values())
                .collect(Collectors.toMap(Enum::name, PointJoueur::getValue));
    }

    // ✅ Récupérer toutes les valeurs de Poste
    @GetMapping("/postes")
    public Map<String, String> getPostes() {
        return Stream.of(Poste.values())
                .collect(Collectors.toMap(Enum::name, Poste::getDescription));
    }

    // ✅ Récupérer tous les statuts de Championnat
    @GetMapping("/statuts")
    public Map<String, String> getStatuts() {
        return Stream.of(Statut.values())
                .collect(Collectors.toMap(Enum::name, Statut::getDescription));
    }

    // ✅ Récupérer tous les statuts de Rencontre
    @GetMapping("/statuts-rencontre")
    public List<String> getStatutsRencontre() {
        return Arrays.stream(StatutRencontre.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // ✅ Récupérer toutes les valeurs de TimePlay
    @GetMapping("/timeplay")
    public Map<String, Double> getTimePlay() {
        return Stream.of(TimePlay.values())
                .collect(Collectors.toMap(Enum::name, TimePlay::getPercentage));
    }
}
