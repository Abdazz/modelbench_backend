package com.example.modelbench.controller;

import com.example.modelbench.dto.ReferenceResponse;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.entity.enums.TypeModele;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reference")
@Tag(name = "Reference", description = "Valeurs d'enumeration destinees aux listes deroulantes")
public class ReferenceController {

    @GetMapping("/formats-dataset")
    @Operation(summary = "Lister les formats de dataset disponibles")
    public List<ReferenceResponse> formatsDataset() {
        return Arrays.stream(FormatDataset.values())
                .map(format -> new ReferenceResponse(format.name(), format.getLibelle()))
                .toList();
    }

    @GetMapping("/types-modele")
    @Operation(summary = "Lister les types de modele disponibles")
    public List<ReferenceResponse> typesModele() {
        return Arrays.stream(TypeModele.values())
                .map(type -> new ReferenceResponse(type.name(), type.getLibelle()))
                .toList();
    }
}
