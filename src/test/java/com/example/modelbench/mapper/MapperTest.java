package com.example.modelbench.mapper;

import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.entity.enums.TypeModele;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final DatasetMapper mapperDataset = new DatasetMapper();
    private final ModeleMLMapper mapperModele = new ModeleMLMapper();
    private final ExperimentationMapper mapperExperimentation = new ExperimentationMapper();

    @Test
    void convertitUneRequeteDatasetEnEntite() {
        DatasetRequest requete = new DatasetRequest(
                "MNIST", "Chiffres manuscrits", "Kaggle", 70000L, FormatDataset.IMAGES);

        Dataset entite = mapperDataset.versEntite(requete);

        assertThat(entite.getNom()).isEqualTo("MNIST");
        assertThat(entite.getNombreObservations()).isEqualTo(70000L);
        assertThat(entite.getFormat()).isEqualTo(FormatDataset.IMAGES);
        assertThat(entite.getId()).isNull();
    }

    @Test
    void convertitUneEntiteDatasetEnReponse() {
        Dataset entite = new Dataset();
        entite.setId(7L);
        entite.setNom("Titanic");
        entite.setSource("Kaggle");
        entite.setNombreObservations(891L);
        entite.setFormat(FormatDataset.CSV);
        entite.setDateAjout(LocalDate.of(2026, 3, 14));

        DatasetResponse reponse = mapperDataset.versReponse(entite);

        assertThat(reponse.id()).isEqualTo(7L);
        assertThat(reponse.nom()).isEqualTo("Titanic");
        assertThat(reponse.dateAjout()).isEqualTo(LocalDate.of(2026, 3, 14));
    }

    @Test
    void metAJourUnDatasetSansToucherALIdentifiantNiALaDateDAjout() {
        Dataset existant = new Dataset();
        existant.setId(7L);
        existant.setNom("Ancien nom");
        existant.setSource("Ancienne source");
        existant.setNombreObservations(1L);
        existant.setFormat(FormatDataset.CSV);
        existant.setDateAjout(LocalDate.of(2026, 1, 1));

        mapperDataset.mettreAJour(existant, new DatasetRequest(
                "Nouveau nom", "Description", "UCI", 500L, FormatDataset.JSON));

        assertThat(existant.getId()).isEqualTo(7L);
        assertThat(existant.getDateAjout()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(existant.getNom()).isEqualTo("Nouveau nom");
        assertThat(existant.getFormat()).isEqualTo(FormatDataset.JSON);
    }

    @Test
    void convertitUnModeleDansLesDeuxSens() {
        ModeleML entite = mapperModele.versEntite(new ModeleMLRequest(
                "ResNet-50", TypeModele.VISION, "CNN", "1.2"));
        entite.setId(3L);
        entite.setDateCreation(LocalDate.of(2026, 2, 2));

        ModeleMLResponse reponse = mapperModele.versReponse(entite);

        assertThat(reponse.id()).isEqualTo(3L);
        assertThat(reponse.type()).isEqualTo(TypeModele.VISION);
        assertThat(reponse.algorithme()).isEqualTo("CNN");
        assertThat(reponse.version()).isEqualTo("1.2");
    }

    @Test
    void aplatitLesRelationsDUneExperimentationDansLaReponse() {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setNom("MNIST");

        ModeleML modele = new ModeleML();
        modele.setId(2L);
        modele.setNom("ResNet-50");

        Experimentation experimentation = new Experimentation();
        experimentation.setId(9L);
        experimentation.setDataset(dataset);
        experimentation.setModele(modele);
        experimentation.setAccuracy(0.98);
        experimentation.setF1Score(0.97);
        experimentation.setDureeEntrainement(7245L);
        experimentation.setDateExecution(LocalDateTime.of(2026, 5, 1, 10, 30));

        ExperimentationResponse reponse = mapperExperimentation.versReponse(experimentation);

        assertThat(reponse.id()).isEqualTo(9L);
        assertThat(reponse.datasetId()).isEqualTo(1L);
        assertThat(reponse.datasetNom()).isEqualTo("MNIST");
        assertThat(reponse.modeleId()).isEqualTo(2L);
        assertThat(reponse.modeleNom()).isEqualTo("ResNet-50");
        assertThat(reponse.dureeEntrainement()).isEqualTo(7245L);
    }

    @Test
    void appliqueUneRequeteExperimentationAvecSesDeuxRelations() {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        ModeleML modele = new ModeleML();
        modele.setId(2L);
        Experimentation cible = new Experimentation();

        mapperExperimentation.appliquer(cible, new ExperimentationRequest(
                1L, 2L, 0.88, 0.85, 3600L, LocalDateTime.of(2026, 6, 1, 9, 0)), dataset, modele);

        assertThat(cible.getDataset()).isSameAs(dataset);
        assertThat(cible.getModele()).isSameAs(modele);
        assertThat(cible.getAccuracy()).isEqualTo(0.88);
        assertThat(cible.getDateExecution()).isEqualTo(LocalDateTime.of(2026, 6, 1, 9, 0));
    }
}
