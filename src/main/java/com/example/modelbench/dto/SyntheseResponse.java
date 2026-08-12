package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicateurs affiches sur le tableau de bord.
 */
@Schema(description = "Synthese chiffree du catalogue")
public record SyntheseResponse(

        @Schema(example = "8") long nbDatasets,

        @Schema(example = "8") long nbModeles,

        @Schema(example = "25") long nbExperimentations,

        @Schema(description = "Accuracy moyenne, nulle s'il n'y a aucune experimentation",
                example = "0.8642")
        Double accuracyMoyenne,

        @Schema(description = "Experimentation ayant la meilleure accuracy, nulle si aucune")
        ExperimentationResponse meilleureExperimentation) {
}
