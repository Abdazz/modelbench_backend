package com.example.modelbench.service.impl;

import com.example.modelbench.dto.ConnexionRequest;
import com.example.modelbench.dto.ConnexionResponse;
import com.example.modelbench.dto.UtilisateurResponse;
import com.example.modelbench.entity.Utilisateur;
import com.example.modelbench.repository.UtilisateurRepository;
import com.example.modelbench.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository depot;
    private final PasswordEncoder encodeur;
    private final JwtEncoder encodeurJeton;
    private final long dureeValiditeSecondes;

    public AuthServiceImpl(UtilisateurRepository depot,
                           PasswordEncoder encodeur,
                           JwtEncoder encodeurJeton,
                           @Value("${security.jwt.duree-validite-secondes}") long dureeValiditeSecondes) {
        this.depot = depot;
        this.encodeur = encodeur;
        this.encodeurJeton = encodeurJeton;
        this.dureeValiditeSecondes = dureeValiditeSecondes;
    }

    @Override
    public ConnexionResponse connecter(ConnexionRequest requete) {
        Utilisateur utilisateur = depot.findByLogin(requete.login())
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));

        if (!utilisateur.isActif()
                || !encodeur.matches(requete.motDePasse(), utilisateur.getMotDePasse())) {
            // Message volontairement identique dans les trois cas : distinguer "login inconnu" de
            // "mot de passe faux" permettrait d'enumerer les comptes existants.
            throw new BadCredentialsException("Identifiants invalides");
        }

        Instant maintenant = Instant.now();
        String role = utilisateur.getRole().name();

        JwtClaimsSet revendications = JwtClaimsSet.builder()
                .issuer("modelbench")
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(dureeValiditeSecondes, ChronoUnit.SECONDS))
                .subject(utilisateur.getLogin())
                .claim("roles", List.of(role))
                .claim("nomComplet", utilisateur.getNomComplet())
                .build();

        String jeton = encodeurJeton.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), revendications)).getTokenValue();

        return new ConnexionResponse(jeton, "Bearer", dureeValiditeSecondes,
                utilisateur.getLogin(), utilisateur.getNomComplet(), List.of(role));
    }

    @Override
    public UtilisateurResponse profil(String login) {
        Utilisateur utilisateur = depot.findByLogin(login)
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));

        return new UtilisateurResponse(utilisateur.getLogin(), utilisateur.getNomComplet(),
                List.of(utilisateur.getRole().name()));
    }
}
