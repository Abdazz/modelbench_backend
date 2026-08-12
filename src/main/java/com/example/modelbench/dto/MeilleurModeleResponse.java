package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Meilleur modele observe sur un dataset donne.
 */
@Schema(description = "Meilleur modele par dataset")
public record MeilleurModeleResponse(
        Long datasetId,
        String datasetNom,
        Long modeleId,
        String modeleNom,
        Double accuracy,
        Double f1Score) {
}
