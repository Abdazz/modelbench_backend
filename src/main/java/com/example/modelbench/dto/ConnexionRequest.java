package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Identifiants de connexion")
public record ConnexionRequest(

        @NotBlank(message = "Le login est obligatoire")
        @Email(message = "Le login doit être une adresse email valide")
        @Schema(example = "admin@example.com")
        String login,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Schema(example = "admin123")
        String motDePasse) {
}
