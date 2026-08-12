package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.FormatDataset;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Donnees necessaires a la creation ou la modification d'un dataset")
public record DatasetRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caracteres")
        @Schema(example = "MNIST")
        String nom,

        @Size(max = 2000, message = "La description ne peut depasser 2000 caracteres")
        @Schema(example = "Chiffres manuscrits en niveaux de gris")
        String description,

        @NotBlank(message = "La source est obligatoire")
        @Size(max = 255, message = "La source ne peut depasser 255 caracteres")
        @Schema(example = "Kaggle")
        String source,

        @NotNull(message = "Le nombre d'observations est obligatoire")
        @PositiveOrZero(message = "Le nombre d'observations ne peut etre negatif")
        @Schema(example = "70000")
        Long nombreObservations,

        @NotNull(message = "Le format est obligatoire")
        @Schema(example = "IMAGES")
        FormatDataset format) {
}
