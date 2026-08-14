package com.example.modelbench.controller;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.service.ExperimentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/experimentations")
@Tag(name = "Experimentations",
        description = "Executions d'un modele sur un dataset et metriques obtenues")
public class ExperimentationController {

    private final ExperimentationService service;

    public ExperimentationController(ExperimentationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les experimentations",
            description = "Pagination, tri et filtrage executes en base de donnees")
    @ApiResponse(responseCode = "200", description = "Page d'experimentations")
    public PageResponse<ExperimentationResponse> lister(
            @Parameter(description = "Fragment cherche dans le nom du dataset ou du modele")
            @RequestParam(required = false) String recherche,

            @Parameter(description = "Restreint a un dataset precis")
            @RequestParam(required = false) Long datasetId,

            @Parameter(description = "Restreint a un modele precis")
            @RequestParam(required = false) Long modeleId,

            @Parameter(description = "Accuracy minimale incluse")
            @RequestParam(required = false) Double accuracyMin,

            @Parameter(description = "Accuracy maximale incluse")
            @RequestParam(required = false) Double accuracyMax,

            @PageableDefault(size = 10, sort = "dateExecution", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return service.rechercher(recherche, datasetId, modeleId, accuracyMin, accuracyMax,
                pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une experimentation par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experimentation trouvee"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ExperimentationResponse obtenir(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    @PostMapping
    @Operation(summary = "Creer une experimentation",
            description = "Le dataset et le modele references doivent exister")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Experimentation creee"),
            @ApiResponse(responseCode = "400",
                    description = "Donnees invalides, notamment accuracy ou f1Score hors de [0, 1]",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dataset ou modele inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ExperimentationResponse> creer(
            @Valid @RequestBody ExperimentationRequest requete) {

        ExperimentationResponse creee = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/experimentations/" + creee.id())).body(creee);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une experimentation existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experimentation modifiee"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404",
                    description = "Experimentation, dataset ou modele inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ExperimentationResponse modifier(@PathVariable Long id,
                                            @Valid @RequestBody ExperimentationRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une experimentation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Experimentation supprimée"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
