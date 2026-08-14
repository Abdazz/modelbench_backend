package com.example.modelbench.service;

import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.UtilisateurCreationRequest;
import com.example.modelbench.dto.UtilisateurModificationRequest;
import com.example.modelbench.entity.enums.Role;
import org.springframework.data.domain.Pageable;

/**
 * Operations metier sur les comptes utilisateur. Reserve au role ADMIN, y compris en lecture (voir
 * SecurityConfig).
 */
public interface UtilisateurService {

    /**
     * @param recherche fragment cherche dans le nom complet ou le login, peut etre nul
     * @param role      role exact attendu, peut etre nul
     */
    PageResponse<UtilisateurAdminResponse> rechercher(String recherche, Role role, Pageable pageable);

    /**
     * @throws com.example.modelbench.exception.ResourceNotFoundException si l'identifiant est inconnu
     */
    UtilisateurAdminResponse trouverParId(Long id);

    /**
     * @throws com.example.modelbench.exception.DuplicateResourceException si le login est deja pris
     */
    UtilisateurAdminResponse creer(UtilisateurCreationRequest requete);

    /**
     * @throws com.example.modelbench.exception.DuplicateResourceException si le nouveau login est
     *                                                                     deja pris par un autre compte
     * @throws com.example.modelbench.exception.ResourceInUseException    si l'operation desactiverait
     *                                                                     le compte courant, ou ferait
     *                                                                     passer le nombre
     *                                                                     d'administrateurs actifs a
     *                                                                     zero
     */
    UtilisateurAdminResponse modifier(Long id, UtilisateurModificationRequest requete);

    /**
     * @throws com.example.modelbench.exception.ResourceInUseException si la cible est le compte
     *                                                                  courant, ou le dernier
     *                                                                  administrateur actif
     */
    void supprimer(Long id);
}
