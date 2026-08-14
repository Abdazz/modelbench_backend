package com.example.modelbench.controller;

import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.service.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UtilisateurController.class)
@AutoConfigureMockMvc(addFilters = false)
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurService service;

    private UtilisateurAdminResponse unUtilisateur() {
        return new UtilisateurAdminResponse(1L, "marie.curie@example.com", "Marie Curie",
                Role.CHERCHEUR, true);
    }

    private static final String CORPS_VALIDE = """
            {"nomComplet":"Marie Curie","login":"marie.curie@example.com",
             "motDePasse":"motdepasse123","role":"CHERCHEUR","actif":true}
            """;

    @Test
    void listeLesUtilisateursDansUneEnveloppeDePagination() throws Exception {
        when(service.rechercher(any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(unUtilisateur()), 0, 10, 1, 1, true));

        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(1))
                .andExpect(jsonPath("$.contenu[0].login").value("marie.curie@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void renvoieUnUtilisateurParIdentifiant() throws Exception {
        when(service.trouverParId(1L)).thenReturn(unUtilisateur());

        mockMvc.perform(get("/api/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("CHERCHEUR"));
    }

    @Test
    void renvoie404QuandLUtilisateurEstIntrouvable() throws Exception {
        when(service.trouverParId(42L)).thenThrow(new ResourceNotFoundException("Utilisateur", 42L));

        mockMvc.perform(get("/api/utilisateurs/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void renvoie201EtLEnteteLocationALaCreation() throws Exception {
        when(service.creer(any())).thenReturn(unUtilisateur());

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/utilisateurs/1"))
                .andExpect(jsonPath("$.login").value("marie.curie@example.com"));
    }

    @Test
    void renvoie409QuandLEmailEstDejaUtilise() throws Exception {
        when(service.creer(any())).thenThrow(
                new DuplicateResourceException("Un utilisateur avec l'email ... existe deja"));

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void renvoie400AvecLeDetailDesChampsQuandLaValidationEchoue() throws Exception {
        String corpsInvalide = """
                {"nomComplet":"","login":"pasunadresse","motDePasse":"court","role":null,"actif":true}
                """;

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsInvalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.length()").value(4));
    }

    @Test
    void renvoie204ALaSuppression() throws Exception {
        doNothing().when(service).supprimer(1L);

        mockMvc.perform(delete("/api/utilisateurs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void renvoie409QuandLaSuppressionEstRefuseeParUnGardeFou() throws Exception {
        doThrow(new ResourceInUseException("Un administrateur ne peut pas se supprimer lui-même"))
                .when(service).supprimer(eq(1L));

        mockMvc.perform(delete("/api/utilisateurs/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"));
    }

    @Test
    void renvoie400QuandLeChampActifEstAbsentALaCreation() throws Exception {
        String corpsSansActif = """
                {"nomComplet":"Marie Curie","login":"marie.curie@example.com",
                 "motDePasse":"motdepasse123","role":"CHERCHEUR"}
                """;

        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsSansActif))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void renvoie400QuandLeChampActifEstAbsentALaModification() throws Exception {
        String corpsSansActif = """
                {"nomComplet":"Marie Curie","login":"marie.curie@example.com",
                 "role":"CHERCHEUR"}
                """;

        mockMvc.perform(put("/api/utilisateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsSansActif))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
