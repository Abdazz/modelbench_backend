package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.TypeModele;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Representation d'un modele renvoyee par l'API")
public record ModeleMLResponse(
        Long id,
        String nom,
        TypeModele type,
        String algorithme,
        String version,
        LocalDateTime dateCreation) {
}
