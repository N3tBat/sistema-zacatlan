package mx.fes.unam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityWebTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /login es público (HTTP 200)")
    void testLoginEsPublico() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bloque/ver como anónimo redirige al login (HTTP 302)")
    void testBloqueVerAnonimoRedirigeLogin() throws Exception {
        mockMvc.perform(get("/bloque/ver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "vendedor1", roles = {"VENDEDOR"})
    @DisplayName("GET /bloque/ver como ROLE_VENDEDOR devuelve HTTP 403 Forbidden")
    void testBloqueVerVendedorForbidden() throws Exception {
        mockMvc.perform(get("/bloque/ver"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /bloque/ver como ROLE_ADMIN devuelve HTTP 200 OK")
    void testBloqueVerAdminOk() throws Exception {
        mockMvc.perform(get("/bloque/ver"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vendedor1", roles = {"VENDEDOR"})
    @DisplayName("GET /vendedor/ver como ROLE_VENDEDOR devuelve HTTP 403 Forbidden")
    void testVendedorVerVendedorForbidden() throws Exception {
        mockMvc.perform(get("/vendedor/ver"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /vendedor/ver como ROLE_ADMIN devuelve HTTP 200 OK")
    void testVendedorVerAdminOk() throws Exception {
        mockMvc.perform(get("/vendedor/ver"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vendedor1", roles = {"VENDEDOR"})
    @DisplayName("GET / como autenticado devuelve HTTP 200 OK")
    void testInicioAutenticado() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
