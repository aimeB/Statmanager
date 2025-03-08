package com.genesis.api.statmanager.repository;

import com.genesis.api.statmanager.dto.championnat.ChampionnatLightDTO;
import com.genesis.api.statmanager.model.Championnat;
import com.genesis.api.statmanager.model.enumeration.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionnatRepository extends JpaRepository<Championnat, Long> {






    // ✅ Récupérer les 10 derniers championnats
    @Query("SELECT c FROM Championnat c LEFT JOIN FETCH c.rencontres ORDER BY c.idChamp DESC")
    List<Championnat> findTop10ByOrderByIdChampDesc();









    @Query("SELECT c FROM Championnat c LEFT JOIN FETCH c.rencontres WHERE c.idChamp = :idChamp")
    Championnat findByIdWithRencontres(@Param("idChamp") Long idChamp);























    // ✅ Récupérer tous les championnats d'une division spécifique
    @Query("""
    SELECT c FROM Championnat c
    WHERE c.division = :division
    """)
    List<Championnat> findByDivision(@Param("division") Division division);

    // ✅ Récupérer tous les championnats actifs
    @Query("""
    SELECT c FROM Championnat c
    WHERE c.statut = 'ENCOURS'
    """)
    List<Championnat> findActiveChampionnats();

    // ✅ Récupérer un championnat par son ID
    @Query("""
    SELECT c FROM Championnat c
    WHERE c.idChamp = :idChamp
    """)
    Championnat findChampionnatById(@Param("idChamp") Long idChamp);

    // ✅ Récupérer les championnats proches de la promotion
    @Query("""
    SELECT c FROM Championnat c
    WHERE c.pointsActuels BETWEEN c.pointsPromotion - 5 AND c.pointsPromotion
    """)
    List<Championnat> findCloseToPromotion();

    // ✅ Récupérer les championnats proches de la relégation
    @Query("""
    SELECT c FROM Championnat c
    WHERE c.pointsActuels BETWEEN c.pointsRelegation - 5 AND c.pointsRelegation
    """)
    List<Championnat> findCloseToRelegation();





    // ✅ Récupérer un championnat avec toutes ses rencontres et feuilles de match (sans instancier `Joueur`)
    @Query("""
    SELECT DISTINCT c FROM Championnat c
    LEFT JOIN FETCH c.rencontres r
    LEFT JOIN FETCH r.feuilleDeMatchs f
    LEFT JOIN FETCH f.joueur j
    WHERE c.idChamp = :idChamp
    AND TYPE(j) IN (Attaquant, Gardien, Defenseur, Milieu )
    """)
    Championnat findChampionnatWithRencontres(@Param("idChamp") Long idChamp);








    @Query("""
    SELECT new com.genesis.api.statmanager.dto.championnat.ChampionnatLightDTO(
        c.idChamp,
        c.division,
        c.statut, 
        c.pointsActuels, 
        CAST((SELECT COUNT(r) FROM Rencontre r WHERE r.championnat.idChamp = c.idChamp) AS integer) 
    ) 
    FROM Championnat c
    ORDER BY c.idChamp DESC
""")
    List<ChampionnatLightDTO> findTop10ChampionnatLight();









    @Modifying
    @Query("DELETE FROM Championnat")
    void clearChampionnats();
}
