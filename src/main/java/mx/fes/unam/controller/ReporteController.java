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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/reporte")
public class ReporteController {

    private final VentaService ventaService;
    private final PdfVentaService pdfVentaService;

    public ReporteController(VentaService ventaService, PdfVentaService pdfVentaService) {
        this.ventaService = ventaService;
        this.pdfVentaService = pdfVentaService;
    }

    // Reporte diario en pantalla: filtro de fecha con default hoy
    @GetMapping("/diario")
    public String diario(@RequestParam(value = "fecha", required = false) String fecha, Model model) {
        LocalDate dia = resolverFecha(fecha);
        List<VentaDTO> ventas = ventaService.listarPorFecha(dia);
        BigDecimal totalDia = ventas.stream()
                .map(VentaDTO::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("ventas", ventas);
        model.addAttribute("fecha", dia.toString());
        model.addAttribute("fechaFormateada", dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("totalDia", totalDia);
        return "reporte/reporte";
    }

    // Descarga del PDF (patrón de LibroController.generar_pdf del proyecto jpa)
    @GetMapping("diario/pdf")
    public ResponseEntity<byte[]> generarPdf(@RequestParam(value = "fecha", required = false) String fecha) {
        LocalDate dia = resolverFecha(fecha);
        List<VentaDTO> lista = ventaService.listarPorFecha(dia);
        byte[] pdfBytes = pdfVentaService.generatePdf(lista);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_zacatlan_" + dia + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private LocalDate resolverFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }
}
