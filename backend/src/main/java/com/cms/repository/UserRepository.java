package com.cms.repository;

import com.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Spring Data JPA repository for User entity.
 * Spring auto-generates SQL for all methods at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find user by email (used for login with email) */
    Optional<User> findByEmail(String email);

    /** Find user by phone (used for login with phone number) */
    Optional<User> findByPhone(String phone);

    /** Check if email already registered (used during registration) */
    boolean existsByEmail(String email);

    /** Check if phone already registered (used during registration) */
    boolean existsByPhone(String phone);
}
