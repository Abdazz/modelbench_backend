package com.example.modelbench.specification;

import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.TypeModele;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteres de filtrage dynamiques appliques aux modeles.
 */
public final class ModeleMLSpecifications {

    private ModeleMLSpecifications() {
    }

    /**
     * @param recherche fragment cherche dans le nom ou l'algorithme, insensible a la casse
     * @param type      famille de tache exacte attendue
     */
    public static Specification<ModeleML> filtrer(String recherche, TypeModele type) {
        return (racine, requete, constructeur) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (recherche != null && !recherche.isBlank()) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicats.add(constructeur.or(
                        constructeur.like(constructeur.lower(racine.get("nom")), motif),
                        constructeur.like(constructeur.lower(racine.get("algorithme")), motif)));
            }

            if (type != null) {
                predicats.add(constructeur.equal(racine.get("type"), type));
            }

            return constructeur.and(predicats.toArray(new Predicate[0]));
        };
    }
}
