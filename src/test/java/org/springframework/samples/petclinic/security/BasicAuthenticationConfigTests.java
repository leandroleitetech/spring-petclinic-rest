package org.springframework.samples.petclinic.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link BasicAuthenticationConfig}: HTTP Basic authentication on /api/**.
 */
@SpringBootTest
@WebAppConfiguration
class BasicAuthenticationConfigTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void testApiRequestWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/owners"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testApiRequestWithInvalidCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/owners").with(httpBasic("admin", "wrong-password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testApiRequestWithAdminCredentialsIsOk() throws Exception {
        mockMvc.perform(get("/api/owners").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk());
    }

    @Test
    void testApiRequestWithVetCredentialsIsOk() throws Exception {
        mockMvc.perform(get("/api/vets").with(httpBasic("vet", "vet123")))
            .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
