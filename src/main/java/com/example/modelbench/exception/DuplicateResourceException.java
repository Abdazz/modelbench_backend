package com.example.modelbench.exception;

/**
 * Levee lorsqu'une contrainte d'unicite metier serait violee.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
