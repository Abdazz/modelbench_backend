package com.example.modelbench.service;

import com.example.modelbench.dto.DatasetRequest;
import com.example.modelbench.dto.DatasetResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.FormatDataset;
import org.springframework.data.domain.Pageable;

/**
 * Operations metier sur le catalogue de datasets.
 */
public interface DatasetService {

    /**
     * @param recherche fragment cherche dans le nom ou la source, peut etre nul
     * @param format    format exact attendu, peut etre nul
     */
    PageResponse<DatasetResponse> rechercher(String recherche, FormatDataset format,
                                             Pageable pageable);

    /**
     * @throws com.example.modelbench.exception.ResourceNotFoundException si l'identifiant est inconnu
     */
    DatasetResponse trouverParId(Long id);

    /**
     * @throws com.example.modelbench.exception.DuplicateResourceException si le nom est deja pris
     */
    DatasetResponse creer(DatasetRequest requete);

    DatasetResponse modifier(Long id, DatasetRequest requete);

    /**
     * @throws com.example.modelbench.exception.ResourceInUseException si au moins une experimentation
     *                                                                 reference ce dataset
     */
    void supprimer(Long id);
}
