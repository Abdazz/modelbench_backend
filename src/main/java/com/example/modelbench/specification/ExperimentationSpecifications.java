package com.example.modelbench.specification;

import com.example.modelbench.entity.Experimentation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteres de filtrage dynamiques appliques aux experimentations.
 */
public final class ExperimentationSpecifications {

    private ExperimentationSpecifications() {
    }

    /**
     * @param recherche   fragment cherche dans le nom du dataset ou du modele lie
     * @param datasetId   restreint a un dataset precis
     * @param modeleId    restreint a un modele precis
     * @param accuracyMin borne inferieure incluse
     * @param accuracyMax borne superieure incluse
     */
    public static Specification<Experimentation> filtrer(String recherche, Long datasetId,
                                                         Long modeleId, Double accuracyMin,
                                                         Double accuracyMax) {
        return (racine, requete, constructeur) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (recherche != null && !recherche.isBlank()) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicats.add(constructeur.or(
                        constructeur.like(
                                constructeur.lower(racine.get("dataset").get("nom")), motif),
                        constructeur.like(
                                constructeur.lower(racine.get("modele").get("nom")), motif)));
            }

            if (datasetId != null) {
                predicats.add(constructeur.equal(racine.get("dataset").get("id"), datasetId));
            }

            if (modeleId != null) {
                predicats.add(constructeur.equal(racine.get("modele").get("id"), modeleId));
            }

            if (accuracyMin != null) {
                predicats.add(constructeur.greaterThanOrEqualTo(
                        racine.get("accuracy"), accuracyMin));
            }

            if (accuracyMax != null) {
                predicats.add(constructeur.lessThanOrEqualTo(racine.get("accuracy"), accuracyMax));
            }

            return constructeur.and(predicats.toArray(new Predicate[0]));
        };
    }
}
