package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Representation d'une experimentation renvoyee par l'API")
public record ExperimentationResponse(
        Long id,
        Long datasetId,
        String datasetNom,
        Long modeleId,
        String modeleNom,
        Double accuracy,
        Double f1Score,
        Long dureeEntrainement,
        LocalDateTime dateExecution) {
}
