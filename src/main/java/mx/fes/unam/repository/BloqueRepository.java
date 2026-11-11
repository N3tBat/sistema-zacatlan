package mx.fes.unam.repository;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueRepository extends JpaRepository<BloqueEntity, Long> {

    List<BloqueEntity> findByActivoTrue();
}
