package com.genesis.api.statmanager.repository;


import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurLightDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import com.genesis.api.statmanager.dto.joueur.PerformanceParDivision;
import com.genesis.api.statmanager.model.Joueur;
import com.genesis.api.statmanager.projection.JoueurLightProjection;
import com.genesis.api.statmanager.projection.JoueurProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoueurRepository extends JpaRepository<com.genesis.api.statmanager.model.Joueur, Long> {

    /**
     * ✅ Récupère un joueur sous forme de projection sans instancier `Joueur`
     */
    @Query("""
    SELECT j.jid AS jid, j.nom AS nom, j.poste AS poste,
           j.totalButs AS totalButs, j.totalPasses AS totalPasses,
           j.totalMinutesJouees AS totalMinutesJouees,
           j.totalMoyenneCote AS totalMoyenneCote,
           TYPE(j) AS typeJoueur
    FROM Joueur j WHERE j.jid = :id
""")

    Optional<JoueurProjection> findJoueurProjectionById(@Param("id") Long id);

    /**
     * ✅ Récupère plusieurs joueurs par leurs IDs sous forme de projection.
     */
    @Query("""
        SELECT j.jid AS jid, j.nom AS nom, j.poste AS poste, 
               j.totalButs AS totalButs, j.totalPasses AS totalPasses, 
               j.totalMinutesJouees AS totalMinutesJouees, 
               j.totalMoyenneCote AS totalMoyenneCote, 
               TYPE(j) AS typeJoueur
        FROM Joueur j WHERE j.jid IN :ids
    """)
    List<JoueurProjection> findAllJoueurProjectionsByIds(@Param("ids") List<Long> ids);

    /**
     * ✅ Récupère tous les joueurs sous forme de projection.
     */
    @Query("""
        SELECT j.jid AS jid, j.nom AS nom, j.poste AS poste, 
               j.totalButs AS totalButs, j.totalPasses AS totalPasses, 
               j.totalMinutesJouees AS totalMinutesJouees, 
               j.totalMoyenneCote AS totalMoyenneCote, 
               TYPE(j) AS typeJoueur
        FROM Joueur j
    """)
    List<JoueurProjection> findAllJoueursAsProjection();

    /**
     * ✅ Récupère les joueurs triés par poste sous forme de projection.
     */
    @Query("""
    SELECT j.jid AS jid, j.nom AS nom, j.poste AS poste
    FROM Joueur j
    ORDER BY j.poste
""")
    List<JoueurLightProjection> findAllOrderByPosteAsProjection();




    @Modifying
    @Query("""
    UPDATE Joueur j
    SET j.point = j.point + :points
    WHERE j.jid IN :joueurIds
""")
    void updatePointsForTopPlayers(@Param("joueurIds") List<Long> joueurIds, @Param("points") int points);





    @Modifying
    @Query("""
    UPDATE Joueur j
    SET j.point = j.point + :points
    WHERE j.jid = :joueurId
""")
    void updatePointsForPlayer(@Param("joueurId") Long joueurId, @Param("points") int points);


    @Query("""
    SELECT new com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO(
        COALESCE(f.joueurId, j.jid), j.nom, j.typeJoueur, j.poste,
        CAST(COALESCE(f.buts, 0) AS int),
        CAST(COALESCE(f.passes, 0) AS int),
        CAST(COALESCE(f.moyenneCote, 5.0) AS double),
        CAST(COALESCE(f.minutesJouees, 0) AS double),
        CAST(COALESCE(f.rencontre.rid, 0) AS Long)
    ) 
    FROM Joueur j
    LEFT JOIN FeuilleDeMatch f ON j.jid = f.joueurId 
    WHERE j.jid = :id 
    ORDER BY f.rencontre.rid DESC
""")
    Page<StatistiquesRencontreDTO> findDerniersMatchsByJoueur(@Param("id") Long id, Pageable pageable);



    @Query("""
    SELECT j.jid AS jid, j.nom AS nom, j.poste AS poste, 
           j.totalButs AS totalButs, j.totalPasses AS totalPasses, 
           j.totalMinutesJouees AS totalMinutesJouees, 
           j.totalMoyenneCote AS totalMoyenneCote, 
           TYPE(j) AS typeJoueur
    FROM Joueur j WHERE j.nom = :nom
""")
    Optional<JoueurProjection> findJoueurProjectionByNom(@Param("nom") String nom);


    @Query("SELECT j FROM Joueur j WHERE j.jid IN :ids")
    List<Joueur> findAllJoueursByIds(@Param("ids") List<Long> ids);



    @Query("""
    SELECT p.jid FROM FeuilleDeMatch f 
    JOIN f.passeurs p 
    WHERE f.joueurId = :id
""")
    List<Long> findPasseursByJoueur(@Param("id") Long id);



    @Query("""
    SELECT new com.genesis.api.statmanager.dto.joueur.PerformanceParDivision(
        f.rencontre.divisionAdversaire, 
        SUM(f.buts), 
        SUM(f.passes), 
        SUM(f.minutesJouees), 
        AVG(f.moyenneCote), 
        SUM(f.buts * 3 + f.passes)
    )
    FROM FeuilleDeMatch f
    WHERE f.joueurId = :id
    GROUP BY f.rencontre.divisionAdversaire
""")
    List<PerformanceParDivision> findPerformancesParDivision(@Param("id") Long id);






    @Modifying
    @Query("""
    UPDATE Joueur j
    SET j.totalButs = j.totalButs + :buts,
        j.totalPasses = j.totalPasses + :passes, 
        j.totalMinutesJouees = j.totalMinutesJouees + :minutesJouees,
        j.totalMoyenneCote = CASE 
            WHEN j.totalMinutesJouees + :minutesJouees = 0 THEN :moyenneCote
            ELSE ((j.totalMoyenneCote * j.totalMinutesJouees) + (:moyenneCote * :minutesJouees)) / (j.totalMinutesJouees + :minutesJouees)
        END
    WHERE j.jid = :joueurId
""")
    void majStatsJoueur(@Param("joueurId") Long joueurId,
                        @Param("buts") int buts,
                        @Param("passes") int passes,
                        @Param("minutesJouees") double minutesJouees,
                        @Param("moyenneCote") double moyenneCote);



    @Query("""
    SELECT COUNT(f.feuilleId) FROM FeuilleDeMatch f
    WHERE f.joueurId = :joueurId
""")
    int countMatchsByJoueur(@Param("joueurId") Long joueurId);



    @Modifying
    @Query("""
    UPDATE Joueur j
    SET j.point = (
        SELECT CASE 
            WHEN classement.rang = 1 THEN :#{T(com.genesis.api.statmanager.model.enumeration.PointJoueur).FIRST.getValue()}
            WHEN classement.rang = 2 THEN :#{T(com.genesis.api.statmanager.model.enumeration.PointJoueur).SECOND.getValue()}
            WHEN classement.rang = 3 THEN :#{T(com.genesis.api.statmanager.model.enumeration.PointJoueur).THIRD.getValue()}
            WHEN classement.rang = 4 OR classement.rang = 5 THEN :#{T(com.genesis.api.statmanager.model.enumeration.PointJoueur).MOTION.getValue()}
            ELSE :#{T(com.genesis.api.statmanager.model.enumeration.PointJoueur).NULL.getValue()}
        END
        FROM (
            SELECT s.id AS joueurId,
                   RANK() OVER (ORDER BY (
                        (s.butsChamp * 4) + 
                        (s.passesChamp * 3) + 
                        ((s.butsChamp + s.passesChamp) / GREATEST(s.minutesJoueesChamp, 1) * 5) + 
                        (s.cleanSheet * 5) + 
                        (s.minutesJoueesChamp * 0.5)
                   ) DESC) AS rang
            FROM StatChampionnat s
        ) AS classement
        WHERE classement.joueurId = j.jid
    )
    WHERE EXISTS (
        SELECT 1 FROM StatChampionnat s WHERE s.id = j.jid
    )
""")
    void calculerPointsFinChampionnat();

    @Query("""
    SELECT new com.genesis.api.statmanager.dto.global.StatistiquesDTO(
        j.jid, j.nom, TYPE(j), j.poste, 
        j.totalButs, j.totalPasses, j.totalMoyenneCote, j.point, j.totalMinutesJouees
    )
    FROM Joueur j
""")
    List<StatistiquesDTO> getAllJoueursStats();



}
