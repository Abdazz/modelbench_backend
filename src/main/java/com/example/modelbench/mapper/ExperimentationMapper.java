package com.example.modelbench.mapper;

import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entite Experimentation et ses DTO.
 */
@Component
public class ExperimentationMapper {

    public ExperimentationResponse versReponse(Experimentation experimentation) {
        Dataset dataset = experimentation.getDataset();
        ModeleML modele = experimentation.getModele();

        return new ExperimentationResponse(
                experimentation.getId(),
                dataset.getId(),
                dataset.getNom(),
                modele.getId(),
                modele.getNom(),
                experimentation.getAccuracy(),
                experimentation.getF1Score(),
                experimentation.getDureeEntrainement(),
                experimentation.getDateExecution());
    }

    /**
     * Reporte la requete sur une experimentation, existante ou neuve. Les entites liees sont
     * passees en parametre car seul le service sait les resoudre et verifier leur existence.
     */
    public void appliquer(Experimentation cible, ExperimentationRequest requete,
                          Dataset dataset, ModeleML modele) {
        cible.setDataset(dataset);
        cible.setModele(modele);
        cible.setAccuracy(requete.accuracy());
        cible.setF1Score(requete.f1Score());
        cible.setDureeEntrainement(requete.dureeEntrainement());
        cible.setDateExecution(requete.dateExecution());
    }
}
