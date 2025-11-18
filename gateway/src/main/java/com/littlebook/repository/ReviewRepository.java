package com.littlebook.repository;

import com.littlebook.entity.ReviewEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    /**
     * Récupère toutes les reviews d'un utilisateur
     */
    List<ReviewEntity> findByUser(UserEntity user);

    /**
     * Récupère toutes les reviews d'un livre
     */
    List<ReviewEntity> findByBook(BookEntity book);

    /**
     * Récupère toutes les reviews d'un livre par ISBN
     */
    @Query("SELECT r FROM ReviewEntity r WHERE r.book.isbn = :isbn")
    List<ReviewEntity> findByBookIsbn(@Param("isbn") String isbn);

    /**
     * Récupère toutes les reviews d'un utilisateur par UUID
     */
    @Query("SELECT r FROM ReviewEntity r WHERE r.user.uuid = :userUuid")
    List<ReviewEntity> findByUserUuid(@Param("userUuid") java.util.UUID userUuid);

    /**
     * Calcule la note moyenne d'un livre
     */
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.book.isbn = :isbn")
    Double getAverageRatingByBookIsbn(@Param("isbn") String isbn);

    /**
     * Compte le nombre de reviews d'un livre
     */
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.book.isbn = :isbn")
    Long countByBookIsbn(@Param("isbn") String isbn);

    /**
     * Vérifie si un utilisateur a déjà reviewé un livre
     */
    @Query("SELECT COUNT(r) > 0 FROM ReviewEntity r WHERE r.user.uuid = :userUuid AND r.book.isbn = :isbn")
    boolean existsByUserUuidAndBookIsbn(@Param("userUuid") java.util.UUID userUuid, @Param("isbn") String isbn);
}