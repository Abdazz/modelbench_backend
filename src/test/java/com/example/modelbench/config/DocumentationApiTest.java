package com.example.modelbench.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publieUnContratOpenApiDecrivantLesSeptControleurs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ModelBench"))
                .andExpect(jsonPath("$.paths['/api/datasets']").exists())
                .andExpect(jsonPath("$.paths['/api/modeles']").exists())
                .andExpect(jsonPath("$.paths['/api/experimentations']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/reference/formats-dataset']").exists())
                .andExpect(jsonPath("$.paths['/api/statistiques/synthese']").exists())
                .andExpect(jsonPath("$.paths['/api/utilisateurs']").exists());
    }

    @Test
    void declareLeSchemaDeSecuriteBearerPourLeBoutonAuthorizeDeSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat")
                        .value("JWT"));
    }

    @Test
    void autoriseLOrigineDuFrontendAngular() throws Exception {
        mockMvc.perform(options("/api/datasets")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
