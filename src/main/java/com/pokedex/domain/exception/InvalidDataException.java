package com.pokedex.domain.exception;

/**
 * Thrown when data breaks a business rule. Mapped to HTTP 400.
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }
}
