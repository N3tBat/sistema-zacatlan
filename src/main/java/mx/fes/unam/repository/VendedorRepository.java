package mx.fes.unam.repository;

import mx.fes.unam.model.entities.vendedor.VendedorEntity;
import mx.fes.unam.security.RolEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<VendedorEntity, Long> {

    Optional<VendedorEntity> findByUsername(String username);

    List<VendedorEntity> findByRol(RolEnum rol);

    List<VendedorEntity> findByRolAndActivoTrue(RolEnum rol);

    boolean existsByBloqueId(Long bloqueId);

    boolean existsByUsername(String username);

    Optional<VendedorEntity> findByEmail(String email);
}
