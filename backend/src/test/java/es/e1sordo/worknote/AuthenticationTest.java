package es.e1sordo.worknote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAuthenticationRedirect() throws Exception {
        // First we make a GET request to /settings as an unauthenticated user
        mockMvc.perform(get("/settings?continue"))
                .andExpect(status().is3xxRedirection()) // Wait for redirection
                .andExpect(redirectedUrl("http://localhost/login")); // Expect redirection to login page

        // Now let's perform successful authentication with the test user
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password")
                        .sessionAttr("SPRING_SECURITY_SAVED_REQUEST", new SimpleSavedRequest("/settings?continue")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/")); // Expect redirection to home page
    }
}
