package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Schema(description = "Donnees necessaires a la creation ou la modification d'une experimentation")
public record ExperimentationRequest(

        @NotNull(message = "Le dataset est obligatoire")
        @Schema(example = "1")
        Long datasetId,

        @NotNull(message = "Le modèle est obligatoire")
        @Schema(example = "1")
        Long modeleId,

        @NotNull(message = "L'accuracy est obligatoire")
        @DecimalMin(value = "0.0", message = "L'accuracy doit être supérieure ou égale à 0")
        @DecimalMax(value = "1.0", message = "L'accuracy doit être inférieure ou égale à 1")
        @Schema(example = "0.98")
        Double accuracy,

        @NotNull(message = "Le F1 score est obligatoire")
        @DecimalMin(value = "0.0", message = "Le F1 score doit être supérieur ou égal à 0")
        @DecimalMax(value = "1.0", message = "Le F1 score doit être inférieur ou égal à 1")
        @Schema(example = "0.97")
        Double f1Score,

        @NotNull(message = "La durée d'entraînement est obligatoire")
        @Positive(message = "La durée d'entraînement doit être strictement positive")
        @Schema(description = "Duree en secondes", example = "7245")
        Long dureeEntrainement,

        @NotNull(message = "La date d'exécution est obligatoire")
        @PastOrPresent(message = "La date d'exécution ne peut pas être dans le futur")
        @Schema(example = "2026-05-01T10:30:00")
        LocalDateTime dateExecution) {
}
