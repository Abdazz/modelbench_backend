package com.example.modelbench.controller;

import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.service.DatasetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatasetController.class)
@AutoConfigureMockMvc(addFilters = false)
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatasetService service;

    private DatasetResponse unDataset() {
        return new DatasetResponse(1L, "MNIST", "Chiffres", "Kaggle", 70000L,
                FormatDataset.IMAGES, LocalDate.of(2026, 3, 14));
    }

    private static final String CORPS_VALIDE = """
            {"nom":"MNIST","description":"Chiffres","source":"Kaggle",
             "nombreObservations":70000,"format":"IMAGES"}
            """;

    @Test
    void listeLesDatasetsDansUneEnveloppeDePagination() throws Exception {
        when(service.rechercher(any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(unDataset()), 0, 10, 1, 1, true));

        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(1))
                .andExpect(jsonPath("$.contenu[0].nom").value("MNIST"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.dernier").value(true));
    }

    @Test
    void renvoieUnDatasetParIdentifiant() throws Exception {
        when(service.trouverParId(1L)).thenReturn(unDataset());

        mockMvc.perform(get("/api/datasets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.format").value("IMAGES"));
    }

    @Test
    void renvoie404QuandLeDatasetEstIntrouvable() throws Exception {
        when(service.trouverParId(42L)).thenThrow(new ResourceNotFoundException("Dataset", 42L));

        mockMvc.perform(get("/api/datasets/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void renvoie201EtLEnteteLocationALaCreation() throws Exception {
        when(service.creer(any())).thenReturn(unDataset());

        mockMvc.perform(post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/datasets/1"))
                .andExpect(jsonPath("$.nom").value("MNIST"));
    }

    @Test
    void renvoie400AvecLeDetailDesChampsQuandLaValidationEchoue() throws Exception {
        String corpsInvalide = """
                {"nom":"","source":"","nombreObservations":-5,"format":null}
                """;

        mockMvc.perform(post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsInvalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.length()").value(5));
    }

    @Test
    void renvoie400QuandLeFormatEnParametreEstInvalide() throws Exception {
        mockMvc.perform(get("/api/datasets").param("format", "FORMAT_INEXISTANT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void renvoie204ALaSuppression() throws Exception {
        doNothing().when(service).supprimer(1L);

        mockMvc.perform(delete("/api/datasets/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void renvoie409QuandLeDatasetEstReferenceParDesExperimentations() throws Exception {
        doThrow(new ResourceInUseException("Ce dataset est utilise par 3 experimentation(s)"))
                .when(service).supprimer(eq(1L));

        mockMvc.perform(delete("/api/datasets/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"));
    }
}
