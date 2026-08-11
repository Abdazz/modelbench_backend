package com.example.modelbench.entity.enums;

/**
 * Formats de stockage possibles pour un jeu de donnees.
 */
public enum FormatDataset {

    CSV("CSV"),
    JSON("JSON"),
    IMAGES("Images"),
    PARQUET("Parquet"),
    TEXTE("Texte brut"),
    AUDIO("Audio");

    private final String libelle;

    FormatDataset(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
