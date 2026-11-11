package mx.fes.unam.model.entities.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String descripcion;

    @NotNull(message = "La forma de venta es obligatoria")
    private FormaVenta formaVenta;

    @PositiveOrZero(message = "El precio por unidad no puede ser negativo")
    private BigDecimal precioUnidad;

    @PositiveOrZero(message = "El precio por bloque no puede ser negativo")
    private BigDecimal precioBloque;

    @PositiveOrZero(message = "Las unidades por bloque no pueden ser negativas")
    private Integer unidadesPorBloque;

    @Builder.Default
    private Boolean activo = true;
}
