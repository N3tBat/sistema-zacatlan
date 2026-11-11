package mx.fes.unam.repository;

import mx.fes.unam.model.entities.venta.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaEntity, Long> {

    List<VentaEntity> findByVendedorId(Long vendedorId);

    boolean existsByProductoId(Long productoId);

    List<VentaEntity> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<VentaEntity> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM VentaEntity v LEFT JOIN FETCH v.vendedor LEFT JOIN FETCH v.producto WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<VentaEntity> findVentasDelDiaConRelaciones(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
