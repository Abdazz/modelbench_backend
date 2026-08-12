package com.example.modelbench.service;

import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.ExperimentationMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.impl.ExperimentationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentationServiceImplTest {

    @Mock
    private ExperimentationRepository depot;

    @Mock
    private DatasetRepository depotDatasets;

    @Mock
    private ModeleMLRepository depotModeles;

    @Spy
    private ExperimentationMapper mapper = new ExperimentationMapper();

    @InjectMocks
    private ExperimentationServiceImpl service;

    private Dataset dataset;
    private ModeleML modele;
    private ExperimentationRequest requete;

    @BeforeEach
    void preparer() {
        dataset = new Dataset();
        dataset.setId(1L);
        dataset.setNom("MNIST");

        modele = new ModeleML();
        modele.setId(2L);
        modele.setNom("ResNet-50");

        requete = new ExperimentationRequest(1L, 2L, 0.98, 0.97, 7245L,
                LocalDateTime.of(2026, 5, 1, 10, 30));
    }

    @Test
    void creeUneExperimentationEnResolvantSesDeuxRelations() {
        when(depotDatasets.findById(1L)).thenReturn(Optional.of(dataset));
        when(depotModeles.findById(2L)).thenReturn(Optional.of(modele));
        when(depot.save(any(Experimentation.class))).thenAnswer(invocation -> {
            Experimentation aEnregistrer = invocation.getArgument(0);
            aEnregistrer.setId(9L);
            return aEnregistrer;
        });

        ExperimentationResponse reponse = service.creer(requete);

        assertThat(reponse.id()).isEqualTo(9L);
        assertThat(reponse.datasetNom()).isEqualTo("MNIST");
        assertThat(reponse.modeleNom()).isEqualTo("ResNet-50");
        assertThat(reponse.accuracy()).isEqualTo(0.98);
    }

    @Test
    void renvoieUne404LisibleQuandLeDatasetReferenceNExistePas() {
        when(depotDatasets.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(requete))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dataset")
                .hasMessageContaining("1");

        verify(depot, never()).save(any());
    }

    @Test
    void renvoieUne404LisibleQuandLeModeleReferenceNExistePas() {
        when(depotDatasets.findById(1L)).thenReturn(Optional.of(dataset));
        when(depotModeles.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(requete))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modele");

        verify(depot, never()).save(any());
    }

    @Test
    void leveUneExceptionQuandLExperimentationDemandeeNExistePas() {
        when(depot.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void modifieUneExperimentationExistante() {
        Experimentation existante = new Experimentation();
        existante.setId(9L);
        existante.setDataset(dataset);
        existante.setModele(modele);

        when(depot.findById(9L)).thenReturn(Optional.of(existante));
        when(depotDatasets.findById(1L)).thenReturn(Optional.of(dataset));
        when(depotModeles.findById(2L)).thenReturn(Optional.of(modele));
        when(depot.save(any(Experimentation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExperimentationResponse reponse = service.modifier(9L, new ExperimentationRequest(
                1L, 2L, 0.75, 0.70, 120L, LocalDateTime.of(2026, 6, 1, 8, 0)));

        assertThat(reponse.accuracy()).isEqualTo(0.75);
        assertThat(reponse.dureeEntrainement()).isEqualTo(120L);
    }

    @Test
    void supprimeToujoursUneExperimentationCarRienNeLaReference() {
        Experimentation existante = new Experimentation();
        existante.setId(9L);
        existante.setDataset(dataset);
        existante.setModele(modele);
        lenient().when(depot.findById(9L)).thenReturn(Optional.of(existante));

        service.supprimer(9L);

        verify(depot).delete(existante);
    }
}
