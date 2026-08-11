package com.example.modelbench.repository;

import com.example.modelbench.entity.ModeleML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ModeleMLRepository extends JpaRepository<ModeleML, Long>, JpaSpecificationExecutor<ModeleML> {

    boolean existsByNomIgnoreCaseAndVersion(String nom, String version);

    boolean existsByNomIgnoreCaseAndVersionAndIdNot(String nom, String version, Long id);
}
