package mx.fes.unam.api.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {
    private Integer cantidad;
    private BigDecimal precioAplicado;
    private Long productoId;
    private Long vendedorId;
}
