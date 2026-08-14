package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Utilisateur tel qu'expose a la gestion des comptes")
public record UtilisateurAdminResponse(
        Long id,
        String login,
        String nomComplet,
        Role role,
        boolean actif) {
}
