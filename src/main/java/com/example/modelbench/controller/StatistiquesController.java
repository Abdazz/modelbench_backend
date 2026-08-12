package com.example.modelbench.controller;

import com.example.modelbench.dto.MeilleurModeleResponse;
import com.example.modelbench.dto.SyntheseResponse;
import com.example.modelbench.service.StatistiquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistiques")
@Tag(name = "Statistiques", description = "Indicateurs agreges du tableau de bord")
public class StatistiquesController {

    private final StatistiquesService service;

    public StatistiquesController(StatistiquesService service) {
        this.service = service;
    }

    @GetMapping("/synthese")
    @Operation(summary = "Obtenir les indicateurs de synthese du catalogue")
    public SyntheseResponse synthese() {
        return service.synthese();
    }

    @GetMapping("/meilleurs-modeles")
    @Operation(summary = "Obtenir le meilleur modele pour chaque dataset")
    public List<MeilleurModeleResponse> meilleursModeles() {
        return service.meilleursModeles();
    }
}
