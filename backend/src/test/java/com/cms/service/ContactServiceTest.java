package com.cms.service;

import com.cms.dto.*;
import com.cms.entity.*;
import com.cms.exception.*;
import com.cms.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ContactServiceTest — unit tests for ContactService.
 *
 * HOW TO RUN:
 *   mvn test -Dtest=ContactServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Unit Tests")
class ContactServiceTest {

    @Mock private ContactRepository contactRepository;
    @Mock private UserRepository    userRepository;

    @InjectMocks
    private ContactService contactService;

    private User   testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).firstName("Alex").email("alex@test.com").build();

        testContact = Contact.builder()
            .id(10L)
            .user(testUser)
            .firstName("John")
            .lastName("Doe")
            .title("Engineer")
            .emails(new ArrayList<>(List.of(
                ContactEmail.builder().id(1L).label("work").email("john@corp.com").build())))
            .phones(new ArrayList<>(List.of(
                ContactPhone.builder().id(1L).label("work").phone("+15550001").build())))
            .build();
    }

    // ── getContacts() ─────────────────────────────────────────

    @Nested
    @DisplayName("getContacts()")
    class GetContactsTests {

        @Test
        @DisplayName("Should return paginated contacts for user")
        void shouldReturnPaginatedContacts() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName"));
            Page<Contact> mockPage = new PageImpl<>(List.of(testContact), pageable, 1);
            when(contactRepository.findByUserId(eq(1L), any())).thenReturn(mockPage);

            PagedResponse<ContactDTO> response = contactService.getContacts(1L, "", 0, 10, "firstName");

            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals("John", response.getContent().get(0).getFirstName());
        }

        @Test
        @DisplayName("Should use search query when search string provided")
        void shouldUseSearchQuery() {
            Page<Contact> mockPage = new PageImpl<>(List.of(testContact));
            when(contactRepository.searchByName(eq(1L), eq("john"), any())).thenReturn(mockPage);

            PagedResponse<ContactDTO> response = contactService.getContacts(1L, "john", 0, 10, "firstName");

            verify(contactRepository).searchByName(eq(1L), eq("john"), any());
            verify(contactRepository, never()).findByUserId(any(), any());
            assertEquals(1, response.getContent().size());
        }

        @Test
        @DisplayName("Should return empty page when no contacts found")
        void shouldReturnEmptyPage() {
            Page<Contact> emptyPage = new PageImpl<>(Collections.emptyList());
            when(contactRepository.findByUserId(eq(1L), any())).thenReturn(emptyPage);

            PagedResponse<ContactDTO> response = contactService.getContacts(1L, "", 0, 10, "firstName");

            assertTrue(response.getContent().isEmpty());
            assertEquals(0, response.getTotalElements());
        }
    }

    // ── getContact() ──────────────────────────────────────────

    @Nested
    @DisplayName("getContact()")
    class GetContactTests {

        @Test
        @DisplayName("Should return contact when found and owned by user")
        void shouldReturnContact() {
            when(contactRepository.findById(10L)).thenReturn(Optional.of(testContact));

            ContactDTO result = contactService.getContact(1L, 10L);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("John", result.getFirstName());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when contact not found")
        void shouldThrowWhenNotFound() {
            when(contactRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> contactService.getContact(1L, 999L));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when contact belongs to another user")
        void shouldThrowWhenWrongUser() {
            when(contactRepository.findById(10L)).thenReturn(Optional.of(testContact));

            // User 99 tries to access contact owned by user 1
            assertThrows(com.cms.exception.AccessDeniedException.class,
                () -> contactService.getContact(99L, 10L));
        }
    }

    // ── createContact() ───────────────────────────────────────

    @Nested
    @DisplayName("createContact()")
    class CreateContactTests {

        @Test
        @DisplayName("Should create contact with emails and phones")
        void shouldCreateContact() {
            CreateContactRequest request = CreateContactRequest.builder()
                .firstName("Jane").lastName("Smith").title("Designer")
                .emails(List.of(new EmailEntryDTO("work", "jane@corp.com")))
                .phones(List.of(new PhoneEntryDTO("mobile", "+15550002")))
                .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(20L);
                return c;
            });

            ContactDTO result = contactService.createContact(1L, request);

            assertNotNull(result);
            assertEquals("Jane", result.getFirstName());
            assertEquals("Smith", result.getLastName());
            assertEquals(1, result.getEmails().size());
            assertEquals("work", result.getEmails().get(0).getLabel());
            verify(contactRepository).save(any(Contact.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> contactService.createContact(999L, new CreateContactRequest()));
        }
    }

    // ── updateContact() ───────────────────────────────────────

    @Nested
    @DisplayName("updateContact()")
    class UpdateContactTests {

        @Test
        @DisplayName("Should update contact fields")
        void shouldUpdateContact() {
            UpdateContactRequest request = UpdateContactRequest.builder()
                .firstName("Johnny").title("Senior Engineer")
                .emails(List.of(new EmailEntryDTO("work", "johnny@corp.com")))
                .phones(List.of(new PhoneEntryDTO("work", "+15550099")))
                .build();

            when(contactRepository.findById(10L)).thenReturn(Optional.of(testContact));
            when(contactRepository.save(any())).thenReturn(testContact);

            ContactDTO result = contactService.updateContact(1L, 10L, request);

            assertEquals("Johnny", testContact.getFirstName());
            assertEquals("Senior Engineer", testContact.getTitle());
            verify(contactRepository).save(testContact);
        }
    }

    // ── deleteContact() ───────────────────────────────────────

    @Nested
    @DisplayName("deleteContact()")
    class DeleteContactTests {

        @Test
        @DisplayName("Should delete contact successfully")
        void shouldDeleteContact() {
            when(contactRepository.findById(10L)).thenReturn(Optional.of(testContact));

            assertDoesNotThrow(() -> contactService.deleteContact(1L, 10L));
            verify(contactRepository).delete(testContact);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when deleting another user's contact")
        void shouldThrowForWrongUser() {
            when(contactRepository.findById(10L)).thenReturn(Optional.of(testContact));

            assertThrows(com.cms.exception.AccessDeniedException.class,
                () -> contactService.deleteContact(99L, 10L));
            verify(contactRepository, never()).delete(any());
        }
    }
}
