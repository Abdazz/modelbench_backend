package com.example.modelbench.exception;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.ApiFieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Traduit toute exception remontant des controleurs en un corps {@link ApiError} unique.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger JOURNAL = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> traiterValidation(MethodArgumentNotValidException exception,
                                                      HttpServletRequest requete) {
        List<ApiFieldError> details = exception.getBindingResult().getFieldErrors().stream()
                .map(erreur -> new ApiFieldError(erreur.getField(), erreur.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ApiError.de(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "La validation de la requête a échoué",
                requete.getRequestURI(),
                details));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> traiterIntrouvable(ResourceNotFoundException exception,
                                                       HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.de(
                HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                exception.getMessage(), requete.getRequestURI()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> traiterDoublon(DuplicateResourceException exception,
                                                    HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.de(
                HttpStatus.CONFLICT, "DUPLICATE_RESOURCE",
                exception.getMessage(), requete.getRequestURI()));
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<ApiError> traiterRessourceUtilisee(ResourceInUseException exception,
                                                             HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.de(
                HttpStatus.CONFLICT, "RESOURCE_IN_USE",
                exception.getMessage(), requete.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> traiterIntegrite(DataIntegrityViolationException exception,
                                                     HttpServletRequest requete) {
        JOURNAL.warn("Violation de contrainte en base sur {}", requete.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.de(
                HttpStatus.CONFLICT, "DUPLICATE_RESOURCE",
                "L'opération viole une contrainte d'unicité ou d'intégrité",
                requete.getRequestURI()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class,
            InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ApiError> traiterRequeteMalformee(Exception exception,
                                                            HttpServletRequest requete) {
        return ResponseEntity.badRequest().body(ApiError.de(
                HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "La requête est mal formée ou un paramètre a un type invalide",
                requete.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> traiterIdentifiantsInvalides(BadCredentialsException exception,
                                                                 HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.de(
                HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED",
                "Identifiants invalides", requete.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> traiterRouteInconnue(NoResourceFoundException exception,
                                                          HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.de(
                HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "La ressource demandée est introuvable", requete.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> traiterMethodeNonAutorisee(HttpRequestMethodNotSupportedException exception,
                                                                HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiError.de(
                HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "La méthode HTTP n'est pas autorisée pour cette ressource", requete.getRequestURI()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> traiterTypeDeContenuNonSupporte(HttpMediaTypeNotSupportedException exception,
                                                                     HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiError.de(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Le type de contenu de la requête n'est pas pris en charge", requete.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> traiterInattendue(Exception exception,
                                                      HttpServletRequest requete) {
        JOURNAL.error("Erreur inattendue sur {}", requete.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.de(
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur interne est survenue, contactez l'administrateur",
                requete.getRequestURI()));
    }
}
