package com.cms.controller;

import com.cms.dto.*;
import com.cms.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ContactController — REST API for contact CRUD operations.
 *
 * Base path: /api/v1/contacts
 * All endpoints require a valid Bearer JWT token.
 *
 * Endpoints:
 *   GET    /contacts              → list contacts (paginated + search)
 *   GET    /contacts/{id}        → get single contact
 *   POST   /contacts             → create contact
 *   PUT    /contacts/{id}        → update contact
 *   DELETE /contacts/{id}        → delete contact
 */
@RestController
@RequestMapping("/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Autowired private ContactService contactService;
    @Autowired private UserService    userService;

    /**
     * GET /contacts?page=0&size=10&search=john&sortBy=firstName
     *
     * Returns a paginated list of the logged-in user's contacts.
     * Optionally filtered by name with the 'search' parameter.
     *
     * @param page   page number, 0-based (default: 0)
     * @param size   page size (default: 10)
     * @param search optional name filter
     * @param sortBy sort field (default: firstName)
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ContactDTO>> getContacts(
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "10")         int    size,
            @RequestParam(defaultValue = "")           String search,
            @RequestParam(defaultValue = "firstName")  String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        PagedResponse<ContactDTO> response = contactService.getContacts(userId, search, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /contacts/{id}
     *
     * Returns the full detail of a single contact.
     * Returns 404 if not found, 403 if it belongs to another user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO contact = contactService.getContact(userId, id);
        return ResponseEntity.ok(contact);
    }

    /**
     * POST /contacts
     *
     * Creates a new contact for the logged-in user.
     * Returns 201 Created with the saved contact.
     *
     * Request body: { firstName, lastName, title, emails[], phones[] }
     */
    @PostMapping
    public ResponseEntity<ContactDTO> createContact(
            @Valid @RequestBody CreateContactRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO created = contactService.createContact(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /contacts/{id}
     *
     * Updates an existing contact.
     * Returns 200 OK with the updated contact.
     * Returns 404 if not found, 403 if wrong user.
     *
     * Request body: { firstName?, lastName?, title?, emails[]?, phones[]? }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO updated = contactService.updateContact(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /contacts/{id}
     *
     * Deletes a contact and all its associated emails/phones.
     * Returns 204 No Content on success.
     * Returns 404 if not found, 403 if wrong user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        contactService.deleteContact(userId, id);
        return ResponseEntity.noContent().build();
    }
}
