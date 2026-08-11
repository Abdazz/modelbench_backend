package com.example.modelbench.entity.enums;

/**
 * Familles de taches d'apprentissage automatique couvertes par un modele.
 */
public enum TypeModele {

    CLASSIFICATION("Classification"),
    REGRESSION("Regression"),
    CLUSTERING("Clustering"),
    REDUCTION_DIMENSION("Reduction de dimension"),
    NLP("Traitement du langage naturel"),
    VISION("Vision par ordinateur");

    private final String libelle;

    TypeModele(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
