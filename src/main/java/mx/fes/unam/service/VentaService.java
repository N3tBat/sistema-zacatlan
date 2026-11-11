package mx.fes.unam.service;

import mx.fes.unam.model.entities.producto.ProductoEntity;
import mx.fes.unam.model.entities.vendedor.VendedorDTO;
import mx.fes.unam.model.entities.vendedor.VendedorEntity;
import mx.fes.unam.model.entities.venta.VentaDTO;
import mx.fes.unam.model.entities.venta.VentaEntity;
import mx.fes.unam.repository.ProductoRepository;
import mx.fes.unam.repository.VendedorRepository;
import mx.fes.unam.repository.VentaRepository;
import mx.fes.unam.security.RolEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final VentaMapper ventaMapper;
    private final VendedorMapper vendedorMapper;

    public VentaService(VentaRepository ventaRepository,
                        VendedorRepository vendedorRepository,
                        ProductoRepository productoRepository,
                        VentaMapper ventaMapper,
                        VendedorMapper vendedorMapper) {
        this.ventaRepository = ventaRepository;
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
        this.ventaMapper = ventaMapper;
        this.vendedorMapper = vendedorMapper;
    }

    // ------------------------------------------------------------------
    // Alta de venta: fecha y total SIEMPRE los asigna/calcula el servidor
    // ------------------------------------------------------------------
    @Transactional
    public VentaDTO registrar(VentaDTO dto) {
        if (dto.getVendedorId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un vendedor.");
        }
        VendedorEntity vendedor = vendedorRepository.findById(dto.getVendedorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado."));

        // Regla: verificar vendedor activo ante cualquier movimiento
        if (!Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalStateException("El vendedor está inactivo, no puede realizar movimientos");
        }

        if (dto.getProductoId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto.");
        }
        ProductoEntity producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (dto.getPrecioAplicado() == null || dto.getPrecioAplicado().signum() <= 0) {
            throw new IllegalArgumentException("El precio aplicado debe ser mayor a cero.");
        }

        VentaEntity entity = new VentaEntity();
        entity.setFecha(LocalDateTime.now());
        entity.setCantidad(dto.getCantidad());
        entity.setPrecioAplicado(dto.getPrecioAplicado());
        entity.setTotal(calcularTotal(dto.getCantidad(), dto.getPrecioAplicado()));
        entity.setVendedor(vendedor);
        entity.setVendedorNombre(vendedor.getNombre() + " " + vendedor.getApellidos());
        entity.setProducto(producto);

        VentaEntity guardado = ventaRepository.save(entity);
        return ventaMapper.toDto(guardado);
    }

    // ------------------------------------------------------------------
    // Listados
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<VentaDTO> listarTodos() {
        return ventaRepository.findAll().stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarPorVendedorUsername(String username) {
        return vendedorRepository.findByUsername(username)
                .map(v -> ventaRepository.findByVendedorId(v.getId()).stream()
                        .map(ventaMapper::toDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        return ventaRepository.findVentasDelDiaConRelaciones(inicio, fin).stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VentaDTO buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .map(ventaMapper::toDto)
                .orElse(null);
    }

    // ------------------------------------------------------------------
    // Modificar venta: SOLO el admin llega aquí (regla de SecurityConfig).
    // Llena la bitácora: adminId + texto concatenado de lo que modificó.
    // ------------------------------------------------------------------
    @Transactional
    public VentaDTO modificar(Long id, VentaDTO dto, String adminUsername) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));

        // Regla: verificar vendedor activo ante cualquier movimiento (también al modificar)
        VendedorEntity vendedor = venta.getVendedor();
        if (vendedor != null && !Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalStateException("El vendedor está inactivo, no puede realizar movimientos");
        }

        if (dto.getProductoId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto.");
        }
        ProductoEntity productoNuevo = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (dto.getPrecioAplicado() == null || dto.getPrecioAplicado().signum() <= 0) {
            throw new IllegalArgumentException("El precio aplicado debe ser mayor a cero.");
        }

        // Texto concatenado del diff: valor anterior → nuevo, solo campos que cambiaron
        StringBuilder diff = new StringBuilder();
        if (!venta.getProducto().getId().equals(productoNuevo.getId())) {
            diff.append("producto: ").append(venta.getProducto().getNombre())
                    .append(" → ").append(productoNuevo.getNombre()).append("; ");
        }
        if (!venta.getCantidad().equals(dto.getCantidad())) {
            diff.append("cantidad: ").append(venta.getCantidad())
                    .append(" → ").append(dto.getCantidad()).append("; ");
        }
        if (venta.getPrecioAplicado().compareTo(dto.getPrecioAplicado()) != 0) {
            diff.append("precioAplicado: ").append(venta.getPrecioAplicado().toPlainString())
                    .append(" → ").append(dto.getPrecioAplicado().toPlainString()).append("; ");
        }

        BigDecimal totalNuevo = calcularTotal(dto.getCantidad(), dto.getPrecioAplicado());
        if (venta.getTotal().compareTo(totalNuevo) != 0) {
            diff.append("total: ").append(venta.getTotal().toPlainString())
                    .append(" → ").append(totalNuevo.toPlainString());
        }

        // Aplicar los cambios
        venta.setProducto(productoNuevo);
        venta.setCantidad(dto.getCantidad());
        venta.setPrecioAplicado(dto.getPrecioAplicado());
        venta.setTotal(totalNuevo);

        // Bitácora del admin (sin FK: sobrevive aunque la cuenta cambie o se borre)
        VendedorEntity admin = vendedorRepository.findByUsername(adminUsername).orElse(null);
        Long adminId = admin != null ? admin.getId() : null;
        venta.setAdminId(adminId);
        venta.setAdminUsername(adminUsername);

        String texto = "ADMIN " + (adminId != null ? adminId : "?") + " (" + adminUsername + ")"
                + " | MODIFICÓ VENTA " + id + " | "
                + (diff.isEmpty() ? "sin cambios en los campos" : diff.toString());
        venta.setMovimiento(texto.length() > 500 ? texto.substring(0, 500) : texto);
        venta.setFechaModificacion(LocalDateTime.now());

        VentaEntity guardado = ventaRepository.save(venta);
        return ventaMapper.toDto(guardado);
    }

    // ------------------------------------------------------------------
    // Helpers del módulo de venta (no tocan el módulo de la Persona A)
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<VendedorDTO> listarVendedoresActivos() {
        return vendedorRepository.findByRolAndActivoTrue(RolEnum.VENDEDOR).stream()
                .map(vendedorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendedorDTO buscarVendedorPorUsername(String username) {
        return vendedorRepository.findByUsername(username)
                .map(vendedorMapper::toDto)
                .orElse(null);
    }

    private BigDecimal calcularTotal(Integer cantidad, BigDecimal precioAplicado) {
        return BigDecimal.valueOf(cantidad).multiply(precioAplicado).setScale(2, RoundingMode.HALF_UP);
    }
}
