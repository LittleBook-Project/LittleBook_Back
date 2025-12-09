package com.littlebook.book.exception;

/**
 * Exception levée lors d'une erreur avec l'API OpenLibrary
 */
public class OpenLibraryException extends RuntimeException {
    
    public OpenLibraryException(String message) {
        super(message);
    }
    
    public OpenLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
