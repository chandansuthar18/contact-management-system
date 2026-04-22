package com.cms.service;

import com.cms.config.JwtUtil;
import com.cms.dto.*;
import com.cms.entity.User;
import com.cms.exception.*;
import com.cms.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceTest — unit tests for AuthService.
 *
 * Uses @ExtendWith(MockitoExtension.class) to enable Mockito without Spring context.
 * @Mock creates a mock object (fake dependency).
 * @InjectMocks creates the real AuthService and injects mocks into it.
 *
 * HOW TO RUN:
 *   mvn test -Dtest=AuthServiceTest
 *   OR right-click in IntelliJ → Run 'AuthServiceTest'
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private JwtUtil         jwtUtil;

    // Use real PasswordEncoder (BCrypt) so encode/matches actually work
    @Spy  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    // ── Register Tests ────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully with email")
        void shouldRegisterWithEmail() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@test.com").password("password123")
                .build();

            when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });
            when(jwtUtil.generateToken("john@test.com")).thenReturn("mock-jwt-token");

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals("mock-jwt-token", response.getToken());
            assertEquals("John", response.getUser().getFirstName());
            assertEquals("john@test.com", response.getUser().getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should register user successfully with phone only")
        void shouldRegisterWithPhone() {
            RegisterRequest request = RegisterRequest.builder()
                .firstName("Jane").phone("+15550001").password("pass123").build();

            when(userRepository.existsByPhone("+15550001")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0); u.setId(2L); return u;
            });
            when(jwtUtil.generateToken("+15550001")).thenReturn("phone-token");

            AuthResponse response = authService.register(request);
            assertNotNull(response);
            assertEquals("phone-token", response.getToken());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowWhenEmailDuplicate() {
            RegisterRequest request = RegisterRequest.builder()
                .email("dup@test.com").password("pass").build();
            when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> authService.register(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when phone already exists")
        void shouldThrowWhenPhoneDuplicate() {
            RegisterRequest request = RegisterRequest.builder()
                .phone("+15550002").password("pass").build();
            when(userRepository.existsByPhone("+15550002")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when neither email nor phone provided")
        void shouldThrowWhenNoIdentifier() {
            RegisterRequest request = RegisterRequest.builder()
                .firstName("NoId").password("pass").build();

            assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        }
    }

    // ── Login Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        private User existingUser;

        @BeforeEach
        void setUp() {
            existingUser = User.builder()
                .id(1L).firstName("Alice").email("alice@test.com")
                .password(new BCryptPasswordEncoder().encode("correct-pass"))
                .build();
        }

        @Test
        @DisplayName("Should login successfully with correct credentials")
        void shouldLoginSuccessfully() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(existingUser));
            when(jwtUtil.generateToken("alice@test.com")).thenReturn("login-token");

            LoginRequest request = new LoginRequest("alice@test.com", "correct-pass");
            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals("login-token", response.getToken());
            assertEquals("Alice", response.getUser().getFirstName());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException for wrong password")
        void shouldThrowForWrongPassword() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(existingUser));

            assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("alice@test.com", "wrong-pass")));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException for non-existent user")
        void shouldThrowForUnknownUser() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(userRepository.findByPhone(any())).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("ghost@test.com", "pass")));
        }
    }

    // ── Change Password Tests ─────────────────────────────────

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            User user = User.builder().id(1L)
                .password(new BCryptPasswordEncoder().encode("oldpass"))
                .build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpass123");
            authService.changePassword(1L, request);

            verify(userRepository).save(user);
            assertTrue(new BCryptPasswordEncoder().matches("newpass123", user.getPassword()));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException for wrong current password")
        void shouldThrowForWrongCurrentPassword() {
            User user = User.builder().id(1L)
                .password(new BCryptPasswordEncoder().encode("realpass"))
                .build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            assertThrows(InvalidCredentialsException.class,
                () -> authService.changePassword(1L, new ChangePasswordRequest("wrongpass", "newpass")));
        }
    }
}
