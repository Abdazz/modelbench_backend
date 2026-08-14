package com.example.modelbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Enveloppe de pagination propre au projet. Le type {@code Page} de Spring Data n'est pas expose
 * directement car sa forme serialisee n'est pas un contrat stable entre versions.
 */
@Schema(description = "Page de resultats")
public record PageResponse<T>(

        @Schema(description = "Elements de la page courante")
        List<T> contenu,

        @Schema(description = "Index de la page, commence à zero", example = "0")
        int page,

        @Schema(description = "Nombre d'elements demandes par page", example = "10")
        int taille,

        @Schema(description = "Nombre total d'elements correspondant au filtre", example = "42")
        long totalElements,

        @Schema(description = "Nombre total de pages", example = "5")
        int totalPages,

        @Schema(description = "Vrai s'il s'agit de la derniere page")
        boolean dernier) {

    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
