package com.example.modelbench.specification;

import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.entity.enums.TypeModele;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SpecificationsTest {

    @Autowired
    private DatasetRepository depotDatasets;

    @Autowired
    private ModeleMLRepository depotModeles;

    @Autowired
    private ExperimentationRepository depotExperimentations;

    @Autowired
    private UtilisateurRepository depotUtilisateurs;

    private Dataset mnist;
    private ModeleML resnet;

    private Dataset creerDataset(String nom, String source, FormatDataset format) {
        Dataset dataset = new Dataset();
        dataset.setNom(nom);
        dataset.setSource(source);
        dataset.setNombreObservations(1000L);
        dataset.setFormat(format);
        return depotDatasets.saveAndFlush(dataset);
    }

    private ModeleML creerModele(String nom, String algorithme, TypeModele type) {
        ModeleML modele = new ModeleML();
        modele.setNom(nom);
        modele.setType(type);
        modele.setAlgorithme(algorithme);
        modele.setVersion("1.0");
        return depotModeles.saveAndFlush(modele);
    }

    private void creerExperimentation(Dataset dataset, ModeleML modele, double accuracy) {
        Experimentation experimentation = new Experimentation();
        experimentation.setDataset(dataset);
        experimentation.setModele(modele);
        experimentation.setAccuracy(accuracy);
        experimentation.setF1Score(0.9);
        experimentation.setDureeEntrainement(600L);
        experimentation.setDateExecution(LocalDateTime.now().minusDays(2));
        depotExperimentations.saveAndFlush(experimentation);
    }

    private Utilisateur creerUtilisateur(String nomComplet, String login, Role role, boolean actif) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNomComplet(nomComplet);
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse("HACHE_DE_TEST");
        utilisateur.setRole(role);
        utilisateur.setActif(actif);
        return depotUtilisateurs.saveAndFlush(utilisateur);
    }

    @BeforeEach
    void preparerLesDonnees() {
        mnist = creerDataset("MNIST", "Kaggle", FormatDataset.IMAGES);
        creerDataset("Titanic", "Kaggle", FormatDataset.CSV);
        creerDataset("IMDB Reviews", "Stanford", FormatDataset.TEXTE);

        resnet = creerModele("ResNet-50", "CNN", TypeModele.VISION);
        creerModele("Foret aleatoire", "Random Forest", TypeModele.CLASSIFICATION);

        creerExperimentation(mnist, resnet, 0.98);
        creerExperimentation(mnist, resnet, 0.72);
    }

    @Test
    void sansAucunCritereLeFiltreRenvoieToutesLesLignes() {
        var page = depotDatasets.findAll(
                DatasetSpecifications.filtrer(null, null), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void filtreLesDatasetsParRechercheSurLeNomSansTenirCompteDeLaCasse() {
        var page = depotDatasets.findAll(
                DatasetSpecifications.filtrer("mnist", null), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Dataset::getNom).containsExactly("MNIST");
    }

    @Test
    void filtreLesDatasetsParRechercheSurLaSource() {
        var page = depotDatasets.findAll(
                DatasetSpecifications.filtrer("kaggle", null), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void filtreLesDatasetsParFormat() {
        var page = depotDatasets.findAll(
                DatasetSpecifications.filtrer(null, FormatDataset.CSV), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Dataset::getNom).containsExactly("Titanic");
    }

    @Test
    void combineLaRechercheEtLeFormatEnEt() {
        var page = depotDatasets.findAll(
                DatasetSpecifications.filtrer("kaggle", FormatDataset.IMAGES),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Dataset::getNom).containsExactly("MNIST");
    }

    @Test
    void filtreLesModelesParRechercheSurLAlgorithmeEtParType() {
        var parAlgorithme = depotModeles.findAll(
                ModeleMLSpecifications.filtrer("random", null), PageRequest.of(0, 10));
        var parType = depotModeles.findAll(
                ModeleMLSpecifications.filtrer(null, TypeModele.VISION), PageRequest.of(0, 10));

        assertThat(parAlgorithme.getContent()).extracting(ModeleML::getNom)
                .containsExactly("Foret aleatoire");
        assertThat(parType.getContent()).extracting(ModeleML::getNom).containsExactly("ResNet-50");
    }

    @Test
    void filtreLesExperimentationsParDatasetEtParBornesDAccuracy() {
        var parDataset = depotExperimentations.findAll(
                ExperimentationSpecifications.filtrer(null, mnist.getId(), null, null, null),
                PageRequest.of(0, 10));
        var parAccuracy = depotExperimentations.findAll(
                ExperimentationSpecifications.filtrer(null, null, null, 0.9, null),
                PageRequest.of(0, 10));

        assertThat(parDataset.getTotalElements()).isEqualTo(2);
        assertThat(parAccuracy.getContent()).extracting(Experimentation::getAccuracy)
                .containsExactly(0.98);
    }

    @Test
    void filtreLesExperimentationsParRechercheSurLeNomDuDatasetOuDuModele() {
        var parNomDataset = depotExperimentations.findAll(
                ExperimentationSpecifications.filtrer("mnist", null, null, null, null),
                PageRequest.of(0, 10));
        var parNomModele = depotExperimentations.findAll(
                ExperimentationSpecifications.filtrer("resnet", null, null, null, null),
                PageRequest.of(0, 10));
        var sansCorrespondance = depotExperimentations.findAll(
                ExperimentationSpecifications.filtrer("inexistant", null, null, null, null),
                PageRequest.of(0, 10));

        assertThat(parNomDataset.getTotalElements()).isEqualTo(2);
        assertThat(parNomModele.getTotalElements()).isEqualTo(2);
        assertThat(sansCorrespondance.getTotalElements()).isZero();
    }

    @Test
    void filtreLesUtilisateursParRechercheSurLeNomOuLeLogin() {
        creerUtilisateur("Marie Curie", "marie.curie@example.com", Role.CHERCHEUR, true);
        creerUtilisateur("Alan Turing", "alan.turing@example.com", Role.ADMIN, true);

        var parNom = depotUtilisateurs.findAll(
                UtilisateurSpecifications.filtrer("curie", null), PageRequest.of(0, 10));
        var parLogin = depotUtilisateurs.findAll(
                UtilisateurSpecifications.filtrer("turing", null), PageRequest.of(0, 10));

        assertThat(parNom.getContent()).extracting(Utilisateur::getNomComplet)
                .containsExactly("Marie Curie");
        assertThat(parLogin.getContent()).extracting(Utilisateur::getNomComplet)
                .containsExactly("Alan Turing");
    }

    @Test
    void filtreLesUtilisateursParRole() {
        creerUtilisateur("Marie Curie", "marie.curie@example.com", Role.CHERCHEUR, true);
        creerUtilisateur("Alan Turing", "alan.turing@example.com", Role.ADMIN, true);

        var page = depotUtilisateurs.findAll(
                UtilisateurSpecifications.filtrer(null, Role.ADMIN), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Utilisateur::getNomComplet)
                .containsExactly("Alan Turing");
    }

    @Test
    void detecteUnLoginDejaPrisSansTenirCompteDeLaCasse() {
        creerUtilisateur("Marie Curie", "marie.curie@example.com", Role.CHERCHEUR, true);

        assertThat(depotUtilisateurs.existsByLoginIgnoreCase("MARIE.CURIE@EXAMPLE.COM")).isTrue();
        assertThat(depotUtilisateurs.existsByLoginIgnoreCase("libre@example.com")).isFalse();
    }

    @Test
    void ignoreLIdentifiantCourantLorsDeLaDetectionDeDoublonDeLogin() {
        Utilisateur marie = creerUtilisateur(
                "Marie Curie", "marie.curie@example.com", Role.CHERCHEUR, true);

        assertThat(depotUtilisateurs.existsByLoginIgnoreCaseAndIdNot(
                "marie.curie@example.com", marie.getId())).isFalse();
        assertThat(depotUtilisateurs.existsByLoginIgnoreCaseAndIdNot(
                "marie.curie@example.com", marie.getId() + 1)).isTrue();
    }

    @Test
    void compteLesAdministrateursActifs() {
        creerUtilisateur("Admin Un", "admin1@example.com", Role.ADMIN, true);
        creerUtilisateur("Admin Deux", "admin2@example.com", Role.ADMIN, false);
        creerUtilisateur("Chercheur", "chercheur@example.com", Role.CHERCHEUR, true);

        assertThat(depotUtilisateurs.countByRoleAndActifTrue(Role.ADMIN)).isEqualTo(1L);
    }
}
