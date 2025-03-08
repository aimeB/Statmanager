package com.genesis.api.statmanager.service.impl;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.model.*;
import com.genesis.api.statmanager.model.enumeration.TimePlay;
import com.genesis.api.statmanager.repository.FeuilleDeMatchRepository;
import com.genesis.api.statmanager.repository.JoueurRepository;
import com.genesis.api.statmanager.repository.RencontreRepository;
import com.genesis.api.statmanager.service.FeuilleDeMatchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 📌 Implémentation du service de gestion des feuilles de match.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeuilleDeMatchServiceImpl implements FeuilleDeMatchService {

    private final FeuilleDeMatchRepository feuilleDeMatchRepository;
    private final RencontreRepository rencontreRepository;
    private final JoueurRepository joueurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PersistenceContext
    private EntityManager entityManager; // ✅ Utilisé pour `flush()` et `refresh()`

    // =========================================================================
    // 🔹 SECTION 1 : RÉCUPÉRATION DES DONNÉES (LECTURE)
    // =========================================================================

    @Override
    public List<FeuilleDeMatchDTO> findByRencontre(Long rencontreId) {
        return feuilleDeMatchRepository.findByRencontre(rencontreId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FeuilleDeMatchDTO findByRencontreAndJoueur(Long rencontreId, Long joueurId) {
        FeuilleDeMatch feuille = feuilleDeMatchRepository.findByRencontreAndJoueur(rencontreId, joueurId)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ Feuille de match introuvable"));

        return mapToDTO(feuille);
    }

    // =========================================================================
    // 🔹 SECTION 2 : MISE À JOUR DES STATISTIQUES ET REMPLACEMENTS
    // =========================================================================

    // =========================================================================
    // 🔹 SECTION 3 : CALCULS ET STATISTIQUES
    // ========================================================================

    // =========================================================================
    // 🔹 SECTION 4 : MAPPERS ET CONVERSIONS
    // =========================================================================

    public FeuilleDeMatchDTO mapToDTO(FeuilleDeMatch feuille) {
        Joueur joueur = feuille.getJoueur();

        if (joueur == null) {
            throw new IllegalArgumentException("❌ Joueur introuvable dans la feuille de match !");
        }

        // ✅ Vérification stricte : s'assurer que le joueur n'est pas une instance abstraite
        if (joueur.getClass().equals(Joueur.class)) {
            log.error("🚨 Joueur abstrait détecté ! ID={} | Nom={}", joueur.getJid(), joueur.getNom());
            throw new IllegalStateException("❌ Impossible d'instancier un joueur abstrait !");
        }
        // ✅ Vérification du type de joueur avant conversion
        if (!(joueur instanceof Attaquant) && !(joueur instanceof Milieu) &&
                !(joueur instanceof Defenseur) && !(joueur instanceof Gardien)) {
            throw new IllegalStateException("❌ Impossible d'instancier un joueur abstrait !");
        }

        return FeuilleDeMatchDTO.builder()
                .id(feuille.getFeuilleId())
                .rid(feuille.getRencontre().getRid())
                .jid(joueur.getJid())
                .nom(joueur.getNom())
                .poste(joueur.getPoste().name())
                .buts(feuille.getButs())
                .passes(feuille.getPasses())
                .moyenneCote(feuille.getMoyenneCote())
                .minutesJouees(feuille.getMinutesJouees())
                .titulaire(feuille.isTitulaire())
                .aJoue(feuille.getAJoue())
                .build();
    }


}
