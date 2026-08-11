package com.example.modelbench.repository;

import com.example.modelbench.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DatasetRepository extends JpaRepository<Dataset, Long>, JpaSpecificationExecutor<Dataset> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);
}
