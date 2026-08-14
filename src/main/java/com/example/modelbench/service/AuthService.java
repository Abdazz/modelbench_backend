package com.example.modelbench.service;

import com.example.modelbench.dto.ConnexionRequest;
import com.example.modelbench.dto.ConnexionResponse;
import com.example.modelbench.dto.UtilisateurResponse;
import org.springframework.security.authentication.BadCredentialsException;
/**
 * Delivrance et exploitation des jetons d'acces.
 */
public interface AuthService {

    /**
     * @throws BadCredentialsException si le login est inconnu, le compte
     * desactive ou le mot de passe faux
     */
    ConnexionResponse connecter(ConnexionRequest requete);

    UtilisateurResponse profil(String login);
}
