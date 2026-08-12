package com.example.modelbench.service.impl;

import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.ModeleML;
import com.example.modelbench.entity.enums.TypeModele;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.ModeleMLMapper;
import com.example.modelbench.repository.ExperimentationRepository;
import com.example.modelbench.repository.ModeleMLRepository;
import com.example.modelbench.service.ModeleMLService;
import com.example.modelbench.specification.ModeleMLSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModeleMLServiceImpl implements ModeleMLService {

    private final ModeleMLRepository depot;
    private final ExperimentationRepository depotExperimentations;
    private final ModeleMLMapper mapper;

    public ModeleMLServiceImpl(ModeleMLRepository depot,
                               ExperimentationRepository depotExperimentations,
                               ModeleMLMapper mapper) {
        this.depot = depot;
        this.depotExperimentations = depotExperimentations;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<ModeleMLResponse> rechercher(String recherche, TypeModele type,
                                                     Pageable pageable) {
        return PageResponse.de(depot
                .findAll(ModeleMLSpecifications.filtrer(recherche, type), pageable)
                .map(mapper::versReponse));
    }

    @Override
    public ModeleMLResponse trouverParId(Long id) {
        return mapper.versReponse(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public ModeleMLResponse creer(ModeleMLRequest requete) {
        if (depot.existsByNomIgnoreCaseAndVersion(requete.nom(), requete.version())) {
            throw new DuplicateResourceException(
                    "Le modele %s en version %s existe deja"
                            .formatted(requete.nom(), requete.version()));
        }

        return mapper.versReponse(depot.save(mapper.versEntite(requete)));
    }

    @Override
    @Transactional
    public ModeleMLResponse modifier(Long id, ModeleMLRequest requete) {
        ModeleML modele = chargerOuEchouer(id);

        if (depot.existsByNomIgnoreCaseAndVersionAndIdNot(requete.nom(), requete.version(), id)) {
            throw new DuplicateResourceException(
                    "Le modele %s en version %s existe deja"
                            .formatted(requete.nom(), requete.version()));
        }

        mapper.mettreAJour(modele, requete);
        return mapper.versReponse(depot.save(modele));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        ModeleML modele = chargerOuEchouer(id);

        long referencements = depotExperimentations.countByModeleId(id);
        if (referencements > 0) {
            throw new ResourceInUseException(
                    "Ce modele est utilise par %d experimentation(s) et ne peut pas etre supprime"
                            .formatted(referencements));
        }

        depot.delete(modele);
    }

    private ModeleML chargerOuEchouer(Long id) {
        return depot.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modele", id));
    }
}
