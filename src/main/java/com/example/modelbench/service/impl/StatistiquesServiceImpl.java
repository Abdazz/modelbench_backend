package com.example.modelbench.service.impl;

import com.example.modelbench.dto.MeilleurModeleResponse;
import com.example.modelbench.dto.SyntheseResponse;
import com.example.modelbench.entity.Experimentation;
import com.example.modelbench.mapper.ExperimentationMapper;
import com.example.modelbench.repository.DatasetRepository;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.StatistiquesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatistiquesServiceImpl implements StatistiquesService {

    private final DatasetRepository depotDatasets;
    private final ModeleMLRepository depotModeles;
    private final ExperimentationRepository depotExperimentations;
    private final ExperimentationMapper mapper;

    public StatistiquesServiceImpl(DatasetRepository depotDatasets,
                                   ModeleMLRepository depotModeles,
                                   ExperimentationRepository depotExperimentations,
                                   ExperimentationMapper mapper) {
        this.depotDatasets = depotDatasets;
        this.depotModeles = depotModeles;
        this.depotExperimentations = depotExperimentations;
        this.mapper = mapper;
    }

    @Override
    public SyntheseResponse synthese() {
        List<Experimentation> experimentations =
                depotExperimentations.chargerToutesTrieesParAccuracy();

        if (experimentations.isEmpty()) {
            return new SyntheseResponse(
                    depotDatasets.count(), depotModeles.count(), 0L, null, null);
        }

        double moyenne = experimentations.stream()
                .mapToDouble(Experimentation::getAccuracy)
                .average()
                .orElse(0d);

        return new SyntheseResponse(
                depotDatasets.count(),
                depotModeles.count(),
                experimentations.size(),
                moyenne,
                mapper.versReponse(experimentations.get(0)));
    }

    @Override
    public List<MeilleurModeleResponse> meilleursModeles() {
        // La liste arrive deja triee par accuracy decroissante : la premiere occurrence rencontree
        // pour un dataset donne est donc necessairement sa meilleure experimentation.
        Map<Long, MeilleurModeleResponse> meilleurParDataset = new LinkedHashMap<>();

        for (Experimentation experimentation : depotExperimentations.chargerToutesTrieesParAccuracy()) {
            meilleurParDataset.computeIfAbsent(
                    experimentation.getDataset().getId(),
                    identifiant -> new MeilleurModeleResponse(
                            experimentation.getDataset().getId(),
                            experimentation.getDataset().getNom(),
                            experimentation.getModele().getId(),
                            experimentation.getModele().getNom(),
                            experimentation.getAccuracy(),
                            experimentation.getF1Score()));
        }

        return new ArrayList<>(meilleurParDataset.values());
    }
}
