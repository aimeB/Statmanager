package com.genesis.api.statmanager.repository;

import com.genesis.api.statmanager.model.FeuilleDeMatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FeuilleDeMatchRepositoryImpl implements FeuilleDeMatchRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager; // ✅ Injection correcte de l'EntityManager

    @Override
    @Transactional
    public void refresh(Long feuilleId) {
        FeuilleDeMatch feuille = entityManager.find(FeuilleDeMatch.class, feuilleId);
        if (feuille != null) {
            entityManager.refresh(feuille); // ✅ Forcer la mise à jour depuis la base
        }
    }
}
