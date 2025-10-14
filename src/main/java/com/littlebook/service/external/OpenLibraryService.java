package com.littlebook.service.external;

import com.littlebook.dto.openlibrary.OpenLibraryBookResponse;
import com.littlebook.dto.openlibrary.OpenLibraryBook;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
    private static final String BASE_URL = "https://openlibrary.org/isbn/%s.json";
    private static final String SEARCH_URL = "https://openlibrary.org/search.json?title=%s";
    
    private final RestTemplate restTemplate;
    
    public OpenLibraryService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Récupère les informations d'un livre depuis OpenLibrary par ISBN
     * @param isbn L'ISBN du livre (format 10 ou 13 chiffres)
     * @return Les données du livre ou null si introuvable
     * @throws IllegalArgumentException si l'ISBN est invalide
     */
    public OpenLibraryBookResponse fetchBookByIsbn(String isbn) {
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
            logger.info("🌐 Appel vers OpenLibrary API: {}", url);
            
            // L'API directe retourne directement un livre, pas un wrapper
            OpenLibraryBook book = restTemplate.getForObject(url, OpenLibraryBook.class);
            
            if (book != null) {
                logger.info("✅ Livre trouvé sur OpenLibrary pour ISBN: {}", cleanIsbn);
                // Créer un wrapper pour uniformiser avec le reste du code
                OpenLibraryBookResponse response = new OpenLibraryBookResponse();
                response.addBook("ISBN:" + cleanIsbn, book);
                return response;
            } else {
                logger.warn("❌ Aucun livre trouvé sur OpenLibrary pour ISBN: {}", cleanIsbn);
                return null;
            }
            
        } catch (RestClientException e) {
            logger.error("💥 Erreur lors de l'appel à OpenLibrary pour l'ISBN {}: {}", cleanIsbn, e.getMessage());
            return null; // Retourne null au lieu de lancer une exception pour éviter de casser l'app
        }
    }
    /**
 * Recherche des livres par auteur sur OpenLibrary
 */
public List<OpenLibraryBook> searchBooksByAuthor(String author) {
    if (author == null || author.trim().isEmpty())
        throw new IllegalArgumentException("L'auteur ne peut pas être vide");
    try {
        String url = "https://openlibrary.org/search.json?author=" + author.replace(" ", "+");
        logger.info("🌐 Recherche de livres par auteur sur OpenLibrary: {}", url);
        var response = restTemplate.getForObject(url, Map.class);
        if (response != null && response.containsKey("docs")) {
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");
            List<OpenLibraryBook> books = new ArrayList<>();
            for (Map<String, Object> doc : docs) {
                OpenLibraryBook book = new OpenLibraryBook();
                book.setTitle((String) doc.get("title"));
                book.setAuthor((List<String>) doc.getOrDefault("author_name", new ArrayList<>()));
                book.setPublishers((List<String>) doc.getOrDefault("publisher", new ArrayList<>()));
                book.setPublishDate(doc.get("first_publish_year") != null ? doc.get("first_publish_year").toString() : null);
                book.setIsbn10((List<String>) doc.getOrDefault("isbn", new ArrayList<>()));
                if (doc.get("cover_i") != null) {
                    book.setCovers(List.of(Long.valueOf(doc.get("cover_i").toString())));
                }
                books.add(book);
            }
            return books;
        }
    } catch (Exception e) {
        logger.error("Erreur lors de la recherche par auteur sur OpenLibrary: {}", e.getMessage());
    }
    return new ArrayList<>();
}

    
    /**
     * Valide le format d'un ISBN (10 ou 13 chiffres)
     */
    private boolean isValidIsbn(String isbn) {
        return isbn.matches("\\d{10}|\\d{13}");
    }

    /**
     * Recherche des livres par titre sur OpenLibrary
     * @param title Le titre à rechercher
     * @return Liste des livres trouvés (peut être vide)
     */
    public List<OpenLibraryBook> searchBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }
        try {
            String url = String.format(SEARCH_URL, title.replace(" ", "+"));
            logger.info("🌐 Recherche de livres par titre sur OpenLibrary: {}", url);
            // La réponse est un objet JSON avec un champ 'docs' qui est une liste de livres
            var response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("docs")) {
                List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");
                List<OpenLibraryBook> books = new ArrayList<>();
                for (Map<String, Object> doc : docs) {
                    OpenLibraryBook book = new OpenLibraryBook();
                    book.setTitle((String) doc.get("title"));
                    book.setAuthor((List<String>) doc.getOrDefault("author_name", new ArrayList<>()));
                    book.setPublishers((List<String>) doc.getOrDefault("publisher", new ArrayList<>()));
                    Object publishYear = doc.get("first_publish_year");
                    book.setPublishDate(publishYear != null ? publishYear.toString() : null);
                    book.setIsbn10((List<String>) doc.getOrDefault("isbn", new ArrayList<>()));
                    // Couverture
                    if (doc.get("cover_i") != null) {
                        List<Long> covers = new ArrayList<>();
                        covers.add(Long.valueOf(doc.get("cover_i").toString()));
                        book.setCovers(covers);
                    }
                    books.add(book);
                }
                return books;
            }
        } catch (Exception e) {
            logger.error("💥 Erreur lors de la recherche par titre sur OpenLibrary: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
}
