package com.cms.service;

import com.cms.entity.User;
import com.cms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsService
 *
 * Spring Security calls loadUserByUsername() on EVERY authenticated request
 * to verify the user still exists in the database.
 *
 * The "username" here is either email or phone (whichever was used to log in).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Load a user by email or phone number.
     * Spring Security uses the returned UserDetails to verify the JWT.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", identifier);

        // Try email first, then phone
        User user = userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByPhone(identifier))
            .orElseThrow(() -> {
                log.warn("User not found: {}", identifier);
                return new UsernameNotFoundException("User not found: " + identifier);
            });

        // Return Spring Security's UserDetails wrapper
        // Collections.emptyList() = no roles/authorities (simple setup)
        return new org.springframework.security.core.userdetails.User(
            // Use email if available, otherwise phone as the principal name
            user.getEmail() != null ? user.getEmail() : user.getPhone(),
            user.getPassword(),
            Collections.emptyList()
        );
    }
}
