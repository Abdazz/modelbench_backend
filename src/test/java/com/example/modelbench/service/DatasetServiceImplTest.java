package com.example.modelbench.service;

import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.DatasetMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.service.impl.DatasetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetServiceImplTest {

    @Mock
    private DatasetRepository depot;

    @Mock
    private ExperimentationRepository depotExperimentations;

    @Spy
    private DatasetMapper mapper = new DatasetMapper();

    @InjectMocks
    private DatasetServiceImpl service;

    private DatasetRequest requete;

    @BeforeEach
    void preparer() {
        requete = new DatasetRequest("MNIST", "Chiffres", "Kaggle", 70000L, FormatDataset.IMAGES);
    }

    private Dataset unDatasetPersiste() {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setNom("MNIST");
        dataset.setSource("Kaggle");
        dataset.setNombreObservations(70000L);
        dataset.setFormat(FormatDataset.IMAGES);
        return dataset;
    }

    @Test
    void creeUnDatasetLorsqueLeNomEstLibre() {
        when(depot.existsByNomIgnoreCase("MNIST")).thenReturn(false);
        when(depot.save(any(Dataset.class))).thenAnswer(invocation -> {
            Dataset aEnregistrer = invocation.getArgument(0);
            aEnregistrer.setId(1L);
            return aEnregistrer;
        });

        DatasetResponse reponse = service.creer(requete);

        assertThat(reponse.id()).isEqualTo(1L);
        assertThat(reponse.nom()).isEqualTo("MNIST");
    }

    @Test
    void refuseLaCreationSiLeNomExisteDeja() {
        when(depot.existsByNomIgnoreCase("MNIST")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(requete))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("MNIST");

        verify(depot, never()).save(any());
    }

    @Test
    void leveUneExceptionQuandLeDatasetDemandeNExistePas() {
        when(depot.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void modifieUnDatasetExistant() {
        when(depot.findById(1L)).thenReturn(Optional.of(unDatasetPersiste()));
        when(depot.existsByNomIgnoreCaseAndIdNot("Titanic", 1L)).thenReturn(false);
        when(depot.save(any(Dataset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DatasetResponse reponse = service.modifier(1L, new DatasetRequest(
                "Titanic", "Passagers", "Kaggle", 891L, FormatDataset.CSV));

        assertThat(reponse.nom()).isEqualTo("Titanic");
        assertThat(reponse.format()).isEqualTo(FormatDataset.CSV);
    }

    @Test
    void refuseLaModificationSiLeNouveauNomAppartientADunAutreDataset() {
        when(depot.findById(1L)).thenReturn(Optional.of(unDatasetPersiste()));
        when(depot.existsByNomIgnoreCaseAndIdNot("Titanic", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.modifier(1L, new DatasetRequest(
                "Titanic", null, "Kaggle", 891L, FormatDataset.CSV)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void supprimeUnDatasetQuAucuneExperimentationNeReference() {
        Dataset dataset = unDatasetPersiste();
        when(depot.findById(1L)).thenReturn(Optional.of(dataset));
        when(depotExperimentations.countByDatasetId(1L)).thenReturn(0L);

        service.supprimer(1L);

        verify(depot).delete(dataset);
    }

    @Test
    void refuseDeSupprimerUnDatasetReferenceParDesExperimentations() {
        when(depot.findById(1L)).thenReturn(Optional.of(unDatasetPersiste()));
        when(depotExperimentations.countByDatasetId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> service.supprimer(1L))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("3");

        verify(depot, never()).delete(any(Dataset.class));
    }
}
