package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detail d'une erreur de validation portant sur un champ precis.
 */
@Schema(description = "Erreur de validation sur un champ")
public record ApiFieldError(

        @Schema(description = "Nom du champ en faute", example = "accuracy")
        String champ,

        @Schema(description = "Raison du rejet", example = "doit etre inferieur ou egal a 1.0")
        String message) {
}
