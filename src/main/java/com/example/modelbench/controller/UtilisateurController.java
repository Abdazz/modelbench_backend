package com.example.modelbench.controller;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.UtilisateurCreationRequest;
import com.example.modelbench.dto.UtilisateurModificationRequest;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.service.UtilisateurService;
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
@RequestMapping("/api/utilisateurs")
@Tag(name = "Utilisateurs", description = "Gestion des comptes, reservee au role ADMIN")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les utilisateurs",
            description = "Reserve au role ADMIN, y compris en lecture")
    @ApiResponse(responseCode = "200", description = "Page d'utilisateurs")
    public PageResponse<UtilisateurAdminResponse> lister(
            @Parameter(description = "Fragment cherche dans le nom complet ou le login")
            @RequestParam(required = false) String recherche,

            @Parameter(description = "Filtre sur le role exact")
            @RequestParam(required = false) Role role,

            @PageableDefault(size = 10, sort = "nomComplet", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return service.rechercher(recherche, role, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un utilisateur par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouve"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UtilisateurAdminResponse obtenir(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    @PostMapping
    @Operation(summary = "Creer un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur cree"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email deja utilise",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UtilisateurAdminResponse> creer(
            @Valid @RequestBody UtilisateurCreationRequest requete) {
        UtilisateurAdminResponse cree = service.creer(requete);
        return ResponseEntity.created(URI.create("/api/utilisateurs/" + cree.id())).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur existant",
            description = "Laisser motDePasse vide ou absent pour conserver le mot de passe actuel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur modifie"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email deja utilise, ou operation "
                    + "refusee sur le compte courant ou le dernier administrateur actif",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UtilisateurAdminResponse modifier(@PathVariable Long id,
                                             @Valid @RequestBody UtilisateurModificationRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur",
            description = "Refuse en 409 pour le compte courant ou le dernier administrateur actif")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur supprime"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Suppression refusee",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
