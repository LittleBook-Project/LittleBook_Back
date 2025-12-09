package com.littlebook.book.service.external;

import com.littlebook.book.dto.openlibrary.OpenLibrarySearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Client pour interagir avec l'API OpenLibrary.
 * Gère la recherche de livres par titre, auteur, et ISBN.
 */
@Component
public class OpenLibraryClient {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryClient.class);
    
    private final RestClient restClient;
    private final String baseUrl;
    private final int timeout;
    
    public OpenLibraryClient(RestClient.Builder builder,
                            @Value("${openlibrary.api.base-url}") String baseUrl,
                            @Value("${openlibrary.api.timeout:5000}") int timeout) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.restClient = builder.build();
    }
    
    /**
     * Recherche des livres par titre
     * @param title Titre (ou partie du titre)
     * @param limit Nombre max de résultats
     * @return Réponse OpenLibrary
     */
    public OpenLibrarySearchResponse searchByTitle(String title, int limit) {
        try {
            String url = baseUrl + "?title=" + encodeParam(title) + "&limit=" + limit;
            logger.info("Searching OpenLibrary by title: {}", title);
            
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
                    
        } catch (RestClientException e) {
            logger.error("OpenLibrary API error while searching by title: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Recherche des livres par auteur
     * @param author Nom de l'auteur
     * @param limit Nombre max de résultats
     * @return Réponse OpenLibrary
     */
    public OpenLibrarySearchResponse searchByAuthor(String author, int limit) {
        try {
            String url = baseUrl + "?author=" + encodeParam(author) + "&limit=" + limit;
            logger.info("Searching OpenLibrary by author: {}", author);
            
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
                    
        } catch (RestClientException e) {
            logger.error("OpenLibrary API error while searching by author: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Recherche des livres par ISBN
     * @param isbn ISBN (10 ou 13)
     * @return Réponse OpenLibrary
     */
    public OpenLibrarySearchResponse searchByIsbn(String isbn) {
        try {
            String url = baseUrl + "?isbn=" + encodeParam(isbn);
            logger.info("Searching OpenLibrary by ISBN: {}", isbn);
            
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
                    
        } catch (RestClientException e) {
            logger.error("OpenLibrary API error while searching by ISBN: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Recherche générique avec plusieurs critères
     * @param title Titre (optionnel)
     * @param author Auteur (optionnel)
     * @param isbn ISBN (optionnel)
     * @param limit Nombre max de résultats
     * @return Réponse OpenLibrary
     */
    public OpenLibrarySearchResponse search(String title, String author, String isbn, int limit) {
        try {
            StringBuilder urlBuilder = new StringBuilder(baseUrl).append("?limit=").append(limit);
            
            if (title != null && !title.isBlank()) {
                urlBuilder.append("&title=").append(encodeParam(title));
            }
            if (author != null && !author.isBlank()) {
                urlBuilder.append("&author=").append(encodeParam(author));
            }
            if (isbn != null && !isbn.isBlank()) {
                urlBuilder.append("&isbn=").append(encodeParam(isbn));
            }
            
            logger.info("Searching OpenLibrary with: title={}, author={}, isbn={}", title, author, isbn);
            
            return restClient.get()
                    .uri(urlBuilder.toString())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
                    
        } catch (RestClientException e) {
            logger.error("OpenLibrary API error: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Encode un paramètre pour l'URL (encodage URL standard)
     */
    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.warn("Error encoding parameter: {}", param, e);
            return param.replace(" ", "+");
        }
    }
}
