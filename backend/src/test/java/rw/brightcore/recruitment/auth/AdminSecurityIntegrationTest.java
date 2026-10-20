package rw.brightcore.recruitment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import rw.brightcore.recruitment.IntegrationTestSupport;

class AdminSecurityIntegrationTest extends IntegrationTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {"/api/admin/overview", "/api/admin/jobs", "/api/admin/jobs/1/applications",
            "/api/admin/applications/1", "/api/admin/applications/1/cv", "/api/auth/me"})
    void adminEndpointsRequireLogin(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in to continue."));
    }

    @Test
    void creatingAJobRequiresLogin() throws Exception {
        mvc.perform(post("/api/admin/jobs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        login("admin@test.rw", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void successfulLoginSetsAnHttpOnlyCookieThatGrantsAccess() throws Exception {
        MvcResult result = login("ADMIN@test.rw", "Correct-Horse-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.rw"))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("admin_token=", "HttpOnly", "SameSite=Strict");

        Cookie cookie = result.getResponse().getCookie(AuthCookies.NAME);
        mvc.perform(get("/api/admin/overview").cookie(cookie)).andExpect(status().isOk());
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        mvc.perform(get("/api/admin/overview").cookie(new Cookie(AuthCookies.NAME, "eyJhbGciOiJIUzI1NiJ9.e30.bad")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accountIsTemporarilyLockedAfterRepeatedFailures() throws Exception {
        String email = "locked-out@test.rw";
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES; i++) {
            login(email, "nope").andExpect(status().isUnauthorized());
        }
        login(email, "nope").andExpect(status().isTooManyRequests());
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }
}
