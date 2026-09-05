package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.repository.UserRepository;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for password change and reset endpoints.
 */
@SpringBootTest
@WebAppConfiguration
@Transactional
class AuthRestControllerTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void testChangePasswordWithValidCredentialsSucceeds() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("admin123", "newpass123", "newpass123")))
            .andExpect(status().isNoContent());
    }

    @Test
    void testChangePasswordWithInvalidCurrentPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("wrong-password", "newpass123", "newpass123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordWithMismatchedConfirmationReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("admin123", "newpass123", "different")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordSameAsCurrentReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("admin123", "admin123", "admin123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordTooShortReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("admin123", "short", "short")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordWithoutAuthenticationIsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChange("admin123", "newpass123", "newpass123")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testPasswordResetRequestSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void testPasswordResetRequestForUnknownUserSucceedsWithoutRevealingExistence() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void testPasswordResetConfirmWithValidTokenSucceeds() throws Exception {
        String token = requestResetToken("vet");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset(token, "resetpass123", "resetpass123")))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPasswordResetConfirmWithInvalidTokenFails() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset("invalid-token", "resetpass123", "resetpass123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testPasswordResetConfirmWithExpiredTokenFails() throws Exception {
        String token = requestResetToken("admin");

        User user = userRepository.findByUsername("admin");
        user.setResetTokenExpiry(Instant.now().minus(1, ChronoUnit.MINUTES));
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset(token, "resetpass123", "resetpass123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testPasswordResetTokenCannotBeReused() throws Exception {
        String token = requestResetToken("vet");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset(token, "resetpass123", "resetpass123")))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset(token, "resetpass123", "resetpass123")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testPasswordResetChangesPasswordAndOldPasswordNoLongerWorks() throws Exception {
        String token = requestResetToken("admin");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReset(token, "newadmin123", "newadmin123")))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/owners").with(httpBasic("admin", "admin123")))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/owners").with(httpBasic("admin", "newadmin123")))
            .andExpect(status().isOk());
    }

    private String requestResetToken(String username) throws Exception {
        String response = mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractToken(response);
    }

    private String extractToken(String tokenResponse) {
        return tokenResponse.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private String jsonChange(String current, String newPassword, String confirm) {
        return String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\",\"confirmPassword\":\"%s\"}",
            current, newPassword, confirm);
    }

    private String jsonReset(String token, String newPassword, String confirm) {
        return String.format("{\"token\":\"%s\",\"newPassword\":\"%s\",\"confirmPassword\":\"%s\"}",
            token, newPassword, confirm);
    }
}
