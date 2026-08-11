package com.example.modelbench.repository;

import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.TypeModele;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ModeleMLRepositoryTest {

    @Autowired
    private ModeleMLRepository depot;

    private ModeleML unModele(String nom, String version) {
        ModeleML modele = new ModeleML();
        modele.setNom(nom);
        modele.setType(TypeModele.CLASSIFICATION);
        modele.setAlgorithme("Random Forest");
        modele.setVersion(version);
        return modele;
    }

    @Test
    void enregistreUnModeleEtRenseigneLaDateDeCreation() {
        ModeleML enregistre = depot.save(unModele("Detecteur de fraude", "1.0"));

        assertThat(enregistre.getId()).isNotNull();
        assertThat(enregistre.getDateCreation()).isNotNull();
    }

    @Test
    void autoriseLeMemeNomSurDeuxVersionsDifferentes() {
        depot.saveAndFlush(unModele("Detecteur de fraude", "1.0"));
        depot.saveAndFlush(unModele("Detecteur de fraude", "2.0"));

        assertThat(depot.count()).isEqualTo(2);
    }

    @Test
    void refuseLeMemeCoupleNomEtVersion() {
        depot.saveAndFlush(unModele("Detecteur de fraude", "1.0"));

        assertThatThrownBy(() -> depot.saveAndFlush(unModele("Detecteur de fraude", "1.0")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detecteLExistenceDUnCoupleNomEtVersion() {
        ModeleML existant = depot.saveAndFlush(unModele("Detecteur de fraude", "1.0"));

        assertThat(depot.existsByNomIgnoreCaseAndVersion("detecteur de fraude", "1.0")).isTrue();
        assertThat(depot.existsByNomIgnoreCaseAndVersion("Detecteur de fraude", "9.9")).isFalse();
        assertThat(depot.existsByNomIgnoreCaseAndVersionAndIdNot(
                "Detecteur de fraude", "1.0", existant.getId())).isFalse();
    }
}
