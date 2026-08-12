package com.example.modelbench.service;

import com.example.modelbench.dto.MeilleurModeleResponse;
import com.example.modelbench.dto.SyntheseResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.mapper.ExperimentationMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.impl.StatistiquesServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatistiquesServiceImplTest {

    @Mock
    private DatasetRepository depotDatasets;

    @Mock
    private ModeleMLRepository depotModeles;

    @Mock
    private ExperimentationRepository depotExperimentations;

    @Spy
    private ExperimentationMapper mapper = new ExperimentationMapper();

    @InjectMocks
    private StatistiquesServiceImpl service;

    private Dataset dataset(long id, String nom) {
        Dataset dataset = new Dataset();
        dataset.setId(id);
        dataset.setNom(nom);
        return dataset;
    }

    private ModeleML modele(long id, String nom) {
        ModeleML modele = new ModeleML();
        modele.setId(id);
        modele.setNom(nom);
        return modele;
    }

    private Experimentation experimentation(long id, Dataset dataset, ModeleML modele,
                                            double accuracy, double f1Score) {
        Experimentation experimentation = new Experimentation();
        experimentation.setId(id);
        experimentation.setDataset(dataset);
        experimentation.setModele(modele);
        experimentation.setAccuracy(accuracy);
        experimentation.setF1Score(f1Score);
        experimentation.setDureeEntrainement(600L);
        experimentation.setDateExecution(LocalDateTime.of(2026, 5, 1, 10, 0));
        return experimentation;
    }

    @Test
    void calculeLaSyntheseAvecLaMoyenneEtLaMeilleureExperimentation() {
        Dataset mnist = dataset(1L, "MNIST");
        ModeleML resnet = modele(1L, "ResNet-50");

        when(depotDatasets.count()).thenReturn(3L);
        when(depotModeles.count()).thenReturn(2L);
        when(depotExperimentations.chargerToutesTrieesParAccuracy()).thenReturn(List.of(
                experimentation(1L, mnist, resnet, 0.98, 0.97),
                experimentation(2L, mnist, resnet, 0.90, 0.88),
                experimentation(3L, mnist, resnet, 0.70, 0.68)));

        SyntheseResponse synthese = service.synthese();

        assertThat(synthese.nbDatasets()).isEqualTo(3);
        assertThat(synthese.nbModeles()).isEqualTo(2);
        assertThat(synthese.nbExperimentations()).isEqualTo(3);
        assertThat(synthese.accuracyMoyenne()).isEqualTo(0.86, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(synthese.meilleureExperimentation().id()).isEqualTo(1L);
    }

    @Test
    void renvoieUneSyntheseNeutreQuandIlNYAAucuneExperimentation() {
        when(depotDatasets.count()).thenReturn(0L);
        when(depotModeles.count()).thenReturn(0L);
        when(depotExperimentations.chargerToutesTrieesParAccuracy()).thenReturn(List.of());

        SyntheseResponse synthese = service.synthese();

        assertThat(synthese.nbExperimentations()).isZero();
        assertThat(synthese.accuracyMoyenne()).isNull();
        assertThat(synthese.meilleureExperimentation()).isNull();
    }

    @Test
    void neRetientQueLaMeilleureExperimentationDeChaqueDataset() {
        Dataset mnist = dataset(1L, "MNIST");
        Dataset titanic = dataset(2L, "Titanic");
        ModeleML resnet = modele(1L, "ResNet-50");
        ModeleML foret = modele(2L, "Foret aleatoire");

        lenient().when(depotExperimentations.chargerToutesTrieesParAccuracy()).thenReturn(List.of(
                experimentation(1L, mnist, resnet, 0.98, 0.97),
                experimentation(2L, titanic, foret, 0.82, 0.80),
                experimentation(3L, mnist, foret, 0.75, 0.73),
                experimentation(4L, titanic, resnet, 0.60, 0.58)));

        List<MeilleurModeleResponse> meilleurs = service.meilleursModeles();

        assertThat(meilleurs).hasSize(2);
        assertThat(meilleurs).extracting(MeilleurModeleResponse::datasetNom)
                .containsExactly("MNIST", "Titanic");
        assertThat(meilleurs).extracting(MeilleurModeleResponse::modeleNom)
                .containsExactly("ResNet-50", "Foret aleatoire");
        assertThat(meilleurs).extracting(MeilleurModeleResponse::accuracy)
                .containsExactly(0.98, 0.82);
    }
}
