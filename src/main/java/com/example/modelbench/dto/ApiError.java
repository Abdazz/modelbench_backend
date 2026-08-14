package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

/**
 * Corps de reponse unique pour toutes les erreurs de l'API.
 */
@Schema(description = "Corps d'erreur standard de l'API")
public record ApiError(

        @Schema(description = "Horodatage UTC de l'erreur")
        Instant timestamp,

        @Schema(description = "Code de statut HTTP", example = "400")
        int status,

        @Schema(description = "Code d'erreur applicatif", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Message explicite")
        String message,

        @Schema(description = "Chemin de la requete en cause", example = "/api/datasets")
        String path,

        @Schema(description = "Detail par champ, present uniquement sur les erreurs de validation")
        List<ApiFieldError> errors) {

    public static ApiError de(HttpStatus statut, String code, String message, String chemin) {
        return new ApiError(Instant.now(), statut.value(), code, message, chemin, null);
    }

    public static ApiError de(HttpStatus statut, String code, String message, String chemin,
                              List<ApiFieldError> erreurs) {
        return new ApiError(Instant.now(), statut.value(), code, message, chemin, erreurs);
    }
}
