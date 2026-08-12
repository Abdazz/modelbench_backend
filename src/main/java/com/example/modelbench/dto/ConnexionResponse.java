package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Jeton delivre apres une connexion reussie")
public record ConnexionResponse(
        String token,
        String typeToken,
        long expirationSecondes,
        String login,
        String nomComplet,
        List<String> roles) {
}
