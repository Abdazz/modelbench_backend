package com.example.modelbench.controller;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.service.DatasetService;
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
@RequestMapping("/api/datasets")
@Tag(name = "Datasets", description = "Catalogue des jeux de donnees du laboratoire")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les datasets",
            description = "Pagination, tri et filtrage executés en base de donnees")
    @ApiResponse(responseCode = "200", description = "Page de datasets")
    public PageResponse<DatasetResponse> lister(
            @Parameter(description = "Fragment cherche dans le nom ou la source")
            @RequestParam(required = false) String recherche,

            @Parameter(description = "Filtre sur le format exact")
            @RequestParam(required = false) FormatDataset format,

            @PageableDefault(size = 10, sort = "dateAjout", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return service.rechercher(recherche, format, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un dataset par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dataset trouve"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public DatasetResponse obtenir(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    @PostMapping
    @Operation(summary = "Creer un dataset")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dataset cree"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nom deja utilise",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<DatasetResponse> creer(@Valid @RequestBody DatasetRequest requete) {
        DatasetResponse cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/datasets/" + cree.id())).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un dataset existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dataset modifie"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nom deja utilise",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public DatasetResponse modifier(@PathVariable Long id,
                                    @Valid @RequestBody DatasetRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un dataset",
            description = "Refuse en 409 si des experimentations referencent ce dataset")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dataset supprime"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Dataset reference ailleurs",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
