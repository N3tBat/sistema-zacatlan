package mx.fes.unam.model.entities.venta;

import jakarta.persistence.*;
import lombok.*;
import mx.fes.unam.model.entities.producto.ProductoEntity;
import mx.fes.unam.model.entities.vendedor.VendedorEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = true)
    private VendedorEntity vendedor;

    @Column(name = "vendedor_nombre", length = 150)
    private String vendedorNombre;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoEntity producto;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_username", length = 50)
    private String adminUsername;

    @Column(length = 500)
    private String movimiento;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
