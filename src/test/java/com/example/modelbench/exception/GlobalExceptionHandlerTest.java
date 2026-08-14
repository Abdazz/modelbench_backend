package com.example.modelbench.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    record RequeteDeTest(@NotBlank String nom, @DecimalMax("1.0") Double accuracy) {
    }

    @RestController
    @RequestMapping("/test")
    static class ControleurDeTest {

        @PostMapping
        String creer(@Valid @RequestBody RequeteDeTest requete) {
            return "ok";
        }

        @org.springframework.web.bind.annotation.GetMapping("/introuvable")
        String introuvable() {
            throw new ResourceNotFoundException("Dataset", 42L);
        }

        @org.springframework.web.bind.annotation.GetMapping("/doublon")
        String doublon() {
            throw new DuplicateResourceException("Un dataset nomme MNIST existe deja");
        }

        @org.springframework.web.bind.annotation.GetMapping("/utilise")
        String utilise() {
            throw new ResourceInUseException("Ce dataset est utilise par 3 experimentation(s)");
        }

        @org.springframework.web.bind.annotation.GetMapping("/panne")
        String panne() {
            throw new IllegalStateException("secret interne a ne jamais exposer");
        }

        @org.springframework.web.bind.annotation.GetMapping("/route-inconnue")
        String routeInconnue() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "/test/route-inconnue", "route-inconnue");
        }

        @org.springframework.web.bind.annotation.GetMapping("/methode-non-autorisee")
        String methodeNonAutorisee() throws HttpRequestMethodNotSupportedException {
            throw new HttpRequestMethodNotSupportedException("POST");
        }

        @org.springframework.web.bind.annotation.GetMapping("/type-non-supporte")
        String typeNonSupporte() throws HttpMediaTypeNotSupportedException {
            throw new HttpMediaTypeNotSupportedException("Content-Type non supporte");
        }
    }

    @BeforeEach
    void preparer() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControleurDeTest())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void traduitUneErreurDeValidationEn400AvecLeDetailDesChamps() throws Exception {
        mockMvc.perform(post("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"accuracy\":1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test"))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void traduitUneRessourceIntrouvableEn404() throws Exception {
        mockMvc.perform(get("/test/introuvable"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Dataset introuvable pour l'identifiant 42"));
    }

    @Test
    void traduitUnDoublonEn409() throws Exception {
        mockMvc.perform(get("/test/doublon"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void traduitUneRessourceUtiliseeEn409() throws Exception {
        mockMvc.perform(get("/test/utilise"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"))
                .andExpect(jsonPath("$.message")
                        .value("Ce dataset est utilise par 3 experimentation(s)"));
    }

    @Test
    void traduitUnCorpsJsonIllisibleEn400() throws Exception {
        mockMvc.perform(post("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ceci n'est pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void traduitUneErreurInattendueEn500SansFuiterLeMessageInterne() throws Exception {
        mockMvc.perform(get("/test/panne"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Une erreur interne est survenue, contactez l'administrateur"));
    }

    @Test
    void traduitUneRouteInconnueEn404() throws Exception {
        mockMvc.perform(get("/test/route-inconnue"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("La ressource demandée est introuvable"));
    }

    @Test
    void traduitUneMethodeHttpNonAutoriseeEn405() throws Exception {
        mockMvc.perform(get("/test/methode-non-autorisee"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message")
                        .value("La méthode HTTP n'est pas autorisée pour cette ressource"));
    }

    @Test
    void traduitUnTypeDeContenuNonSupporteEn415() throws Exception {
        mockMvc.perform(get("/test/type-non-supporte"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message")
                        .value("Le type de contenu de la requête n'est pas pris en charge"));
    }
}
