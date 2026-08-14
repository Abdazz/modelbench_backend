package com.example.modelbench.controller;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.ConnexionRequest;
import com.example.modelbench.dto.ConnexionResponse;
import com.example.modelbench.dto.UtilisateurResponse;
import com.example.modelbench.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Connexion et identite du porteur du jeton")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter et obtenir un jeton",
            description = "Comptes de demonstration : admin@example.com/admin123 (ecriture) "
                    + "et chercheur@example.com/chercheur123 (lecture seule)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jeton delivre"),
            @ApiResponse(responseCode = "400", description = "Champs manquants",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ConnexionResponse connecter(@Valid @RequestBody ConnexionRequest requete) {
        return service.connecter(requete);
    }

    @GetMapping("/moi")
    @Operation(summary = "Obtenir l'identite associee au jeton courant",
            description = "Permet au frontend de restaurer la session apres un rechargement de page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Identite du porteur"),
            @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UtilisateurResponse moi(@AuthenticationPrincipal Jwt jeton) {
        return service.profil(jeton.getSubject());
    }
}
