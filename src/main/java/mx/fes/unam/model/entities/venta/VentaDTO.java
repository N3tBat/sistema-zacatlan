package mx.fes.unam.model.entities.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {

    private Long id;

    private LocalDateTime fecha;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El precio aplicado es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private BigDecimal precioAplicado;

    private BigDecimal total;

    private Long vendedorId;

    private String vendedorNombre;

    private String bloqueNombre;

    @NotNull(message = "Debe seleccionar un producto")
    private Long productoId;

    private String productoNombre;

    private Long adminId;

    private String adminUsername;

    private String movimiento;

    private LocalDateTime fechaModificacion;
}
