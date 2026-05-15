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

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Autowired private ContactService contactService;
    @Autowired private UserService    userService;

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


    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO contact = contactService.getContact(userId, id);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(
            @Valid @RequestBody CreateContactRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO created = contactService.createContact(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        ContactDTO updated = contactService.updateContact(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        contactService.deleteContact(userId, id);
        return ResponseEntity.noContent().build();
    }
}
