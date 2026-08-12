package com.example.modelbench.service;

import com.example.modelbench.dto.ModeleMLRequest;
import com.example.modelbench.dto.ModeleMLResponse;
import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.entity.enums.TypeModele;
import org.springframework.data.domain.Pageable;

/**
 * Operations metier sur le catalogue de modeles de Machine Learning.
 */
public interface ModeleMLService {

    /**
     * @param recherche fragment cherche dans le nom ou l'algorithme, peut etre nul
     * @param type      famille de tache exacte attendue, peut etre nul
     */
    PageResponse<ModeleMLResponse> rechercher(String recherche, TypeModele type, Pageable pageable);

    /**
     * @throws com.example.modelbench.exception.ResourceNotFoundException si l'identifiant est inconnu
     */
    ModeleMLResponse trouverParId(Long id);

    /**
     * @throws com.example.modelbench.exception.DuplicateResourceException si le couple nom et version
     *                                                                    est deja pris
     */
    ModeleMLResponse creer(ModeleMLRequest requete);

    ModeleMLResponse modifier(Long id, ModeleMLRequest requete);

    /**
     * @throws com.example.modelbench.exception.ResourceInUseException si au moins une experimentation
     *                                                                 reference ce modele
     */
    void supprimer(Long id);
}
