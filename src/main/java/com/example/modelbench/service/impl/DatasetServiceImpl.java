package com.example.modelbench.service.impl;

import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.Dataset;
import com.example.modelbench.entity.enums.FormatDataset;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.DatasetMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.service.DatasetService;
import com.example.modelbench.specification.DatasetSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatasetServiceImpl implements DatasetService {

    private final DatasetRepository depot;
    private final ExperimentationRepository depotExperimentations;
    private final DatasetMapper mapper;

    public DatasetServiceImpl(DatasetRepository depot,
                              ExperimentationRepository depotExperimentations,
                              DatasetMapper mapper) {
        this.depot = depot;
        this.depotExperimentations = depotExperimentations;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<DatasetResponse> rechercher(String recherche, FormatDataset format,
                                                    Pageable pageable) {
        return PageResponse.de(depot
                .findAll(DatasetSpecifications.filtrer(recherche, format), pageable)
                .map(mapper::versReponse));
    }

    @Override
    public DatasetResponse trouverParId(Long id) {
        return mapper.versReponse(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public DatasetResponse creer(DatasetRequest requete) {
        if (depot.existsByNomIgnoreCase(requete.nom())) {
            throw new DuplicateResourceException(
                    "Un dataset nommé %s existe déjà".formatted(requete.nom()));
        }

        return mapper.versReponse(depot.save(mapper.versEntite(requete)));
    }

    @Override
    @Transactional
    public DatasetResponse modifier(Long id, DatasetRequest requete) {
        Dataset dataset = chargerOuEchouer(id);

        if (depot.existsByNomIgnoreCaseAndIdNot(requete.nom(), id)) {
            throw new DuplicateResourceException(
                    "Un dataset nommé %s existe déjà".formatted(requete.nom()));
        }

        mapper.mettreAJour(dataset, requete);
        return mapper.versReponse(depot.save(dataset));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Dataset dataset = chargerOuEchouer(id);

        long referencements = depotExperimentations.countByDatasetId(id);
        if (referencements > 0) {
            throw new ResourceInUseException(
                    "Ce dataset est utilisé par %d expérimentation(s) et ne peut pas être supprimé"
                            .formatted(referencements));
        }

        depot.delete(dataset);
    }

    private Dataset chargerOuEchouer(Long id) {
        return depot.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", id));
    }
}
