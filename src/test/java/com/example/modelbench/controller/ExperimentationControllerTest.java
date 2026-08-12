package com.example.modelbench.controller;

import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.service.ExperimentationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExperimentationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExperimentationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExperimentationService service;

    private ExperimentationResponse uneExperimentation() {
        return new ExperimentationResponse(9L, 1L, "MNIST", 2L, "ResNet-50",
                0.98, 0.97, 7245L, LocalDateTime.of(2026, 5, 1, 10, 30));
    }

    private static final String CORPS_VALIDE = """
            {"datasetId":1,"modeleId":2,"accuracy":0.98,"f1Score":0.97,
             "dureeEntrainement":7245,"dateExecution":"2026-05-01T10:30:00"}
            """;

    @Test
    void listeLesExperimentationsAvecLesNomsDesRelationsAplatis() throws Exception {
        when(service.rechercher(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(uneExperimentation()), 0, 10, 1, 1, true));

        mockMvc.perform(get("/api/experimentations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[0].datasetNom").value("MNIST"))
                .andExpect(jsonPath("$.contenu[0].modeleNom").value("ResNet-50"))
                .andExpect(jsonPath("$.contenu[0].dureeEntrainement").value(7245));
    }

    @Test
    void renvoie201EtLEnteteLocationALaCreation() throws Exception {
        when(service.creer(any())).thenReturn(uneExperimentation());

        mockMvc.perform(post("/api/experimentations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/experimentations/9"));
    }

    @Test
    void renvoie400QuandLAccuracyDepasseUn() throws Exception {
        String corps = """
                {"datasetId":1,"modeleId":2,"accuracy":1.5,"f1Score":0.97,
                 "dureeEntrainement":7245,"dateExecution":"2026-05-01T10:30:00"}
                """;

        mockMvc.perform(post("/api/experimentations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].champ").value("accuracy"));
    }

    @Test
    void renvoie400QuandLaDureeDEntrainementEstNegative() throws Exception {
        String corps = """
                {"datasetId":1,"modeleId":2,"accuracy":0.9,"f1Score":0.9,
                 "dureeEntrainement":-10,"dateExecution":"2026-05-01T10:30:00"}
                """;

        mockMvc.perform(post("/api/experimentations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].champ").value("dureeEntrainement"));
    }

    @Test
    void renvoie404QuandLeDatasetReferenceNExistePas() throws Exception {
        when(service.creer(any())).thenThrow(new ResourceNotFoundException("Dataset", 1L));

        mockMvc.perform(post("/api/experimentations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
