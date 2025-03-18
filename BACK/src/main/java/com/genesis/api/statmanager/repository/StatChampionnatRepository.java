package com.genesis.api.statmanager.repository;

import com.genesis.api.statmanager.dto.StatChampionnatDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.rencontre.ClotureRencontreDTO;
import com.genesis.api.statmanager.model.Championnat;
import com.genesis.api.statmanager.model.Rencontre;
import com.genesis.api.statmanager.model.StatChampionnat;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatChampionnatRepository extends JpaRepository<StatChampionnat, Long> {

    // ✅ Récupérer les statistiques d'un joueur dans un championnat spécifique en utilisant `joueurId`
    @Query("""
        SELECT s FROM StatChampionnat s
        WHERE s.joueurId = :joueurId
        AND s.championnat = :championnat
    """)
    Optional<StatChampionnat> findByJoueurIdAndChampionnat(@Param("joueurId") Long joueurId, @Param("championnat") Championnat championnat);

    // ✅ Récupérer toutes les statistiques d'un joueur sans instancier `Joueur`
    @Query("""
        SELECT s FROM StatChampionnat s
        WHERE s.joueurId = :joueurId
        ORDER BY s.championnat.idChamp DESC
    """)
    List<StatChampionnat> findByJoueurId(@Param("joueurId") Long joueurId);

    // ✅ Récupérer toutes les statistiques d'un championnat sans instancier `Joueur`
    @Query("""
        SELECT s FROM StatChampionnat s
        WHERE s.championnat = :championnat
        ORDER BY s.butsChamp DESC
    """)
    List<StatChampionnat> findByChampionnat(@Param("championnat") Championnat championnat);

    // ✅ Récupérer les meilleurs buteurs d'un championnat sans instancier `Joueur`
    @Query("""
        SELECT s FROM StatChampionnat s
        WHERE s.championnat = :championnat
        ORDER BY s.butsChamp DESC
    """)
    List<StatChampionnat> findTopScorersByChampionnat(@Param("championnat") Championnat championnat);

    // ✅ Récupérer les meilleurs buteurs sous forme de `JoueurDTO` via projection
    @Query("""
        SELECT new com.genesis.api.statmanager.dto.StatChampionnatDTO(
            s.id,
            s.joueurId,
            (SELECT j.nom FROM Joueur j WHERE j.jid = s.joueurId),
            s.championnat.idChamp,
            s.butsChamp,
            s.passesChamp,
            s.minutesJoueesChamp,
            s.moyenneCoteChamp
        )
        FROM StatChampionnat s
        WHERE s.championnat = :championnat
        ORDER BY s.butsChamp DESC
    """)
    List<StatChampionnatDTO> findTopScorersDTOByChampionnat(@Param("championnat") Championnat championnat);

    // ✅ Récupérer toutes les statistiques sous forme de DTO sans instancier `Joueur`
    @Query("""
        SELECT new com.genesis.api.statmanager.dto.StatChampionnatDTO(
            s.id,
            s.joueurId,
            (SELECT j.nom FROM Joueur j WHERE j.jid = s.joueurId),
            s.championnat.idChamp,
            s.butsChamp,
            s.passesChamp,
            s.minutesJoueesChamp,
            s.moyenneCoteChamp
        )
        FROM StatChampionnat s
        WHERE s.championnat.idChamp = :idChampionnat
    """)
    List<StatChampionnatDTO> findByChampionnatId(@Param("idChampionnat") Long idChampionnat);



    @Query("""
    SELECT new com.genesis.api.statmanager.dto.joueur.JoueurDTO(
        j.jid,
        j.nom,
        j.poste
    
    )
    FROM StatChampionnat s
    JOIN Joueur j ON s.joueurId = j.jid
    WHERE s.championnat.idChamp = :idChampionnat
""")
    List<JoueurDTO> findJoueursByChampionnatId(@Param("idChampionnat") Long idChampionnat);




    @Modifying
    @Query("""
    UPDATE StatChampionnat s
    SET s.butsChamp = s.butsChamp + :buts, 
        s.passesChamp = s.passesChamp + :passes, 
        s.minutesJoueesChamp = s.minutesJoueesChamp + :minutesJouees,
        s.cleanSheet = s.cleanSheet + :cleanSheet,
        s.moyenneCoteChamp = CASE 
            WHEN s.minutesJoueesChamp + :minutesJouees = 0 THEN :moyenneCote
            ELSE ((s.moyenneCoteChamp * s.minutesJoueesChamp) + (:moyenneCote * :minutesJouees)) / (s.minutesJoueesChamp + :minutesJouees)
        END
    WHERE s.joueurId = :joueurId AND s.championnat.idChamp = :championnatId
""")
    void majStatsChampionnat(@Param("joueurId") Long joueurId,
                             @Param("championnatId") Long championnatId,
                             @Param("buts") int buts,
                             @Param("passes") int passes,
                             @Param("minutesJouees") double minutesJouees,
                             @Param("moyenneCote") double moyenneCote,
                             @Param("cleanSheet") int cleanSheet);



    /**
     * ✅ Récupère le total des minutes jouées par un joueur dans un championnat.
     */
    @Query("""
        SELECT COALESCE(SUM(f.minutesJouees), 0)
        FROM FeuilleDeMatch f 
        WHERE f.jid = :joueurId AND f.rencontre.championnat.idChamp = :championnatId
    """)
    int sumMinutesJoueesByJoueurAndChampionnat(@Param("joueurId") Long joueurId, @Param("championnatId") Long championnatId);








    @Modifying
    @Query("""
    UPDATE StatChampionnat s
    SET s.cleanSheet = s.cleanSheet + 1
    WHERE s.joueurId IN (
        SELECT f.jid FROM FeuilleDeMatch f
        WHERE f.rencontre.rid = :rencontreId AND f.joueur.typeJoueur IN ('GARDIEN', 'DEFENSEUR')
    )
    AND :butAdversaire = 0
""")
    void updateCleanSheetsAfterMatch(@Param("rencontreId") Long rencontreId, @Param("butAdversaire") int butAdversaire);

}
