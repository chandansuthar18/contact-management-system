package com.cms.controller;

import com.cms.dto.*;
import com.cms.service.AuthService;
import com.cms.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerTest — integration tests for AuthController endpoints.
 *
 * @WebMvcTest loads only the web layer (controller + security), not the full app.
 * MockMvc simulates HTTP requests without starting a real server.
 *
 * HOW TO RUN:
 *   mvn test -Dtest=AuthControllerTest
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockBean AuthService authService;
    @MockBean UserService userService;

    private UserDTO mockUser;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockUser = UserDTO.builder()
            .id(1L).firstName("Alex").lastName("Jordan")
            .email("alex@test.com").build();

        mockAuthResponse = AuthResponse.builder()
            .token("test-jwt-token")
            .tokenType("Bearer")
            .user(mockUser)
            .build();
    }

    // ── POST /auth/register ───────────────────────────────────

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 with token on valid registration")
        void shouldReturn201OnValidRegistration() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                .firstName("Alex").email("alex@test.com").password("pass123").build();

            when(authService.register(any())).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("alex@test.com"));
        }

        @Test
        @DisplayName("Should return 400 when firstName is missing")
        void shouldReturn400WhenFirstNameMissing() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@test.com");
            request.setPassword("pass123");
            // firstName intentionally omitted

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    // ── POST /auth/login ──────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 with token on valid login")
        void shouldReturn200OnValidLogin() throws Exception {
            LoginRequest request = new LoginRequest("alex@test.com", "pass123");
            when(authService.login(any())).thenReturn(mockAuthResponse);

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.user.firstName").value("Alex"));
        }

        @Test
        @DisplayName("Should return 400 when fields are blank")
        void shouldReturn400WhenFieldsBlank() throws Exception {
            LoginRequest request = new LoginRequest("", "");

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    // ── PUT /auth/change-password ─────────────────────────────

    @Nested
    @DisplayName("PUT /auth/change-password")
    class ChangePasswordEndpointTests {

        @Test
        @WithMockUser(username = "alex@test.com")
        @DisplayName("Should return 200 on successful password change")
        void shouldReturn200OnPasswordChange() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpass123");

            when(userService.getUserIdByIdentifier("alex@test.com")).thenReturn(1L);

            mockMvc.perform(put("/auth/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }
    }
}
