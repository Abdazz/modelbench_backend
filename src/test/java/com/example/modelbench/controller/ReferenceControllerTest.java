package com.example.modelbench.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listeLesSixFormatsDeDatasetAvecLeurLibelle() throws Exception {
        mockMvc.perform(get("/api/reference/formats-dataset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].valeur").value("CSV"))
                .andExpect(jsonPath("$[2].valeur").value("IMAGES"))
                .andExpect(jsonPath("$[2].libelle").value("Images"));
    }

    @Test
    void listeLesSixTypesDeModeleAvecLeurLibelle() throws Exception {
        mockMvc.perform(get("/api/reference/types-modele"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].valeur").value("CLASSIFICATION"))
                .andExpect(jsonPath("$[3].valeur").value("REDUCTION_DIMENSION"))
                .andExpect(jsonPath("$[3].libelle").value("Reduction de dimension"));
    }
}
