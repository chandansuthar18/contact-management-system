package com.cms.service;

import com.cms.dto.*;
import com.cms.entity.*;
import com.cms.exception.*;
import com.cms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ContactService — core business logic for contact management.
 *
 * Responsibilities:
 *   - Get contacts (paginated + search)
 *   - Get single contact detail
 *   - Create new contact
 *   - Update existing contact
 *   - Delete contact
 *   - Ownership validation (user can only access their own contacts)
 */
@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    @Autowired private ContactRepository contactRepository;
    @Autowired private UserRepository    userRepository;

    // ─────────────────────────────────────────────────────────
    // GET ALL CONTACTS (Paginated + Search)
    // ─────────────────────────────────────────────────────────

    /**
     * Fetch paginated contacts for a user, with optional name search.
     *
     * @param userId  logged-in user's ID
     * @param search  optional search string (first or last name)
     * @param page    page number (0-based)
     * @param size    page size
     * @param sortBy  field to sort by (default: firstName)
     * @return PagedResponse<ContactDTO>
     */
    public PagedResponse<ContactDTO> getContacts(
            Long userId, String search, int page, int size, String sortBy) {

        log.info("Fetching contacts: userId={}, search='{}', page={}, size={}", userId, search, page, size);

        // Build pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        // Use search query if search string provided, otherwise fetch all
        Page<Contact> contactPage;
        if (StringUtils.hasText(search)) {
            contactPage = contactRepository.searchByName(userId, search, pageable);
        } else {
            contactPage = contactRepository.findByUserId(userId, pageable);
        }

        log.debug("Found {} contacts (total: {})", contactPage.getNumberOfElements(), contactPage.getTotalElements());

        // Map entities to DTOs
        List<ContactDTO> dtos = contactPage.getContent()
            .stream()
            .map(this::toContactDTO)
            .collect(Collectors.toList());

        return PagedResponse.<ContactDTO>builder()
            .content(dtos)
            .page(contactPage.getNumber())
            .size(contactPage.getSize())
            .totalElements(contactPage.getTotalElements())
            .totalPages(contactPage.getTotalPages())
            .last(contactPage.isLast())
            .build();
    }

    // ─────────────────────────────────────────────────────────
    // GET SINGLE CONTACT
    // ─────────────────────────────────────────────────────────

    /**
     * Get a single contact by ID, verifying it belongs to the logged-in user.
     */
    public ContactDTO getContact(Long userId, Long contactId) {
        log.info("Fetching contact: userId={}, contactId={}", userId, contactId);

        Contact contact = findContactByIdAndUserId(contactId, userId);
        return toContactDTO(contact);
    }

    // ─────────────────────────────────────────────────────────
    // CREATE CONTACT
    // ─────────────────────────────────────────────────────────

    /**
     * Create a new contact for the logged-in user.
     *
     * Steps:
     *   1. Load the User entity
     *   2. Build Contact + emails + phones
     *   3. Save (cascade saves child records too)
     */
    @Transactional
    public ContactDTO createContact(Long userId, CreateContactRequest request) {
        log.info("Creating contact: userId={}, name='{} {}'",
            userId, request.getFirstName(), request.getLastName());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Build the Contact entity
        Contact contact = Contact.builder()
            .user(user)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .title(request.getTitle())
            .build();

        // Map email DTOs → ContactEmail entities
        if (request.getEmails() != null) {
            List<ContactEmail> emails = request.getEmails().stream()
                .map(dto -> ContactEmail.builder()
                    .contact(contact)
                    .label(dto.getLabel())
                    .email(dto.getEmail())
                    .build())
                .collect(Collectors.toList());
            contact.setEmails(emails);
        }

        // Map phone DTOs → ContactPhone entities
        if (request.getPhones() != null) {
            List<ContactPhone> phones = request.getPhones().stream()
                .map(dto -> ContactPhone.builder()
                    .contact(contact)
                    .label(dto.getLabel())
                    .phone(dto.getPhone())
                    .build())
                .collect(Collectors.toList());
            contact.setPhones(phones);
        }

        Contact saved = contactRepository.save(contact);
        log.info("Contact created: id={}", saved.getId());
        return toContactDTO(saved);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE CONTACT
    // ─────────────────────────────────────────────────────────

    /**
     * Update an existing contact.
     *
     * Strategy for emails/phones:
     *   - Clear existing child records (orphanRemoval=true deletes them)
     *   - Add new ones from the request
     *   This avoids complex diff logic and is safe for small collections.
     */
    @Transactional
    public ContactDTO updateContact(Long userId, Long contactId, UpdateContactRequest request) {
        log.info("Updating contact: userId={}, contactId={}", userId, contactId);

        Contact contact = findContactByIdAndUserId(contactId, userId);

        // Update basic fields only if provided (partial update support)
        if (StringUtils.hasText(request.getFirstName())) contact.setFirstName(request.getFirstName());
        if (request.getLastName() != null)               contact.setLastName(request.getLastName());
        if (request.getTitle()    != null)               contact.setTitle(request.getTitle());

        // Replace emails
        if (request.getEmails() != null) {
            contact.getEmails().clear();  // orphanRemoval=true → deletes old rows
            request.getEmails().forEach(dto ->
                contact.getEmails().add(
                    ContactEmail.builder()
                        .contact(contact)
                        .label(dto.getLabel())
                        .email(dto.getEmail())
                        .build()));
        }

        // Replace phones
        if (request.getPhones() != null) {
            contact.getPhones().clear();
            request.getPhones().forEach(dto ->
                contact.getPhones().add(
                    ContactPhone.builder()
                        .contact(contact)
                        .label(dto.getLabel())
                        .phone(dto.getPhone())
                        .build()));
        }

        Contact updated = contactRepository.save(contact);
        log.info("Contact updated: id={}", updated.getId());
        return toContactDTO(updated);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE CONTACT
    // ─────────────────────────────────────────────────────────

    /**
     * Delete a contact by ID, after verifying ownership.
     * CascadeType.ALL removes associated emails and phones automatically.
     */
    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        log.info("Deleting contact: userId={}, contactId={}", userId, contactId);

        Contact contact = findContactByIdAndUserId(contactId, userId);
        contactRepository.delete(contact);

        log.info("Contact deleted: id={}", contactId);
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Find a contact and verify it belongs to the user.
     * Throws ResourceNotFoundException (404) if not found.
     * Throws AccessDeniedException (403) if wrong user.
     */
    private Contact findContactByIdAndUserId(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));

        if (!contact.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access contact {} owned by user {}",
                userId, contactId, contact.getUser().getId());
            throw new com.cms.exception.AccessDeniedException(
                "You do not have permission to access this contact");
        }
        return contact;
    }

    /** Convert Contact entity → ContactDTO */
    private ContactDTO toContactDTO(Contact contact) {
        List<EmailEntryDTO> emails = contact.getEmails().stream()
            .map(e -> EmailEntryDTO.builder().label(e.getLabel()).email(e.getEmail()).build())
            .collect(Collectors.toList());

        List<PhoneEntryDTO> phones = contact.getPhones().stream()
            .map(p -> PhoneEntryDTO.builder().label(p.getLabel()).phone(p.getPhone()).build())
            .collect(Collectors.toList());

        return ContactDTO.builder()
            .id(contact.getId())
            .firstName(contact.getFirstName())
            .lastName(contact.getLastName())
            .title(contact.getTitle())
            .emails(emails)
            .phones(phones)
            .createdAt(contact.getCreatedAt())
            .updatedAt(contact.getUpdatedAt())
            .build();
    }
}
