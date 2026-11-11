package mx.fes.unam;

import mx.fes.unam.model.entities.producto.ProductoDTO;
import mx.fes.unam.model.entities.venta.VentaDTO;
import mx.fes.unam.service.PdfVentaService;
import mx.fes.unam.service.ProductoService;
import mx.fes.unam.service.VentaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonaBTests {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private PdfVentaService pdfVentaService;

    // ------------------------------------------------------------------
    // Productos
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Producto por UNIDAD exige precio por unidad")
    void testProductoUnidadSinPrecio() {
        ProductoDTO dto = ProductoDTO.builder()
                .nombre("Producto prueba")
                .formaVenta(mx.fes.unam.model.entities.producto.FormaVenta.UNIDAD)
                .precioUnidad(null)
                .activo(true)
                .build();
        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(dto));
    }

    @Test
    @DisplayName("Producto por BLOQUE exige precio por bloque y unidades por bloque")
    void testProductoBloqueSinPrecio() {
        ProductoDTO dto = ProductoDTO.builder()
                .nombre("Producto prueba")
                .formaVenta(mx.fes.unam.model.entities.producto.FormaVenta.BLOQUE)
                .precioBloque(null)
                .activo(true)
                .build();
        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(dto));
    }

    @Test
    @DisplayName("No se puede eliminar un producto con ventas registradas")
    void testEliminarProductoConVentas() {
        // El producto 1 (Manzana Royale) tiene ventas en data.sql
        assertThrows(IllegalStateException.class, () -> productoService.eliminar(1L));
    }

    // ------------------------------------------------------------------
    // Ventas
    // ------------------------------------------------------------------
    @Test
    @Transactional
    @DisplayName("Registrar venta con vendedor inactivo es rechazado")
    void testRegistrarVentaVendedorInactivo() {
        // El vendedor 11 (Fernando Ortiz Vargas) está inactivo en data.sql
        VentaDTO dto = VentaDTO.builder()
                .vendedorId(11L)
                .productoId(4L)
                .cantidad(2)
                .precioAplicado(new BigDecimal("15.00"))
                .build();

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ventaService.registrar(dto));
        assertEquals("El vendedor está inactivo, no puede realizar movimientos", e.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("Registrar venta: fecha, total y snapshot los asigna el servidor")
    void testRegistrarVenta() {
        VentaDTO dto = VentaDTO.builder()
                .vendedorId(3L)   // Carlos Hernández López (activo)
                .productoId(4L)   // Pan de Huevo (UNIDAD)
                .cantidad(3)
                .precioAplicado(new BigDecimal("15.00"))
                .build();

        VentaDTO guardada = ventaService.registrar(dto);

        assertNotNull(guardada.getId());
        assertNotNull(guardada.getFecha());   // asignada por el servidor
        assertEquals(0, guardada.getTotal().compareTo(new BigDecimal("45.00"))); // calculado por el servidor
        assertEquals("Carlos Hernández López", guardada.getVendedorNombre());    // snapshot
        assertEquals("Pan de Huevo", guardada.getProductoNombre());
    }

    @Test
    @DisplayName("El vendedor logueado solo ve sus propias ventas")
    void testListarPorVendedorUsername() {
        List<VentaDTO> ventas = ventaService.listarPorVendedorUsername("vendedor1");
        assertNotNull(ventas);
        assertTrue(ventas.stream().allMatch(v -> "Carlos Hernández López".equals(v.getVendedorNombre())));
    }

    @Test
    @DisplayName("Las ventas de vendedores eliminados siguen listándose (snapshot)")
    void testVentasConVendedorBorrado() {
        List<VentaDTO> todas = ventaService.listarTodos();
        assertTrue(todas.stream().anyMatch(v -> v.getVendedorId() == null && v.getVendedorNombre() != null));
    }

    @Test
    @Transactional
    @DisplayName("Modificar venta (admin) llena la bitácora con el texto concatenado")
    void testModificarVenta() {
        VentaDTO modificada = ventaService.modificar(1L, VentaDTO.builder()
                .vendedorId(3L)
                .productoId(1L)
                .cantidad(5)
                .precioAplicado(new BigDecimal("450.00"))
                .build(), "admin");

        assertNotNull(modificada.getMovimiento());
        assertTrue(modificada.getMovimiento().contains("MODIFICÓ VENTA 1"));
        assertTrue(modificada.getMovimiento().contains("cantidad: 2 → 5"));
        assertEquals("admin", modificada.getAdminUsername());
        assertEquals(Long.valueOf(1L), modificada.getAdminId());
        assertNotNull(modificada.getFechaModificacion());
        assertEquals(0, modificada.getTotal().compareTo(new BigDecimal("2250.00")));
    }

    @Test
    @Transactional
    @DisplayName("Modificar venta de vendedor inactivo es rechazado")
    void testModificarVentaVendedorInactivo() {
        // La venta 9 pertenece al vendedor 11 (inactivo)
        assertThrows(IllegalStateException.class, () -> ventaService.modificar(9L, VentaDTO.builder()
                .vendedorId(11L)
                .productoId(7L)
                .cantidad(1)
                .precioAplicado(new BigDecimal("70.00"))
                .build(), "admin"));
    }

    // ------------------------------------------------------------------
    // Reporte PDF
    // ------------------------------------------------------------------
    @Test
    @DisplayName("El PDF del reporte diario se genera correctamente")
    void testPdfReporte() {
        List<VentaDTO> hoy = ventaService.listarPorFecha(java.time.LocalDate.now());
        byte[] pdf = pdfVentaService.generatePdf(hoy);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals('%', pdf[0]);  // firma %PDF
        assertEquals('P', pdf[1]);
        assertEquals('D', pdf[2]);
        assertEquals('F', pdf[3]);
    }
}
