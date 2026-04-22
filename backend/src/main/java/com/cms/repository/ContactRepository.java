package com.cms.repository;

import com.cms.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ContactRepository — JPA repository for Contact entity.
 * Provides paginated and search-enabled queries.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Fetch all contacts for a user (paginated).
     * Used when search string is empty.
     */
    Page<Contact> findByUserId(Long userId, Pageable pageable);

    /**
     * Search contacts by first name OR last name (case-insensitive), paginated.
     * JPQL query joins across the relationship.
     */
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contact> searchByName(
        @Param("userId") Long userId,
        @Param("search") String search,
        Pageable pageable
    );

    /**
     * Count total contacts for a user.
     */
    long countByUserId(Long userId);

    /**
     * Check ownership — does this contact belong to this user?
     */
    boolean existsByIdAndUserId(Long contactId, Long userId);
}
