package com.example.modelbench.repository;

import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.enums.FormatDataset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class DatasetRepositoryTest {

    @Autowired
    private DatasetRepository depot;

    private Dataset unDataset(String nom) {
        Dataset dataset = new Dataset();
        dataset.setNom(nom);
        dataset.setDescription("Jeu de donnees de test");
        dataset.setSource("Kaggle");
        dataset.setNombreObservations(70000L);
        dataset.setFormat(FormatDataset.CSV);
        return dataset;
    }

    @Test
    void enregistreUnDatasetEtRenseigneLaDateDAjout() {
        Dataset enregistre = depot.save(unDataset("MNIST"));

        assertThat(enregistre.getId()).isNotNull();
        assertThat(enregistre.getDateAjout()).isNotNull();
        assertThat(enregistre.getFormat()).isEqualTo(FormatDataset.CSV);
    }

    @Test
    void refuseDeuxDatasetsDeMemeNom() {
        depot.saveAndFlush(unDataset("MNIST"));

        assertThatThrownBy(() -> depot.saveAndFlush(unDataset("MNIST")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detecteLExistenceDUnNomSansTenirCompteDeLaCasse() {
        depot.saveAndFlush(unDataset("MNIST"));

        assertThat(depot.existsByNomIgnoreCase("mnist")).isTrue();
        assertThat(depot.existsByNomIgnoreCase("Titanic")).isFalse();
    }

    @Test
    void ignoreLaLigneCouranteLorsDUnControleDUniciteEnModification() {
        Dataset existant = depot.saveAndFlush(unDataset("MNIST"));

        assertThat(depot.existsByNomIgnoreCaseAndIdNot("MNIST", existant.getId())).isFalse();
        assertThat(depot.existsByNomIgnoreCaseAndIdNot("MNIST", 999L)).isTrue();
    }
}
