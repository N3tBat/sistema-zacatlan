package mx.fes.unam.api.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private String formaVenta;
    private BigDecimal precioUnidad;
    private BigDecimal precioBloque;
    private Integer unidadesPorBloque;
    private Boolean activo;
    private Long bloqueId;
}
