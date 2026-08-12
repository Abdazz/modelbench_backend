package com.example.modelbench.service;

import com.example.modelbench.dto.ExperimentationRequest;
import com.example.modelbench.dto.ExperimentationResponse;
import com.example.modelbench.dto.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Operations metier sur les experimentations, qui associent un modele a un dataset.
 */
public interface ExperimentationService {

    /**
     * @param recherche   fragment cherche dans le nom du dataset ou du modele, peut etre nul
     * @param datasetId   restreint a un dataset precis, peut etre nul
     * @param modeleId    restreint a un modele precis, peut etre nul
     * @param accuracyMin borne inferieure incluse, peut etre nulle
     * @param accuracyMax borne superieure incluse, peut etre nulle
     */
    PageResponse<ExperimentationResponse> rechercher(String recherche, Long datasetId,
                                                     Long modeleId, Double accuracyMin,
                                                     Double accuracyMax, Pageable pageable);

    /**
     * @throws com.example.modelbench.exception.ResourceNotFoundException si l'identifiant est inconnu
     */
    ExperimentationResponse trouverParId(Long id);

    /**
     * @throws com.example.modelbench.exception.ResourceNotFoundException si le dataset ou le modele
     *                                                                   reference n'existe pas
     */
    ExperimentationResponse creer(ExperimentationRequest requete);

    ExperimentationResponse modifier(Long id, ExperimentationRequest requete);

    void supprimer(Long id);
}
