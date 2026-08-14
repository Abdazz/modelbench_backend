package com.example.modelbench.controller;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.TypeModele;
import com.example.modelbench.service.ModeleMLService;
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
@RequestMapping("/api/modeles")
@Tag(name = "Modeles", description = "Catalogue des modeles de Machine Learning")
public class ModeleMLController {

    private final ModeleMLService service;

    public ModeleMLController(ModeleMLService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les modeles",
            description = "Pagination, tri et filtrage executes en base de donnees")
    @ApiResponse(responseCode = "200", description = "Page de modeles")
    public PageResponse<ModeleMLResponse> lister(
            @Parameter(description = "Fragment cherche dans le nom ou l'algorithme")
            @RequestParam(required = false) String recherche,

            @Parameter(description = "Filtre sur la famille de tache")
            @RequestParam(required = false) TypeModele type,

            @PageableDefault(size = 10, sort = "dateCreation", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return service.rechercher(recherche, type, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un modele par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modele trouve"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ModeleMLResponse obtenir(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    @PostMapping
    @Operation(summary = "Creer un modele")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Modele créé"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Couple nom et version deja utilisé",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ModeleMLResponse> creer(@Valid @RequestBody ModeleMLRequest requete) {
        ModeleMLResponse cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/modeles/" + cree.id())).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un modele existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modele modifie"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Couple nom et version deja utilise",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ModeleMLResponse modifier(@PathVariable Long id,
                                     @Valid @RequestBody ModeleMLRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un modele",
            description = "Refuse en 409 si des experimentations referencent ce modele")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Modele supprime"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Modele reference ailleurs",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
