package com.example.modelbench.exception;

/**
 * Levee lorsqu'une ressource demandee par identifiant n'existe pas.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String ressource, Long id) {
        super("%s introuvable pour l'identifiant %d".formatted(ressource, id));
    }
}
