package com.littlebook.repository;

import com.littlebook.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, String> {

    /**
     * Recherche des livres par nom (insensible à la casse)
     */
    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<BookEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Recherche des livres par auteur (insensible à la casse)
     */
    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<BookEntity> findByAuthorContainingIgnoreCase(@Param("author") String author);

    /**
     * Recherche des livres par éditeur (insensible à la casse)
     */
    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))")
    List<BookEntity> findByPublisherContainingIgnoreCase(@Param("publisher") String publisher);

    /**
     * Recherche des livres par type/genre
     */
    List<BookEntity> findByTypeIgnoreCase(String type);

    /**
     * Vérifie si un livre existe par ISBN
     */
    boolean existsByIsbn(String isbn);
}