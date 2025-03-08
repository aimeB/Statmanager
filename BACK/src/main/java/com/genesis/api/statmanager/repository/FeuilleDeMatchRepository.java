package com.genesis.api.statmanager.repository;

import com.genesis.api.statmanager.model.FeuilleDeMatch;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeuilleDeMatchRepository extends JpaRepository<FeuilleDeMatch, Long> , FeuilleDeMatchRepositoryCustom {



    @Query("""
    SELECT
        f.feuilleId AS id,
        f.rencontre.rid AS rid,
        f.joueur.jid AS jid,
        f.joueur.nom AS nom,
        CAST(f.joueur.poste AS string) AS poste, 
        f.buts AS buts,
        f.passes AS passes,
        f.moyenneCote AS moyenneCote,
        f.minutesJouees AS minutesJouees,
        f.aJoue AS aJoue,
        f.titulaire AS titulaire,
        f.butArreter AS butArreter,
        f.butEncaisser AS butEncaisser,
        COALESCE((SELECT GROUP_CONCAT(CAST(p.jid AS string)) FROM f.passeurs p), '') AS passeursIds,
        COALESCE((SELECT GROUP_CONCAT(p.nom) FROM f.passeurs p), '') AS nomsPasseurs
    FROM FeuilleDeMatch f
    WHERE f.rencontre.rid = :idRencontre
""")
    List<FeuilleDeMatchProjection> findFeuillesDeMatchAsProjection(@Param("idRencontre") Long idRencontre);


    @Modifying
    @Query("""
    UPDATE FeuilleDeMatch f
    SET f.passes = :passes
    WHERE f.feuilleId = :feuilleId
""")
    void updatePasses(@Param("feuilleId") Long feuilleId, @Param("passes") int passes);



    @Modifying
    @Query(value = """
    INSERT INTO passeurs_feuille_match (feuille_id, passeur_id)
    VALUES (:feuilleId, :passeurJoueurId)
""", nativeQuery = true)
    void addPasseurByIds(@Param("feuilleId") Long feuilleId, @Param("passeurJoueurId") Long passeurJoueurId);


    @Query("SELECT f.joueurId FROM FeuilleDeMatch f WHERE f.feuilleId = :feuilleId")
    Optional<Long> findJoueurIdByFeuilleId(@Param("feuilleId") Long feuilleId);


    /**
     * ✅ Récupère une feuille de match sous forme de projection sans instancier `Joueur`
     */
    @Query("""
    SELECT
        f.feuilleId AS id, f.rencontre.rid AS rid,
        f.joueur.jid AS jid, f.joueur.nom AS nom, CAST(f.joueur.poste AS string) AS poste,
        f.buts AS buts, f.passes AS passes, f.moyenneCote AS moyenneCote,
        f.minutesJouees AS minutesJouees, f.aJoue AS aJoue, f.titulaire AS titulaire,
        f.butArreter AS butArreter, f.butEncaisser AS butEncaisser,
        COALESCE((SELECT GROUP_CONCAT(CAST(p.jid AS string)) FROM f.passeurs p), '') AS passeursIds,
        COALESCE((SELECT GROUP_CONCAT(p.nom) FROM f.passeurs p), '') AS nomsPasseurs
    FROM FeuilleDeMatch f
    WHERE f.feuilleId = :idFeuilleMatch
""")
    Optional<FeuilleDeMatchProjection> findFeuilleDeMatchProjection(@Param("idFeuilleMatch") Long idFeuilleMatch);


    @Query("""
    SELECT
        f.feuilleId AS id, f.rencontre.rid AS rid, f.joueurId AS joueurId,
        f.buts AS buts, f.passes AS passes, f.moyenneCote AS moyenneCote, 
        f.minutesJouees AS minutesJouees, f.aJoue AS aJoue, f.titulaire AS titulaire,
        f.butArreter AS butArreter, f.butEncaisser AS butEncaisser
    FROM FeuilleDeMatch f
    WHERE f.rencontre.rid = :idRencontre
""")
    List<Object[]> findFeuillesDeMatchSansJoueur(@Param("idRencontre") Long idRencontre);


    @Modifying
    @Query("""
    UPDATE FeuilleDeMatch f
    SET f.buts = :buts,
        f.passes = :passes
    WHERE f.feuilleId = :feuilleId
""")
    void updateFeuilleStats(@Param("feuilleId") Long feuilleId,
                            @Param("buts") int buts,
                            @Param("passes") int passes);



    @Modifying
    @Query("""
    UPDATE FeuilleDeMatch f
    SET f.buts = :buts, f.passes = :passes, f.minutesJouees = :minutesJouees, f.aJoue = :aJoue, f.titulaire = :titulaire
    WHERE f.feuilleId = :feuilleId
""")
    void updateFeuilleStats2(
            @Param("feuilleId") Long feuilleId,
            @Param("buts") int buts,
            @Param("passes") int passes,
            @Param("minutesJouees") double minutesJouees,
            @Param("aJoue") boolean aJoue,
            @Param("titulaire") boolean titulaire
    );


    @Query(value = "SELECT buts FROM feuille_de_match WHERE feuille_id = :feuilleId", nativeQuery = true)
    int getButsFromDatabase(@Param("feuilleId") Long feuilleId);

    @Modifying
    @Query(value = "FLUSH TABLE feuille_de_match", nativeQuery = true)
    void clearCache();


    @Query("""
    SELECT DISTINCT f FROM FeuilleDeMatch f
    JOIN FETCH f.joueur j
    WHERE f.rencontre.rid = :rencontreId
    AND j.class != Joueur
    ORDER BY j.nom
""")
    List<FeuilleDeMatch> findByRencontre(@Param("rencontreId") Long rencontreId);


    @Modifying
    @Query("UPDATE FeuilleDeMatch f SET f.buts = f.buts + 1 WHERE f.feuilleId = :feuilleId")
    void incrementButs(@Param("feuilleId") Long feuilleId);

    @Modifying
    @Query("UPDATE FeuilleDeMatch f SET f.passes = f.passes + 1 WHERE f.feuilleId = :feuilleId")
    void incrementPasses(@Param("feuilleId") Long feuilleId);




    @Query("""
    SELECT f FROM FeuilleDeMatch f
    JOIN FETCH f.joueur j
    WHERE f.rencontre.rid = :rencontreId
    AND f.joueur.jid = :joueurId
    AND TYPE(f.joueur) IN (Attaquant, Gardien, Defenseur, Milieu)
""")
    List<FeuilleDeMatch> findByRencontreAndJoueur(@Param("rencontreId") Long rencontreId, @Param("joueurId") Long joueurId);


    @Query("""
    SELECT f FROM FeuilleDeMatch f
    WHERE f.rencontre.championnat.idChamp = :idChampionnat
""")
    List<FeuilleDeMatch> findByChampionnatId(@Param("idChampionnat") Long idChampionnat);


    @Query("SELECT f FROM FeuilleDeMatch f WHERE f.rencontre.rid = :idRencontre AND f.joueur.jid = :idJoueur")
    List<FeuilleDeMatch> findByRencontreAndJoueurId(@Param("idRencontre") Long idRencontre, @Param("idJoueur") Long idJoueur);


    @Modifying
    @Query("""
    UPDATE FeuilleDeMatch f
    SET f.butEncaisser = :butEncaisser,
        f.butArreter = :butArreter
    WHERE f.feuilleId = :feuilleId
""")
    void updateFeuilleGardienStats(@Param("feuilleId") Long feuilleId,
                                   @Param("butEncaisser") int butEncaisser,
                                   @Param("butArreter") int butArreter);



    @Modifying
    @Query("""
    UPDATE FeuilleDeMatch f
    SET f.buts = :buts, 
        f.passes = :passes, 
        f.moyenneCote = :moyenneCote, 
        f.minutesJouees = :minutesJouees, 
        f.butEncaisser = CASE WHEN :isGardien = true THEN :butEncaisser ELSE f.butEncaisser END, 
        f.butArreter = CASE WHEN :isGardien = true THEN :butArreter ELSE f.butArreter END
    WHERE f.feuilleId = :feuilleId
""")
    void majStatsFeuilleDeMatch(@Param("feuilleId") Long feuilleId,
                                @Param("buts") int buts,
                                @Param("passes") int passes,
                                @Param("moyenneCote") double moyenneCote,
                                @Param("minutesJouees") double minutesJouees,
                                @Param("isGardien") boolean isGardien,
                                @Param("butEncaisser") int butEncaisser,
                                @Param("butArreter") int butArreter);


    @Query("""
    SELECT b.nom AS buteur, p.nom AS passeur
    FROM FeuilleDeMatch f
    JOIN Joueur b ON f.joueurId = b.jid
    LEFT JOIN f.passeurs p
    WHERE f.rencontre.rid = :idRencontre
    ORDER BY f.feuilleId ASC
""")
    List<Object[]> findHistoriqueButs(@Param("idRencontre") Long idRencontre);






    @Query("""
    SELECT f.feuilleId AS id,
           f.joueurId AS jid,
           f.buts AS buts,
           f.passes AS passes,
           f.minutesJouees AS minutesJouees,
           j.poste AS poste,
           f.moyenneCote AS moyenneCote,
           f.butEncaisser AS butEncaisser,
           f.butArreter AS butArreter
    FROM FeuilleDeMatch f
    JOIN Joueur j ON j.jid = f.joueurId
    WHERE f.rencontre.rid = :rencontreId
""")
    List<FeuilleDeMatchProjection> findAllByRencontreId(@Param("rencontreId") Long rencontreId);


}



