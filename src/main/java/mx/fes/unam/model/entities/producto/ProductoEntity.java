package mx.fes.unam.model.entities.producto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_venta", nullable = false, length = 20)
    private FormaVenta formaVenta;

    @Column(name = "precio_unidad", precision = 10, scale = 2)
    private BigDecimal precioUnidad;

    @Column(name = "precio_bloque", precision = 10, scale = 2)
    private BigDecimal precioBloque;

    @Column(name = "unidades_por_bloque")
    private Integer unidadesPorBloque;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
