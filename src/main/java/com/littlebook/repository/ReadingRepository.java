package com.littlebook.repository;

import com.littlebook.entity.ReadingEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.entity.BookEntity;
import com.littlebook.enums.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRepository extends JpaRepository<ReadingEntity, Long> {

    /**
     * Récupère toutes les lectures d'un utilisateur
     */
    List<ReadingEntity> findByUser(UserEntity user);

    /**
     * Récupère toutes les lectures d'un utilisateur par UUID
     */
    @Query("SELECT r FROM ReadingEntity r WHERE r.user.uuid = :userUuid")
    List<ReadingEntity> findByUserUuid(@Param("userUuid") java.util.UUID userUuid);

    /**
     * Récupère toutes les lectures d'un utilisateur avec un statut donné
     */
    @Query("SELECT r FROM ReadingEntity r WHERE r.user.uuid = :userUuid AND r.status = :status")
    List<ReadingEntity> findByUserUuidAndStatus(@Param("userUuid") java.util.UUID userUuid, @Param("status") ReadingStatus status);

    /**
     * Récupère une lecture spécifique d'un utilisateur pour un livre
     */
    @Query("SELECT r FROM ReadingEntity r WHERE r.user.uuid = :userUuid AND r.book.isbn = :isbn")
    Optional<ReadingEntity> findByUserUuidAndBookIsbn(@Param("userUuid") java.util.UUID userUuid, @Param("isbn") String isbn);

    /**
     * Récupère toutes les lectures d'un livre par ISBN
     */
    @Query("SELECT r FROM ReadingEntity r WHERE r.book.isbn = :isbn")
    List<ReadingEntity> findByBookIsbn(@Param("isbn") String isbn);

    /**
     * Compte le nombre de lecteurs d'un livre
     */
    @Query("SELECT COUNT(DISTINCT r.user) FROM ReadingEntity r WHERE r.book.isbn = :isbn")
    Long countReadersByBookIsbn(@Param("isbn") String isbn);

    /**
     * Compte le nombre de livres lus par un utilisateur
     */
    @Query("SELECT COUNT(r) FROM ReadingEntity r WHERE r.user.uuid = :userUuid AND r.status = 'READ'")
    Long countBooksReadByUser(@Param("userUuid") java.util.UUID userUuid);

    /**
     * Vérifie si un utilisateur a une lecture en cours pour un livre
     */
    @Query("SELECT COUNT(r) > 0 FROM ReadingEntity r WHERE r.user.uuid = :userUuid AND r.book.isbn = :isbn")
    boolean existsByUserUuidAndBookIsbn(@Param("userUuid") java.util.UUID userUuid, @Param("isbn") String isbn);
}