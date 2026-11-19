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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /reporte/diario con rango de fechas devuelve HTTP 200 OK")
    void testReporteRangoFechasAdminOk() throws Exception {
        mockMvc.perform(get("/reporte/diario")
                        .param("fechaInicio", "2026-11-01")
                        .param("fechaFin", "2026-11-12"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /vendedor/modificar/3 muestra el bloque de solo lectura (HTTP 200)")
    void testModificarVendedorAdminOk() throws Exception {
        mockMvc.perform(get("/vendedor/modificar/3"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /producto/modificar/1 muestra el selector de bloques con el bloque actual")
    void testModificarProductoMuestraBloques() throws Exception {
        mockMvc.perform(get("/producto/modificar/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bloques"))
                .andExpect(content().string(containsString("Seleccione un bloque")))
                .andExpect(content().string(containsString("Comida")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Rango invertido: muestra el error y NO muestra ventas")
    void testReporteRangoInvertidoNoMuestraVentas() throws Exception {
        mockMvc.perform(get("/reporte/diario")
                        .param("fechaInicio", "2026-11-12")
                        .param("fechaFin", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorRango", "La fecha inicial no puede ser mayor que la fecha final."))
                .andExpect(model().attribute("ventas", hasSize(0)))
                .andExpect(content().string(not(containsString("Carlos Hernández López"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Fecha con formato inválido: muestra el error y NO muestra ventas")
    void testReporteFechaInvalidaNoMuestraVentas() throws Exception {
        mockMvc.perform(get("/reporte/diario")
                        .param("fechaInicio", "2026-13-45")
                        .param("fechaFin", "2026-11-12"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorRango",
                        "Las fechas no son válidas. Use el formato AAAA-MM-DD (por ejemplo, 2026-11-12)."))
                .andExpect(model().attribute("ventas", hasSize(0)))
                .andExpect(content().string(not(containsString("Carlos Hernández López"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Sin fechas: se muestra el día de hoy sin error")
    void testReporteSinFechasUsaHoy() throws Exception {
        mockMvc.perform(get("/reporte/diario"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("errorRango"))
                .andExpect(model().attributeExists("ventas"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PDF con rango invertido: redirige al reporte sin generar archivo")
    void testPdfRangoInvertidoRedirige() throws Exception {
        mockMvc.perform(get("/reporte/diario/pdf")
                        .param("fechaInicio", "2026-11-12")
                        .param("fechaFin", "2026-11-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reporte/diario"));
    }
}
