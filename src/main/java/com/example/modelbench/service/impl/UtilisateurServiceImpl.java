package com.example.modelbench.service.impl;

import com.example.modelbench.dto.PageResponse;
import com.example.modelbench.dto.UtilisateurAdminResponse;
import com.example.modelbench.dto.UtilisateurCreationRequest;
import com.example.modelbench.dto.UtilisateurModificationRequest;
import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.entity.enums.Role;
import com.example.modelbench.exception.DuplicateResourceException;
import com.example.modelbench.exception.ResourceInUseException;
import com.example.modelbench.exception.ResourceNotFoundException;
import com.example.modelbench.mapper.UtilisateurMapper;
import com.example.modelbench.repository.UtilisateurRepository;
import com.example.modelbench.service.UtilisateurService;
import com.example.modelbench.specification.UtilisateurSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository depot;
    private final UtilisateurMapper mapper;
    private final PasswordEncoder encodeur;

    public UtilisateurServiceImpl(UtilisateurRepository depot, UtilisateurMapper mapper,
                                  PasswordEncoder encodeur) {
        this.depot = depot;
        this.mapper = mapper;
        this.encodeur = encodeur;
    }

    @Override
    public PageResponse<UtilisateurAdminResponse> rechercher(String recherche, Role role,
                                                              Pageable pageable) {
        return PageResponse.de(depot
                .findAll(UtilisateurSpecifications.filtrer(recherche, role), pageable)
                .map(mapper::versReponse));
    }

    @Override
    public UtilisateurAdminResponse trouverParId(Long id) {
        return mapper.versReponse(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public UtilisateurAdminResponse creer(UtilisateurCreationRequest requete) {
        if (depot.existsByLoginIgnoreCase(requete.login())) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec l'email %s existe déjà".formatted(requete.login()));
        }

        Utilisateur utilisateur = mapper.versEntite(requete, encodeur.encode(requete.motDePasse()));
        return mapper.versReponse(depot.save(utilisateur));
    }

    @Override
    @Transactional
    public UtilisateurAdminResponse modifier(Long id, UtilisateurModificationRequest requete) {
        Utilisateur utilisateur = chargerOuEchouer(id);

        if (depot.existsByLoginIgnoreCaseAndIdNot(requete.login(), id)) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec l'email %s existe déjà".formatted(requete.login()));
        }

        if (!requete.actif()) {
            refuserSiCompteCourant(utilisateur, "désactiver son propre compte");
        }
        if (!utilisateur.getLogin().equalsIgnoreCase(requete.login())) {
            refuserSiCompteCourant(utilisateur, "changer son propre email de connexion");
        }
        boolean resteAdministrateurActif = requete.role() == Role.ADMIN && requete.actif();
        refuserSiPerteDuDernierAdministrateurActif(utilisateur, resteAdministrateurActif);

        mapper.mettreAJour(utilisateur, requete);
        if (requete.motDePasse() != null && !requete.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(encodeur.encode(requete.motDePasse()));
        }

        return mapper.versReponse(depot.save(utilisateur));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Utilisateur utilisateur = chargerOuEchouer(id);

        refuserSiCompteCourant(utilisateur, "se supprimer lui-même");
        refuserSiPerteDuDernierAdministrateurActif(utilisateur, false);

        depot.delete(utilisateur);
    }

    private void refuserSiCompteCourant(Utilisateur cible, String action) {
        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        if (authentification == null) {
            return;
        }
        if (cible.getLogin().equalsIgnoreCase(authentification.getName())) {
            throw new ResourceInUseException("Un administrateur ne peut pas %s".formatted(action));
        }
    }

    /**
     * @param resteAdministrateurActif vrai si, apres l'operation en cours, la cible sera encore un
     *                                 ADMIN actif (faux pour une suppression, calcule a partir de la
     *                                 requete pour une modification)
     */
    private void refuserSiPerteDuDernierAdministrateurActif(Utilisateur cible,
                                                            boolean resteAdministrateurActif) {
        boolean etaitAdministrateurActif = cible.getRole() == Role.ADMIN && cible.isActif();
        if (etaitAdministrateurActif && !resteAdministrateurActif
                && depot.countByRoleAndActifTrue(Role.ADMIN) <= 1) {
            throw new ResourceInUseException(
                    "Impossible de modifier ou supprimer le dernier administrateur actif");
        }
    }

    private Utilisateur chargerOuEchouer(Long id) {
        return depot.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }
}
