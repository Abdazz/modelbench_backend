package com.example.modelbench.exception;

import com.example.modelbench.dto.ApiError;
import com.example.modelbench.dto.ApiFieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
                "La validation de la requete a echoue",
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
                "L'operation viole une contrainte d'unicite ou d'integrite",
                requete.getRequestURI()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> traiterRequeteMalformee(Exception exception,
                                                            HttpServletRequest requete) {
        return ResponseEntity.badRequest().body(ApiError.de(
                HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "La requete est mal formee ou un parametre a un type invalide",
                requete.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> traiterIdentifiantsInvalides(BadCredentialsException exception,
                                                                 HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.de(
                HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED",
                "Identifiants invalides", requete.getRequestURI()));
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
