package com.example.modelbench.service.impl;

import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Fait le pont entre l'entite Utilisateur et le modele de compte de Spring Security.
 */
@Service
@Transactional(readOnly = true)
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository depot;

    public UtilisateurDetailsService(UtilisateurRepository depot) {
        this.depot = depot;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        Utilisateur utilisateur = depot.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Compte inconnu : " + login));

        return User.withUsername(utilisateur.getLogin())
                .password(utilisateur.getMotDePasse())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + utilisateur.getRole().name())))
                .disabled(!utilisateur.isActif())
                .build();
    }
}
