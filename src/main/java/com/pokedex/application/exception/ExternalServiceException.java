package com.pokedex.application.exception;

/**
 * Thrown when an external service (e.g. PokeAPI) fails or is unreachable. Mapped to HTTP 502.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
