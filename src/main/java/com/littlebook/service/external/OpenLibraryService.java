package com.littlebook.service.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour interagir avec l'API OpenLibrary
 * Documentation API: https://openlibrary.org/dev/docs/api/books
 */
@Service
public class OpenLibraryService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryService.class);
    private static final String BASE_URL = "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";
    
    private final RestTemplate restTemplate;
    
    public OpenLibraryService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Récupère les informations d'un livre depuis OpenLibrary par ISBN
     * @param isbn L'ISBN du livre (format 10 ou 13 chiffres)
     * @return Les données JSON du livre ou null si introuvable
     * @throws IllegalArgumentException si l'ISBN est invalide
     */
    public String fetchBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("L'ISBN ne peut pas être vide");
        }
        
        // Nettoyage de l'ISBN (suppression des tirets/espaces)
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");
        
        if (!isValidIsbn(cleanIsbn)) {
            throw new IllegalArgumentException("Format ISBN invalide: " + isbn);
        }
        
        try {
            String url = String.format(BASE_URL, cleanIsbn);
            logger.debug("Appel vers OpenLibrary: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            logger.debug("Réponse reçue pour ISBN {}: {}", cleanIsbn, response);
            
            return response;
            
        } catch (RestClientException e) {
            logger.error("Erreur lors de l'appel à OpenLibrary pour l'ISBN {}: {}", cleanIsbn, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des données du livre", e);
        }
    }
    
    /**
     * Valide le format d'un ISBN (10 ou 13 chiffres)
     */
    private boolean isValidIsbn(String isbn) {
        return isbn.matches("\\d{10}|\\d{13}");
    }
}
