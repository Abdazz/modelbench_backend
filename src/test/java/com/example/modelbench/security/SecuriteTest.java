package com.example.modelbench.security;

import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.repository.UtilisateurRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecuriteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository depotUtilisateurs;

    @Autowired
    private PasswordEncoder encodeur;

    @BeforeEach
    void creerLesComptes() {
        depotUtilisateurs.deleteAll();
        depotUtilisateurs.save(compte("admin@example.com", "admin123", Role.ADMIN));
        depotUtilisateurs.save(compte("chercheur@example.com", "chercheur123", Role.CHERCHEUR));
    }

    private Utilisateur compte(String login, String motDePasse, Role role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(encodeur.encode(motDePasse));
        utilisateur.setNomComplet("Compte " + login);
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        return utilisateur;
    }

    private String jetonDe(String login, String motDePasse) throws Exception {
        String corps = """
                {"login":"%s","motDePasse":"%s"}
                """.formatted(login, motDePasse);

        String reponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> corpsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(reponse, new TypeReference<>() {
                });
        return (String) corpsJson.get("token");
    }

    private static final String CORPS_DATASET = """
            {"nom":"Dataset de test","description":"","source":"Kaggle",
             "nombreObservations":100,"format":"CSV"}
            """;

    @Test
    void refuseUnAccesSansJetonAvecLeFormatDErreurStandard() throws Exception {
        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void refuseUneConnexionAvecUnLoginQuiNEstPasUneAdresseEmail() throws Exception {
        String corps = """
                {"login":"pasunadresse","motDePasse":"admin123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void laisseSwaggerEtLeContratOpenApiPublics() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void renvoieUnJetonExploitableApresUneConnexionValide() throws Exception {
        String jeton = jetonDe("admin@example.com", "admin123");

        assertThat(jeton).isNotBlank();

        mockMvc.perform(get("/api/auth/moi").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void refuseUneConnexionAvecUnMauvaisMotDePasse() throws Exception {
        String corps = """
                {"login":"admin@example.com","motDePasse":"mauvais"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void autoriseLaLectureAUnChercheur() throws Exception {
        String jeton = jetonDe("chercheur@example.com", "chercheur123");

        mockMvc.perform(get("/api/datasets").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk());
    }

    @Test
    void refuseLEcritureAUnChercheurAvecLeFormatDErreurStandard() throws Exception {
        String jeton = jetonDe("chercheur@example.com", "chercheur123");

        mockMvc.perform(post("/api/datasets")
                        .header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_DATASET))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void autoriseLEcritureAUnAdministrateur() throws Exception {
        String jeton = jetonDe("admin@example.com", "admin123");

        mockMvc.perform(post("/api/datasets")
                        .header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_DATASET))
                .andExpect(status().isCreated());
    }
}
