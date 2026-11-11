package mx.fes.unam.service;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.entities.vendedor.VendedorDTO;
import mx.fes.unam.model.entities.vendedor.VendedorEntity;
import mx.fes.unam.model.entities.venta.VentaEntity;
import mx.fes.unam.repository.BloqueRepository;
import mx.fes.unam.repository.VendedorRepository;
import mx.fes.unam.repository.VentaRepository;
import mx.fes.unam.security.RolEnum;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VendedorService {

    private final VendedorRepository vendedorRepository;
    private final BloqueRepository bloqueRepository;
    private final VentaRepository ventaRepository;
    private final VendedorMapper vendedorMapper;
    private final PasswordEncoder passwordEncoder;

    public VendedorService(VendedorRepository vendedorRepository,
                           BloqueRepository bloqueRepository,
                           VentaRepository ventaRepository,
                           VendedorMapper vendedorMapper,
                           PasswordEncoder passwordEncoder) {
        this.vendedorRepository = vendedorRepository;
        this.bloqueRepository = bloqueRepository;
        this.ventaRepository = ventaRepository;
        this.vendedorMapper = vendedorMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<VendedorDTO> listarVendedores() {
        return vendedorRepository.findByRol(RolEnum.VENDEDOR).stream()
                .map(vendedorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendedorDTO buscarPorId(Long id) {
        return vendedorRepository.findById(id)
                .map(vendedorMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public VendedorDTO guardar(VendedorDTO dto, String password) {
        BloqueEntity bloque = null;
        if (dto.getBloqueId() != null) {
            bloque = bloqueRepository.findById(dto.getBloqueId()).orElse(null);
        }

        if (dto.getId() == null) {
            // ALTA
            if (vendedorRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya está registrado.");
            }

            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()
                    && vendedorRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El correo electrónico '" + dto.getEmail() + "' ya está registrado.");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos vendedores.");
            }

            dto.setFechaAlta(LocalDateTime.now());
            dto.setActivo(true);
            dto.setFechaBaja(null);
            dto.setRol(RolEnum.VENDEDOR);

            VendedorEntity entity = vendedorMapper.toEntity(dto, bloque);
            entity.setPassword(passwordEncoder.encode(password));

            VendedorEntity guardado = vendedorRepository.save(entity);
            return vendedorMapper.toDto(guardado);
        } else {
            // MODIFICAR
            VendedorEntity existing = vendedorRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con ID: " + dto.getId()));

            Optional<VendedorEntity> byUsername = vendedorRepository.findByUsername(dto.getUsername());
            if (byUsername.isPresent() && !byUsername.get().getId().equals(dto.getId())) {
                throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya pertenece a otro usuario.");
            }

            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                Optional<VendedorEntity> byEmail = vendedorRepository.findByEmail(dto.getEmail());
                if (byEmail.isPresent() && !byEmail.get().getId().equals(dto.getId())) {
                    throw new IllegalArgumentException("El correo electrónico '" + dto.getEmail() + "' ya pertenece a otro usuario.");
                }
            }

            existing.setNombre(dto.getNombre());
            existing.setApellidos(dto.getApellidos());
            existing.setTelefono(dto.getTelefono());
            existing.setEmail(dto.getEmail());
            existing.setUsername(dto.getUsername());
            existing.setBloque(bloque);

            // Si llega password se actualiza con BCrypt; si llega vacío se conserva el hash actual
            if (password != null && !password.trim().isEmpty()) {
                existing.setPassword(passwordEncoder.encode(password));
            }

            VendedorEntity guardado = vendedorRepository.save(existing);
            return vendedorMapper.toDto(guardado);
        }
    }

    @Transactional
    public void desactivar(Long id) {
        VendedorEntity vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado."));

        if (vendedor.getRol() == RolEnum.ADMIN) {
            throw new IllegalStateException("No se puede desactivar a un administrador.");
        }

        vendedor.setActivo(false);
        vendedor.setFechaBaja(LocalDateTime.now());
        vendedorRepository.save(vendedor);
    }

    @Transactional
    public void reactivar(Long id) {
        VendedorEntity vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado."));

        vendedor.setActivo(true);
        vendedor.setFechaBaja(null);
        vendedorRepository.save(vendedor);
    }

    @Transactional
    public void eliminar(Long id) {
        VendedorEntity vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con ID: " + id));

        if (vendedor.getRol() == RolEnum.ADMIN) {
            throw new IllegalStateException("No se puede eliminar a un usuario con rol de Administrador.");
        }

        List<VentaEntity> ventas = ventaRepository.findByVendedorId(id);
        for (VentaEntity venta : ventas) {
            venta.setVendedor(null);
        }
        ventaRepository.saveAll(ventas);

        vendedorRepository.delete(vendedor);
    }
}
