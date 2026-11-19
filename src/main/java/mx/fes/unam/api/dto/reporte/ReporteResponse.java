package mx.fes.unam.api.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.api.dto.venta.VentaResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponse {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String rango;
    private Integer movimientos;
    private BigDecimal totalPeriodo;
    private String mensaje;
    private List<VentaResponse> ventas;
}
