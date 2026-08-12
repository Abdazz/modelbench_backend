package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Identifiants de connexion")
public record ConnexionRequest(

        @NotBlank(message = "Le login est obligatoire")
        @Schema(example = "admin")
        String login,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Schema(example = "admin123")
        String motDePasse) {
}
