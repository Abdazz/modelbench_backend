package com.example.modelbench.mapper;

import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.entity.Dataset;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entite Dataset et ses DTO.
 */
@Component
public class DatasetMapper {

    public Dataset versEntite(DatasetRequest requete) {
        Dataset dataset = new Dataset();
        mettreAJour(dataset, requete);
        return dataset;
    }

    public DatasetResponse versReponse(Dataset dataset) {
        return new DatasetResponse(
                dataset.getId(),
                dataset.getNom(),
                dataset.getDescription(),
                dataset.getSource(),
                dataset.getNombreObservations(),
                dataset.getFormat(),
                dataset.getDateAjout());
    }

    /**
     * Reporte les champs modifiables de la requete sur une entite existante. L'identifiant et la
     * date d'ajout ne sont volontairement jamais touches.
     */
    public void mettreAJour(Dataset cible, DatasetRequest requete) {
        cible.setNom(requete.nom());
        cible.setDescription(requete.description());
        cible.setSource(requete.source());
        cible.setNombreObservations(requete.nombreObservations());
        cible.setFormat(requete.format());
    }
}
