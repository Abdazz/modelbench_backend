package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Donnees necessaires a la creation d'un utilisateur")
public record UtilisateurCreationRequest(

        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(max = 120, message = "Le nom complet ne peut depasser 120 caracteres")
        @Schema(example = "Marie Curie")
        String nomComplet,

        @NotBlank(message = "Le login est obligatoire")
        @Email(message = "Le login doit etre une adresse email valide")
        @Size(max = 60, message = "Le login ne peut depasser 60 caracteres")
        @Schema(example = "marie.curie@example.com")
        String login,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        @Schema(example = "motdepasse123")
        String motDePasse,

        @NotNull(message = "Le role est obligatoire")
        @Schema(example = "CHERCHEUR")
        Role role,

        @Schema(description = "Compte actif des sa creation", example = "true")
        boolean actif) {
}
