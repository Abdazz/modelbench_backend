package com.example.modelbench.bootstrap;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Remplit la base au premier demarrage pour que l'application soit demontrable immediatement.
 * Inactif sous le profil test, ou chaque test construit ses propres donnees.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger JOURNAL = LoggerFactory.getLogger(DataSeeder.class);

    private final UtilisateurRepository depotUtilisateurs;
    private final DatasetRepository depotDatasets;
    private final ModeleMLRepository depotModeles;
    private final ExperimentationRepository depotExperimentations;
    private final PasswordEncoder encodeur;

    public DataSeeder(UtilisateurRepository depotUtilisateurs,
                      DatasetRepository depotDatasets,
                      ModeleMLRepository depotModeles,
                      ExperimentationRepository depotExperimentations,
                      PasswordEncoder encodeur) {
        this.depotUtilisateurs = depotUtilisateurs;
        this.depotDatasets = depotDatasets;
        this.depotModeles = depotModeles;
        this.depotExperimentations = depotExperimentations;
        this.encodeur = encodeur;
    }

    @Override
    @Transactional
    public void run(String... arguments) {
        creerLesComptes();
        creerLeCatalogue();
    }

    private void creerLesComptes() {
        if (depotUtilisateurs.count() > 0) {
            return;
        }

        depotUtilisateurs.save(compte("admin", "admin123", "Administrateur du laboratoire",
                Role.ADMIN));
        depotUtilisateurs.save(compte("chercheur", "chercheur123", "Chercheur invite",
                Role.CHERCHEUR));

        JOURNAL.info("Comptes de demonstration crees : admin/admin123 et chercheur/chercheur123");
    }

    private Utilisateur compte(String login, String motDePasse, String nomComplet, Role role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(encodeur.encode(motDePasse));
        utilisateur.setNomComplet(nomComplet);
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        return utilisateur;
    }

    private void creerLeCatalogue() {
        if (depotDatasets.count() > 0) {
            return;
        }

        List<Dataset> datasets = depotDatasets.saveAll(List.of(
                dataset("MNIST", "Chiffres manuscrits en niveaux de gris, 28 par 28 pixels",
                        "Kaggle", 70000L, FormatDataset.IMAGES),
                dataset("Titanic", "Passagers du Titanic et leur survie",
                        "Kaggle", 891L, FormatDataset.CSV),
                dataset("IMDB Reviews", "Critiques de films annotees en sentiment",
                        "Stanford AI Lab", 50000L, FormatDataset.TEXTE),
                dataset("CIFAR-10", "Images couleur reparties en dix categories",
                        "University of Toronto", 60000L, FormatDataset.IMAGES),
                dataset("Boston Housing", "Prix de l'immobilier et variables de quartier",
                        "UCI Machine Learning Repository", 506L, FormatDataset.CSV),
                dataset("Iris", "Mesures morphologiques de trois especes d'iris",
                        "UCI Machine Learning Repository", 150L, FormatDataset.CSV),
                dataset("LibriSpeech", "Enregistrements de lecture a voix haute transcrits",
                        "OpenSLR", 292000L, FormatDataset.AUDIO),
                dataset("Web Traffic", "Series temporelles de frequentation de pages",
                        "Interne au laboratoire", 145000L, FormatDataset.PARQUET)));

        List<ModeleML> modeles = depotModeles.saveAll(List.of(
                modele("ResNet-50", TypeModele.VISION, "Reseau de neurones convolutif", "1.0"),
                modele("ResNet-50", TypeModele.VISION, "Reseau de neurones convolutif", "2.1"),
                modele("Foret aleatoire", TypeModele.CLASSIFICATION, "Random Forest", "1.0"),
                modele("XGBoost", TypeModele.CLASSIFICATION, "Gradient Boosting", "1.7"),
                modele("Regression lineaire", TypeModele.REGRESSION, "Moindres carres", "1.0"),
                modele("K-Means", TypeModele.CLUSTERING, "K-Means", "1.0"),
                modele("BERT", TypeModele.NLP, "Transformer bidirectionnel", "1.0"),
                modele("ACP", TypeModele.REDUCTION_DIMENSION, "Analyse en composantes principales",
                        "1.0")));

        // Graine fixe : le jeu de demonstration est identique a chaque installation, ce qui rend
        // les captures d'ecran et la demonstration reproductibles.
        Random tirage = new Random(20260811L);
        List<Experimentation> experimentations = new ArrayList<>();

        for (int index = 0; index < 24; index++) {
            Dataset dataset = datasets.get(index % datasets.size());
            ModeleML modele = modeles.get((index * 3) % modeles.size());

            double accuracy = arrondir(0.62 + tirage.nextDouble() * 0.36);
            double f1Score = arrondir(Math.max(0d, accuracy - tirage.nextDouble() * 0.06));

            Experimentation experimentation = new Experimentation();
            experimentation.setDataset(dataset);
            experimentation.setModele(modele);
            experimentation.setAccuracy(accuracy);
            experimentation.setF1Score(f1Score);
            experimentation.setDureeEntrainement(120L + tirage.nextInt(14400));
            experimentation.setDateExecution(
                    LocalDateTime.now().minusDays(tirage.nextInt(90)).withNano(0));
            experimentations.add(experimentation);
        }

        depotExperimentations.saveAll(experimentations);

        JOURNAL.info("Catalogue de demonstration cree : {} datasets, {} modeles, {} experimentations",
                datasets.size(), modeles.size(), experimentations.size());
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 10000d) / 10000d;
    }

    private Dataset dataset(String nom, String description, String source, Long observations,
                            FormatDataset format) {
        Dataset dataset = new Dataset();
        dataset.setNom(nom);
        dataset.setDescription(description);
        dataset.setSource(source);
        dataset.setNombreObservations(observations);
        dataset.setFormat(format);
        return dataset;
    }

    private ModeleML modele(String nom, TypeModele type, String algorithme, String version) {
        ModeleML modele = new ModeleML();
        modele.setNom(nom);
        modele.setType(type);
        modele.setAlgorithme(algorithme);
        modele.setVersion(version);
        return modele;
    }
}
