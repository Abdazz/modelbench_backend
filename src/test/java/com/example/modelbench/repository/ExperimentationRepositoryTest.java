package com.example.modelbench.repository;

import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.entity.enums.TypeModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ExperimentationRepositoryTest {

    @Autowired
    private ExperimentationRepository depot;

    @Autowired
    private DatasetRepository depotDatasets;

    @Autowired
    private ModeleMLRepository depotModeles;

    private Dataset dataset;
    private ModeleML modele;

    @BeforeEach
    void preparerLesReferences() {
        Dataset nouveauDataset = new Dataset();
        nouveauDataset.setNom("MNIST");
        nouveauDataset.setSource("Kaggle");
        nouveauDataset.setNombreObservations(70000L);
        nouveauDataset.setFormat(FormatDataset.IMAGES);
        dataset = depotDatasets.saveAndFlush(nouveauDataset);

        ModeleML nouveauModele = new ModeleML();
        nouveauModele.setNom("ResNet-50");
        nouveauModele.setType(TypeModele.VISION);
        nouveauModele.setAlgorithme("CNN");
        nouveauModele.setVersion("1.0");
        modele = depotModeles.saveAndFlush(nouveauModele);
    }

    private Experimentation uneExperimentation(double accuracy) {
        Experimentation experimentation = new Experimentation();
        experimentation.setDataset(dataset);
        experimentation.setModele(modele);
        experimentation.setAccuracy(accuracy);
        experimentation.setF1Score(0.91);
        experimentation.setDureeEntrainement(7245L);
        experimentation.setDateExecution(LocalDateTime.now().minusDays(1));
        return experimentation;
    }

    @Test
    void enregistreUneExperimentationRattacheeAUnDatasetEtUnModele() {
        Experimentation enregistree = depot.saveAndFlush(uneExperimentation(0.98));

        assertThat(enregistree.getId()).isNotNull();
        assertThat(enregistree.getDataset().getNom()).isEqualTo("MNIST");
        assertThat(enregistree.getModele().getNom()).isEqualTo("ResNet-50");
        assertThat(enregistree.getDureeEntrainement()).isEqualTo(7245L);
    }

    @Test
    void refuseUneExperimentationSansDataset() {
        Experimentation orpheline = uneExperimentation(0.98);
        orpheline.setDataset(null);

        assertThatThrownBy(() -> depot.saveAndFlush(orpheline))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void refuseUneExperimentationSansModele() {
        Experimentation orpheline = uneExperimentation(0.98);
        orpheline.setModele(null);

        assertThatThrownBy(() -> depot.saveAndFlush(orpheline))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void compteLesExperimentationsParDatasetEtParModele() {
        depot.saveAndFlush(uneExperimentation(0.98));
        depot.saveAndFlush(uneExperimentation(0.95));

        assertThat(depot.countByDatasetId(dataset.getId())).isEqualTo(2);
        assertThat(depot.countByModeleId(modele.getId())).isEqualTo(2);
        assertThat(depot.countByDatasetId(999L)).isZero();
    }

    @Test
    void chargeLesExperimentationsTrieesParAccuracyAvecLeursRelations() {
        depot.saveAndFlush(uneExperimentation(0.72));
        depot.saveAndFlush(uneExperimentation(0.98));
        depot.saveAndFlush(uneExperimentation(0.85));

        List<Experimentation> triees = depot.chargerToutesTrieesParAccuracy();

        assertThat(triees).extracting(Experimentation::getAccuracy)
                .containsExactly(0.98, 0.85, 0.72);
        assertThat(triees).allSatisfy(experimentation -> {
            assertThat(experimentation.getDataset().getNom()).isEqualTo("MNIST");
            assertThat(experimentation.getModele().getNom()).isEqualTo("ResNet-50");
        });
    }
}
