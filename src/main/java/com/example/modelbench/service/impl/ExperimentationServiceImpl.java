package com.example.modelbench.service.impl;

import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.ExperimentationMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.ExperimentationService;
import com.example.modelbench.specification.ExperimentationSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExperimentationServiceImpl implements ExperimentationService {

    private final ExperimentationRepository depot;
    private final DatasetRepository depotDatasets;
    private final ModeleMLRepository depotModeles;
    private final ExperimentationMapper mapper;

    public ExperimentationServiceImpl(ExperimentationRepository depot,
                                      DatasetRepository depotDatasets,
                                      ModeleMLRepository depotModeles,
                                      ExperimentationMapper mapper) {
        this.depot = depot;
        this.depotDatasets = depotDatasets;
        this.depotModeles = depotModeles;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<ExperimentationResponse> rechercher(String recherche, Long datasetId,
                                                            Long modeleId, Double accuracyMin,
                                                            Double accuracyMax, Pageable pageable) {
        return PageResponse.de(depot
                .findAll(ExperimentationSpecifications.filtrer(
                        recherche, datasetId, modeleId, accuracyMin, accuracyMax), pageable)
                .map(mapper::versReponse));
    }

    @Override
    public ExperimentationResponse trouverParId(Long id) {
        return mapper.versReponse(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public ExperimentationResponse creer(ExperimentationRequest requete) {
        Experimentation experimentation = new Experimentation();
        mapper.appliquer(experimentation, requete,
                chargerDataset(requete.datasetId()), chargerModele(requete.modeleId()));

        return mapper.versReponse(depot.save(experimentation));
    }

    @Override
    @Transactional
    public ExperimentationResponse modifier(Long id, ExperimentationRequest requete) {
        Experimentation experimentation = chargerOuEchouer(id);
        mapper.appliquer(experimentation, requete,
                chargerDataset(requete.datasetId()), chargerModele(requete.modeleId()));

        return mapper.versReponse(depot.save(experimentation));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        depot.delete(chargerOuEchouer(id));
    }

    private Experimentation chargerOuEchouer(Long id) {
        return depot.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expérimentation", id));
    }

    /**
     * Resout le dataset reference. Passer par une 404 explicite plutot que de laisser la contrainte
     * de cle etrangere echouer donne un message utilisable par le frontend.
     */
    private Dataset chargerDataset(Long datasetId) {
        return depotDatasets.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", datasetId));
    }

    private ModeleML chargerModele(Long modeleId) {
        return depotModeles.findById(modeleId)
                .orElseThrow(() -> new ResourceNotFoundException("Modèle", modeleId));
    }
}
