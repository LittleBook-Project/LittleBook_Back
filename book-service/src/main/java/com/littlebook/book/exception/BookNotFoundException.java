package com.littlebook.book.exception;

/**
 * Exception levée lorsqu'un livre n'est pas trouvé
 */
public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
