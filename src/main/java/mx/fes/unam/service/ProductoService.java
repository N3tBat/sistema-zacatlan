package mx.fes.unam.service;

import mx.fes.unam.model.entities.producto.FormaVenta;
import mx.fes.unam.model.entities.producto.ProductoDTO;
import mx.fes.unam.model.entities.producto.ProductoEntity;
import mx.fes.unam.repository.ProductoRepository;
import mx.fes.unam.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository,
                           VentaRepository ventaRepository,
                           ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public ProductoDTO guardar(ProductoDTO dto) {
        validarFormaVenta(dto);

        if (dto.getId() == null) {
            // ALTA
            ProductoEntity entity = productoMapper.toEntity(dto);
            ProductoEntity guardado = productoRepository.save(entity);
            return productoMapper.toDto(guardado);
        }

        // MODIFICAR
        ProductoEntity existing = productoRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + dto.getId()));

        existing.setNombre(dto.getNombre());
        existing.setDescripcion(dto.getDescripcion());
        existing.setFormaVenta(dto.getFormaVenta());
        existing.setPrecioUnidad(dto.getPrecioUnidad());
        existing.setPrecioBloque(dto.getPrecioBloque());
        existing.setUnidadesPorBloque(dto.getUnidadesPorBloque());
        existing.setActivo(dto.getActivo() != null ? dto.getActivo() : existing.getActivo());

        ProductoEntity guardado = productoRepository.save(existing);
        return productoMapper.toDto(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        if (ventaRepository.existsByProductoId(id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque tiene ventas registradas.");
        }
        productoRepository.deleteById(id);
    }

    private void validarFormaVenta(ProductoDTO dto) {
        if (dto.getFormaVenta() == FormaVenta.UNIDAD
                && (dto.getPrecioUnidad() == null || dto.getPrecioUnidad().signum() <= 0)) {
            throw new IllegalArgumentException("El precio por unidad es obligatorio cuando la forma de venta es por UNIDAD.");
        }
        if (dto.getFormaVenta() == FormaVenta.BLOQUE) {
            if (dto.getPrecioBloque() == null || dto.getPrecioBloque().signum() <= 0) {
                throw new IllegalArgumentException("El precio por bloque es obligatorio cuando la forma de venta es por BLOQUE.");
            }
            if (dto.getUnidadesPorBloque() == null || dto.getUnidadesPorBloque() <= 0) {
                throw new IllegalArgumentException("Debe indicar cuántas unidades agrupa un bloque.");
            }
        }
    }
}
