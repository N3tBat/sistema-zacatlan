package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.client.ReporteApiClient;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.dto.reporte.ReporteResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Collections;

@Controller
@RequestMapping("/reporte")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteApiClient reporteApiClient;

    @GetMapping("/diario")
    public String diario(@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                         @RequestParam(value = "fechaFin", required = false) String fechaFin,
                         @RequestParam(value = "fecha", required = false) String fecha,
                         Model model) {
        String inicio = fechaInicio != null ? fechaInicio : "";
        String fin = fechaFin != null ? fechaFin : "";
        model.addAttribute("fechaInicio", inicio);
        model.addAttribute("fechaFin", fin);

        try {
            ReporteResponse reporte = reporteApiClient.diario(fechaInicio, fechaFin, fecha);
            aplicarReporte(model, reporte);
        } catch (BackendApiException ex) {
            // Para errores de validación de rango/fechas (400) se muestra en la misma vista.
            if (ex.getStatus() == 400) {
                ApiError apiError = ex.getApiError();
                String mensaje = (apiError != null && apiError.getMensaje() != null && !apiError.getMensaje().isBlank())
                        ? apiError.getMensaje()
                        : "El rango de fechas no es válido.";
                model.addAttribute("errorRango", mensaje);
                model.addAttribute("ventas", Collections.emptyList());
                model.addAttribute("totalPeriodo", BigDecimal.ZERO);
                model.addAttribute("rangoFormateado", "—");
            } else {
                throw ex;
            }
        }
        return "reporte/reporte";
    }

    @GetMapping("/diario/pdf")
    public ResponseEntity<byte[]> generarPdf(@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                             @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                             @RequestParam(value = "fecha", required = false) String fecha) {
        ResponseEntity<byte[]> backendResponse = reporteApiClient.diarioPdf(fechaInicio, fechaFin, fecha);

        HttpHeaders headers = new HttpHeaders();
        if (backendResponse.getHeaders().getContentType() != null) {
            headers.setContentType(backendResponse.getHeaders().getContentType());
        }
        if (backendResponse.getHeaders().getContentDisposition() != null) {
            headers.setContentDisposition(backendResponse.getHeaders().getContentDisposition());
        } else {
            String contentDisposition = backendResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (contentDisposition != null && !contentDisposition.isBlank()) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
        }
        if (backendResponse.getHeaders().getContentLength() >= 0) {
            headers.setContentLength(backendResponse.getHeaders().getContentLength());
        }

        return ResponseEntity.status(backendResponse.getStatusCode())
                .headers(headers)
                .body(backendResponse.getBody());
    }

    private void aplicarReporte(Model model, ReporteResponse reporte) {
        if (reporte == null) {
            model.addAttribute("ventas", Collections.emptyList());
            model.addAttribute("totalPeriodo", BigDecimal.ZERO);
            model.addAttribute("rangoFormateado", "—");
            return;
        }
        model.addAttribute("ventas", reporte.getVentas() != null ? reporte.getVentas() : Collections.emptyList());
        model.addAttribute("totalPeriodo", reporte.getTotalPeriodo() != null ? reporte.getTotalPeriodo() : BigDecimal.ZERO);
        model.addAttribute("rangoFormateado", reporte.getRango() != null ? reporte.getRango() : "—");

        if (reporte.getMensaje() != null && !reporte.getMensaje().isBlank()) {
            model.addAttribute("infoFechas", reporte.getMensaje());
        }
        // Mantener fechas efectivas del reporte si el backend las devuelve
        if (reporte.getFechaInicio() != null) {
            model.addAttribute("fechaInicio", reporte.getFechaInicio().toString());
        }
        if (reporte.getFechaFin() != null) {
            model.addAttribute("fechaFin", reporte.getFechaFin().toString());
        }
    }
}
