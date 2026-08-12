package com.example.modelbench.exception;

/**
 * Levee lorsqu'une ressource ne peut pas etre supprimee parce qu'elle est referencee ailleurs.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
