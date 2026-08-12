package com.example.modelbench.entity.enums;

/**
 * Roles applicatifs. ADMIN peut ecrire, CHERCHEUR est en lecture seule.
 */
public enum Role {

    ADMIN("Administrateur"),
    CHERCHEUR("Chercheur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
