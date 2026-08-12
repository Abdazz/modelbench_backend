package com.example.modelbench.specification;

import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.enums.FormatDataset;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteres de filtrage dynamiques appliques aux datasets.
 */
public final class DatasetSpecifications {

    private DatasetSpecifications() {
    }

    /**
     * @param recherche fragment cherche dans le nom ou la source, insensible a la casse
     * @param format    format exact attendu
     */
    public static Specification<Dataset> filtrer(String recherche, FormatDataset format) {
        return (racine, requete, constructeur) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (recherche != null && !recherche.isBlank()) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicats.add(constructeur.or(
                        constructeur.like(constructeur.lower(racine.get("nom")), motif),
                        constructeur.like(constructeur.lower(racine.get("source")), motif)));
            }

            if (format != null) {
                predicats.add(constructeur.equal(racine.get("format"), format));
            }

            return constructeur.and(predicats.toArray(new Predicate[0]));
        };
    }
}
