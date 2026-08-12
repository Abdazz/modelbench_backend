package com.example.modelbench.service;

import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.TypeModele;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.ModeleMLMapper;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.impl.ModeleMLServiceImpl;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModeleMLServiceImplTest {

    @Mock
    private ModeleMLRepository depot;

    @Mock
    private ExperimentationRepository depotExperimentations;

    @Spy
    private ModeleMLMapper mapper = new ModeleMLMapper();

    @InjectMocks
    private ModeleMLServiceImpl service;

    private final ModeleMLRequest requete =
            new ModeleMLRequest("ResNet-50", TypeModele.VISION, "CNN", "1.0");

    private ModeleML unModelePersiste() {
        ModeleML modele = new ModeleML();
        modele.setId(1L);
        modele.setNom("ResNet-50");
        modele.setType(TypeModele.VISION);
        modele.setAlgorithme("CNN");
        modele.setVersion("1.0");
        return modele;
    }

    @Test
    void creeUnModeleLorsqueLeCoupleNomEtVersionEstLibre() {
        when(depot.existsByNomIgnoreCaseAndVersion("ResNet-50", "1.0")).thenReturn(false);
        when(depot.save(any(ModeleML.class))).thenAnswer(invocation -> {
            ModeleML aEnregistrer = invocation.getArgument(0);
            aEnregistrer.setId(1L);
            return aEnregistrer;
        });

        ModeleMLResponse reponse = service.creer(requete);

        assertThat(reponse.id()).isEqualTo(1L);
        assertThat(reponse.version()).isEqualTo("1.0");
    }

    @Test
    void refuseLaCreationSiLeCoupleNomEtVersionExisteDeja() {
        when(depot.existsByNomIgnoreCaseAndVersion("ResNet-50", "1.0")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(requete))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1.0");

        verify(depot, never()).save(any());
    }

    @Test
    void leveUneExceptionQuandLeModeleDemandeNExistePas() {
        when(depot.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void modifieUnModeleExistant() {
        when(depot.findById(1L)).thenReturn(Optional.of(unModelePersiste()));
        when(depot.existsByNomIgnoreCaseAndVersionAndIdNot("ResNet-50", "2.0", 1L))
                .thenReturn(false);
        when(depot.save(any(ModeleML.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModeleMLResponse reponse = service.modifier(1L,
                new ModeleMLRequest("ResNet-50", TypeModele.VISION, "CNN profond", "2.0"));

        assertThat(reponse.version()).isEqualTo("2.0");
        assertThat(reponse.algorithme()).isEqualTo("CNN profond");
    }

    @Test
    void supprimeUnModeleQuAucuneExperimentationNeReference() {
        when(depot.findById(1L)).thenReturn(Optional.of(unModelePersiste()));
        when(depotExperimentations.countByModeleId(1L)).thenReturn(0L);

        service.supprimer(1L);

        verify(depot, times(1)).delete((ModeleML) any());
    }

    @Test
    void refuseDeSupprimerUnModeleReferenceParDesExperimentations() {
        when(depot.findById(1L)).thenReturn(Optional.of(unModelePersiste()));
        when(depotExperimentations.countByModeleId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> service.supprimer(1L))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("5");

        verify(depot, never()).delete((ModeleML) any());
    }
}
