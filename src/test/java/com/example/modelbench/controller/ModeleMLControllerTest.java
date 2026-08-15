package com.example.modelbench.controller;

import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.TypeModele;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.service.ModeleMLService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeleMLController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModeleMLControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModeleMLService service;

    private ModeleMLResponse unModele() {
        return new ModeleMLResponse(1L, "ResNet-50", TypeModele.VISION, "CNN", "1.0",
                LocalDateTime.of(2026, 2, 2, 10, 0, 0));
    }

    @Test
    void listeLesModelesDansUneEnveloppeDePagination() throws Exception {
        when(service.rechercher(any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(unModele()), 0, 10, 1, 1, true));

        mockMvc.perform(get("/api/modeles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[0].nom").value("ResNet-50"))
                .andExpect(jsonPath("$.contenu[0].type").value("VISION"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void renvoie201EtLEnteteLocationALaCreation() throws Exception {
        when(service.creer(any())).thenReturn(unModele());

        String corps = """
                {"nom":"ResNet-50","type":"VISION","algorithme":"CNN","version":"1.0"}
                """;

        mockMvc.perform(post("/api/modeles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/modeles/1"));
    }

    @Test
    void renvoie400QuandLaVersionNeRespectePasLeFormatAttendu() throws Exception {
        String corps = """
                {"nom":"ResNet-50","type":"VISION","algorithme":"CNN","version":"version-finale"}
                """;

        mockMvc.perform(post("/api/modeles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].champ").value("version"));
    }

    @Test
    void renvoie409QuandLeModeleEstReferenceParDesExperimentations() throws Exception {
        doThrow(new ResourceInUseException("Ce modele est utilise par 5 experimentation(s)"))
                .when(service).supprimer(1L);

        mockMvc.perform(delete("/api/modeles/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"));
    }
}
