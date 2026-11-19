package mx.fes.unam.api.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {
    private Long id;
    private Long vendedorId;
    private Long productoId;
    private Long adminId;
    private LocalDateTime fecha;
    private LocalDateTime fechaModificacion;
    private Integer cantidad;
    private BigDecimal precioAplicado;
    private BigDecimal total;
    private String vendedorNombre;
    private String bloqueNombre;
    private String productoNombre;
    private String adminUsername;
    private String movimiento;
}
