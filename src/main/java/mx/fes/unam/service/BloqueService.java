package mx.fes.unam.service;

import mx.fes.unam.model.entities.bloque.BloqueDTO;
import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.repository.BloqueRepository;
import mx.fes.unam.repository.VendedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloqueService {

    private final BloqueRepository bloqueRepository;
    private final VendedorRepository vendedorRepository;
    private final BloqueMapper bloqueMapper;

    public BloqueService(BloqueRepository bloqueRepository,
                         VendedorRepository vendedorRepository,
                         BloqueMapper bloqueMapper) {
        this.bloqueRepository = bloqueRepository;
        this.vendedorRepository = vendedorRepository;
        this.bloqueMapper = bloqueMapper;
    }

    @Transactional(readOnly = true)
    public List<BloqueDTO> listarTodos() {
        return bloqueRepository.findAll().stream()
                .map(bloqueMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BloqueDTO> listarActivos() {
        return bloqueRepository.findByActivoTrue().stream()
                .map(bloqueMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BloqueDTO buscarPorId(Long id) {
        return bloqueRepository.findById(id)
                .map(bloqueMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public BloqueDTO guardar(BloqueDTO dto) {
        BloqueEntity entity = bloqueMapper.toEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }
        BloqueEntity guardado = bloqueRepository.save(entity);
        return bloqueMapper.toDto(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (vendedorRepository.existsByBloqueId(id)) {
            throw new IllegalStateException("No se puede eliminar el bloque porque tiene vendedores asignados.");
        }
        if (!bloqueRepository.existsById(id)) {
            throw new IllegalArgumentException("El bloque no existe.");
        }
        bloqueRepository.deleteById(id);
    }
}
