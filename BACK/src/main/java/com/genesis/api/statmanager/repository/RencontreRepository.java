package com.genesis.api.statmanager.repository;

import com.genesis.api.statmanager.dto.rencontre.RencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.RencontreLightDTO;
import com.genesis.api.statmanager.model.Rencontre;
import com.genesis.api.statmanager.model.Championnat;
import com.genesis.api.statmanager.model.enumeration.StatutRencontre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RencontreRepository extends JpaRepository<Rencontre, Long> {






    @Query("""
    SELECT new com.genesis.api.statmanager.dto.rencontre.RencontreDTO(
        r.rid, r.championnat.idChamp,
        CAST(COALESCE(SUM(f.buts), 0) AS int),
        r.nomAdversaire, r.butAdversaire, r.divisionAdversaire,
        COALESCE(h.nom, 'Aucun'), r.statutRencontre
    )
    FROM Rencontre r
    LEFT JOIN r.feuilleDeMatchs f
    LEFT JOIN r.hommeDuMatch h
    WHERE r.rid = :idRencontre
    GROUP BY r.rid, r.championnat.idChamp, r.nomAdversaire, r.butAdversaire, 
             r.divisionAdversaire, h.nom, r.statutRencontre
""")
    Optional<RencontreDTO> findRencontreDTOById(@Param("idRencontre") Long idRencontre);




    @Query("""
    SELECT new com.genesis.api.statmanager.dto.rencontre.RencontreDTO(
        r.rid,
        r.championnat.idChamp,
        (SELECT COALESCE(SUM(f.buts), 0) FROM FeuilleDeMatch f WHERE f.rencontre.rid = r.rid), 
        r.nomAdversaire,
        r.butAdversaire,
        r.divisionAdversaire,
        (CASE WHEN r.hommeDuMatch IS NOT NULL THEN r.hommeDuMatch.nom ELSE 'Aucun' END),
        r.statutRencontre
    )
    FROM Rencontre r 
    WHERE r.championnat.idChamp = :idChampionnat
""")
    List<RencontreDTO> findRencontresByChampionnat(@Param("idChampionnat") Long idChampionnat);



    /**
     * ✅ Récupère les rencontres en fonction de leur statut.
     */
    List<Rencontre> findByStatutRencontre(StatutRencontre statut);




    /**
     * ✅ Récupère la dernière rencontre terminée d'un championnat.
     */
    @Query("SELECT r FROM Rencontre r WHERE r.championnat.idChamp = :idChampionnat AND r.statutRencontre = com.genesis.api.statmanager.model.enumeration.StatutRencontre.TERMINE ORDER BY r.rid DESC")
    Optional<Rencontre> findDerniereRencontreJouee(@Param("idChampionnat") Long idChampionnat);


    @Query("""
    SELECT new com.genesis.api.statmanager.dto.rencontre.RencontreLightDTO(
        r.rid, 
        r.nomAdversaire, 
        r.divisionAdversaire, 
        r.butAdversaire, 
        r.statutRencontre
    )
    FROM Rencontre r
    WHERE r.championnat.idChamp = :idChampionnat
""")
    List<RencontreLightDTO> findRencontresLightByChampionnat(@Param("idChampionnat") Long idChampionnat);


    @Query("SELECT r FROM Rencontre r WHERE r.championnat.idChamp = :idChampionnat")
    List<Rencontre> findAllRencontresByChampionnat(@Param("idChampionnat") Long idChampionnat);



    /**
     * ✅ Récupérer les rencontres d'un championnat en filtrant par statut.
     */
    @Query("SELECT r FROM Rencontre r WHERE r.championnat = :championnat AND r.statutRencontre = :statut")
    List<Rencontre> findByChampionnatAndStatut(@Param("championnat") Championnat championnat, @Param("statut") StatutRencontre statut);

    /**
     * ✅ Récupère une rencontre avec ses joueurs associés en évitant l'instanciation directe de la classe abstraite Joueur.
     */
    @Query("""
    SELECT r FROM Rencontre r
    LEFT JOIN FETCH r.feuilleDeMatchs f
    LEFT JOIN FETCH f.joueur j
    WHERE r.rid = :id AND TYPE(j) IN (Attaquant, Gardien, Defenseur, Milieu)
""")
    Optional<Rencontre> findByIdWithJoueurs(@Param("id") Long id);



    /**
     * ✅ Compte le nombre de rencontres jouées dans un championnat.
     */
    long countByChampionnat(Championnat championnat);
}
