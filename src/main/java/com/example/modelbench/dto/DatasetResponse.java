package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.FormatDataset;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representation d'un dataset renvoyee par l'API")
public record DatasetResponse(
        Long id,
        String nom,
        String description,
        String source,
        Long nombreObservations,
        FormatDataset format,
        LocalDate dateAjout) {
}
