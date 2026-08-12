package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Valeur d'enumeration exposee au frontend pour alimenter une liste deroulante.
 */
@Schema(description = "Valeur de reference")
public record ReferenceResponse(

        @Schema(description = "Valeur technique a renvoyer a l'API", example = "CSV")
        String valeur,

        @Schema(description = "Libelle affichable", example = "CSV")
        String libelle) {
}
