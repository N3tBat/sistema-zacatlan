package mx.fes.unam.controller;

import mx.fes.unam.model.entities.venta.VentaDTO;
import mx.fes.unam.service.PdfVentaService;
import mx.fes.unam.service.VentaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/reporte")
public class ReporteController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentaService ventaService;
    private final PdfVentaService pdfVentaService;

    public ReporteController(VentaService ventaService, PdfVentaService pdfVentaService) {
        this.ventaService = ventaService;
        this.pdfVentaService = pdfVentaService;
    }

    // Reporte de ventas por rango de fechas (por defecto, el día de hoy)
    @GetMapping("/diario")
    public String diario(@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                         @RequestParam(value = "fechaFin", required = false) String fechaFin,
                         @RequestParam(value = "fecha", required = false) String fecha,
                         Model model) {
        ResultadoRango resultado = resolverRango(fechaInicio, fechaFin, fecha);

        model.addAttribute("fechaInicio", textoParaFormulario(resultado.fechaInicioTexto()));
        model.addAttribute("fechaFin", textoParaFormulario(resultado.fechaFinTexto()));

        // Si las fechas son inválidas NO se consultan ventas: solo se muestra el error.
        if (resultado.error() != null) {
            model.addAttribute("errorRango", resultado.error());
            model.addAttribute("ventas", List.of());
            model.addAttribute("totalPeriodo", BigDecimal.ZERO);
            model.addAttribute("rangoFormateado", "—");
            return "reporte/reporte";
        }

        if (resultado.sinFechas()) {
            model.addAttribute("infoFechas", "No se indicaron fechas; se muestra el día de hoy.");
        }

        List<VentaDTO> ventas = ventaService.listarPorRango(resultado.inicio(), resultado.fin());
        BigDecimal totalPeriodo = ventas.stream()
                .map(VentaDTO::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("ventas", ventas);
        model.addAttribute("rangoFormateado", formatearRango(resultado.inicio(), resultado.fin()));
        model.addAttribute("totalPeriodo", totalPeriodo);
        return "reporte/reporte";
    }

    // Descarga del PDF del mismo rango de fechas. Si el rango no es válido,
    // no se genera el archivo y se regresa al reporte con el mensaje de error.
    @GetMapping("/diario/pdf")
    public Object generarPdf(@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                             @RequestParam(value = "fechaFin", required = false) String fechaFin,
                             @RequestParam(value = "fecha", required = false) String fecha,
                             RedirectAttributes redirectAttributes) {
        ResultadoRango resultado = resolverRango(fechaInicio, fechaFin, fecha);

        if (resultado.error() != null) {
            redirectAttributes.addFlashAttribute("errorRango", resultado.error());
            redirectAttributes.addFlashAttribute("fechaInicio", resultado.fechaInicioTexto());
            redirectAttributes.addFlashAttribute("fechaFin", resultado.fechaFinTexto());
            return "redirect:/reporte/diario";
        }

        List<VentaDTO> lista = ventaService.listarPorRango(resultado.inicio(), resultado.fin());
        String rango = formatearRango(resultado.inicio(), resultado.fin());
        String titulo = "Reporte de Ventas del " + rango;
        String etiquetaTotal = resultado.inicio().equals(resultado.fin()) ? "TOTAL DEL DÍA" : "TOTAL DEL PERÍODO";
        byte[] pdfBytes = pdfVentaService.generatePdf(lista, titulo, etiquetaTotal, rango);

        String nombreArchivo = resultado.inicio().equals(resultado.fin())
                ? "reporte_zacatlan_" + resultado.inicio() + ".pdf"
                : "reporte_zacatlan_" + resultado.inicio() + "_a_" + resultado.fin() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Resuelve y valida el rango. Distingue "campo vacío" de "fecha mal escrita"
     * para que una fecha inválida nunca caiga silenciosamente en el día de hoy.
     */
    private ResultadoRango resolverRango(String fechaInicio, String fechaFin, String fecha) {
        String textoInicio = fechaInicio;
        String textoFin = fechaFin;

        // Compatibilidad con el parámetro anterior de un solo día
        if (esVacio(textoInicio) && esVacio(textoFin) && !esVacio(fecha)) {
            textoInicio = fecha;
            textoFin = fecha;
        }

        boolean inicioInvalido = !esVacio(textoInicio) && parsearFecha(textoInicio) == null;
        boolean finInvalido = !esVacio(textoFin) && parsearFecha(textoFin) == null;
        if (inicioInvalido || finInvalido) {
            return new ResultadoRango(null, null,
                    "Las fechas no son válidas. Use el formato AAAA-MM-DD (por ejemplo, 2026-11-12).",
                    textoParaMostrar(fechaInicio, fecha), textoParaMostrar(fechaFin, fecha), false);
        }

        LocalDate inicio = parsearFecha(textoInicio);
        LocalDate fin = parsearFecha(textoFin);
        boolean sinFechas = inicio == null && fin == null;

        if (sinFechas) {
            inicio = LocalDate.now();
            fin = inicio;
        } else if (inicio == null) {
            inicio = fin;
        } else if (fin == null) {
            fin = inicio;
        }

        if (inicio.isAfter(fin)) {
            return new ResultadoRango(inicio, fin,
                    "La fecha inicial no puede ser mayor que la fecha final.",
                    textoInicio, textoFin, false);
        }

        return new ResultadoRango(inicio, fin, null, textoInicio, textoFin, sinFechas);
    }

    private String textoParaMostrar(String valor, String fechaLegacy) {
        return esVacio(valor) && !esVacio(fechaLegacy) ? fechaLegacy : valor;
    }

    private String textoParaFormulario(String valor) {
        return valor == null ? "" : valor;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(fecha.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String formatearRango(LocalDate inicio, LocalDate fin) {
        if (inicio.equals(fin)) {
            return inicio.format(FORMATO_FECHA);
        }
        return inicio.format(FORMATO_FECHA) + " al " + fin.format(FORMATO_FECHA);
    }

    private record ResultadoRango(LocalDate inicio,
                                  LocalDate fin,
                                  String error,
                                  String fechaInicioTexto,
                                  String fechaFinTexto,
                                  boolean sinFechas) {
    }
}
