package com.example.modelbench.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Aligne les reponses 401 et 403 produites par Spring Security sur le format ApiError utilise
 * partout ailleurs dans l'API.
 */
@Component
public class GestionnaireErreursSecurite implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest requete, HttpServletResponse reponse,
                         AuthenticationException exception) throws IOException {
        ecrire(requete, reponse, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED",
                "Authentification requise : jeton absent, expiré ou invalide");
    }

    @Override
    public void handle(HttpServletRequest requete, HttpServletResponse reponse,
                       AccessDeniedException exception) throws IOException {
        ecrire(requete, reponse, HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "Votre rôle ne permet pas cette opération");
    }

    private void ecrire(HttpServletRequest requete, HttpServletResponse reponse,
                        HttpStatus statut, String code, String message) throws IOException {
        reponse.setStatus(statut.value());
        reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String corps = """
                {"timestamp":"%s","status":%d,"code":"%s","message":"%s","path":"%s"}
                """.formatted(Instant.now(), statut.value(), code, message,
                requete.getRequestURI());

        reponse.getWriter().write(corps);
    }
}
