package mx.fes.unam;

import mx.fes.unam.model.entities.bloque.BloqueDTO;
import mx.fes.unam.model.entities.vendedor.VendedorDTO;
import mx.fes.unam.security.RolEnum;
import mx.fes.unam.security.VendedorDetailsService;
import mx.fes.unam.service.BloqueService;
import mx.fes.unam.service.VendedorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonaATests {

    @Autowired
    private VendedorDetailsService vendedorDetailsService;

    @Autowired
    private BloqueService bloqueService;

    @Autowired
    private VendedorService vendedorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("VendedorDetailsService carga admin con ROLE_ADMIN y activo")
    void testLoadAdmin() {
        UserDetails admin = vendedorDetailsService.loadUserByUsername("admin");
        assertNotNull(admin);
        assertTrue(admin.isEnabled());
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()));
        assertTrue(admin.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("VendedorDetailsService carga municipal con ROLE_ADMIN y activo")
    void testLoadMunicipal() {
        UserDetails municipal = vendedorDetailsService.loadUserByUsername("municipal");
        assertNotNull(municipal);
        assertTrue(municipal.isEnabled());
        assertTrue(passwordEncoder.matches("admin123", municipal.getPassword()));
        assertTrue(municipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("VendedorDetailsService carga vendedor1 con ROLE_VENDEDOR y activo")
    void testLoadVendedor1() {
        UserDetails v1 = vendedorDetailsService.loadUserByUsername("vendedor1");
        assertNotNull(v1);
        assertTrue(v1.isEnabled());
        assertTrue(passwordEncoder.matches("vendedor123", v1.getPassword()));
        assertTrue(v1.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR")));
    }

    @Test
    @DisplayName("VendedorDetailsService carga vendedor9 inactivo (disabled)")
    void testLoadVendedor9Inactivo() {
        UserDetails v9 = vendedorDetailsService.loadUserByUsername("vendedor9");
        assertNotNull(v9);
        assertFalse(v9.isEnabled(), "vendedor9 debe estar inactivo (enabled=false)");
    }

    @Test
    @DisplayName("Listar vendedores solo retorna usuarios con rol VENDEDOR (excluye ADMIN)")
    void testListarVendedoresExcluyeAdmin() {
        List<VendedorDTO> lista = vendedorService.listarVendedores();
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(v -> v.getRol() == RolEnum.VENDEDOR));
        assertTrue(lista.stream().noneMatch(v -> v.getUsername().equals("admin")));
        assertTrue(lista.stream().noneMatch(v -> v.getUsername().equals("municipal")));
    }

    @Test
    @DisplayName("Bloque con vendedores asignados rechaza la eliminación")
    void testEliminarBloqueConVendedores() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bloqueService.eliminar(2L); // Textiles tiene vendedores asignados y aún no tiene productos
        });
        assertTrue(ex.getMessage().contains("tiene vendedores asignados"));
    }

    @Test
    @DisplayName("Bloque con productos asignados rechaza la eliminación")
    void testEliminarBloqueConProductos() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bloqueService.eliminar(1L); // Comida tiene productos y vendedores asignados
        });
        assertTrue(ex.getMessage().contains("tiene productos asignados"));
    }

    @Test
    @Transactional
    @DisplayName("Bloque sin vendedores ni productos se elimina exitosamente")
    void testEliminarBloqueVacio() {
        BloqueDTO temporal = bloqueService.guardar(BloqueDTO.builder()
                .nombre("Bloque Temporal de Prueba")
                .activo(true)
                .build());
        assertDoesNotThrow(() -> bloqueService.eliminar(temporal.getId()));
        assertNull(bloqueService.buscarPorId(temporal.getId()));
    }

    @Test
    @Transactional
    @DisplayName("Desactivar y reactivar vendedor modifica activo y fechaBaja")
    void testDesactivarYReactivar() {
        // Desactivar vendedor1
        vendedorService.desactivar(3L);
        VendedorDTO v1 = vendedorService.buscarPorId(3L);
        assertFalse(v1.getActivo());
        assertNotNull(v1.getFechaBaja());

        // Reactivar vendedor1
        vendedorService.reactivar(3L);
        v1 = vendedorService.buscarPorId(3L);
        assertTrue(v1.getActivo());
        assertNull(v1.getFechaBaja());
    }

    @Test
    @Transactional
    @DisplayName("Al modificar un vendedor no se puede cambiar el bloque asignado")
    void testModificarVendedorNoCambiaBloque() {
        // El vendedor 3 (vendedor1) tiene asignado el bloque 1 (Comida)
        VendedorDTO dto = vendedorService.buscarPorId(3L);
        dto.setBloqueId(2L); // intento de cambio de bloque
        dto.setNombre("Carlos Modificado");

        vendedorService.guardar(dto, null);

        VendedorDTO actualizado = vendedorService.buscarPorId(3L);
        assertEquals("Carlos Modificado", actualizado.getNombre());
        assertEquals(Long.valueOf(1L), actualizado.getBloqueId(),
                "El bloque debe conservarse aunque el formulario envíe otro distinto");
    }

    @Test
    @DisplayName("Rechazar eliminación de un usuario ADMIN")
    void testRechazarEliminarAdmin() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            vendedorService.eliminar(1L);
        });
        assertTrue(ex.getMessage().contains("Administrador"));
    }
}
