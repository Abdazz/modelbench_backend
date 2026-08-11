package com.example.modelbench.repository;

import com.example.modelbench.entity.Experimentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExperimentationRepository
        extends JpaRepository<Experimentation, Long>, JpaSpecificationExecutor<Experimentation> {

    long countByDatasetId(Long datasetId);

    long countByModeleId(Long modeleId);

    /**
     * Charge toutes les experimentations triees par accuracy decroissante, avec leur dataset et leur
     * modele deja resolus. Le {@code join fetch} evite le probleme du N plus 1 lorsque l'appelant
     * lit le nom des entites liees.
     */
    @Query("""
            select e from Experimentation e
            join fetch e.dataset
            join fetch e.modele
            order by e.accuracy desc
            """)
    List<Experimentation> chargerToutesTrieesParAccuracy();
}
