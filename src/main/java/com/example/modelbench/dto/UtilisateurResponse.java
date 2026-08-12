package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Identite du porteur du jeton")
public record UtilisateurResponse(String login, String nomComplet, List<String> roles) {
}
