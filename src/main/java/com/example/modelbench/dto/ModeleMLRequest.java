package com.example.modelbench.dto;

import com.example.modelbench.entity.enums.TypeModele;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Donnees necessaires a la creation ou la modification d'un modele")
public record ModeleMLRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caracteres")
        @Schema(example = "ResNet-50")
        String nom,

        @NotNull(message = "Le type est obligatoire")
        @Schema(example = "VISION")
        TypeModele type,

        @NotBlank(message = "L'algorithme est obligatoire")
        @Size(max = 120, message = "L'algorithme ne peut depasser 120 caracteres")
        @Schema(example = "Reseau de neurones convolutif")
        String algorithme,

        @NotBlank(message = "La version est obligatoire")
        @Pattern(regexp = "^\\d+(\\.\\d+){0,2}$",
                message = "La version doit etre au format 1, 1.0 ou 1.0.0")
        @Schema(example = "1.2")
        String version) {
}
