package com.example.modelbench.specification;

import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteres de filtrage dynamiques appliques aux utilisateurs.
 */
public final class UtilisateurSpecifications {

    private UtilisateurSpecifications() {
    }

    /**
     * @param recherche fragment cherche dans le nom complet ou le login, insensible a la casse
     * @param role      role exact attendu
     */
    public static Specification<Utilisateur> filtrer(String recherche, Role role) {
        return (racine, requete, constructeur) -> {
            List<Predicate> predicats = new ArrayList<>();

            if (recherche != null && !recherche.isBlank()) {
                String motif = "%" + recherche.toLowerCase() + "%";
                predicats.add(constructeur.or(
                        constructeur.like(constructeur.lower(racine.get("nomComplet")), motif),
                        constructeur.like(constructeur.lower(racine.get("login")), motif)));
            }

            if (role != null) {
                predicats.add(constructeur.equal(racine.get("role"), role));
            }

            return constructeur.and(predicats.toArray(new Predicate[0]));
        };
    }
}
