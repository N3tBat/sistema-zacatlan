package mx.fes.unam.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mx.fes.unam.model.entities.venta.VentaDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera el reporte diario de ventas en PDF (patrón de PdfLibroService del proyecto jpa):
 * A4 con márgenes de 36, título centrado y tabla con encabezados coloreados.
 */
@Service
public class PdfVentaService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generatePdf(List<VentaDTO> lista) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Paragraph title = new Paragraph("Reporte Diario de Ventas — Municipio de Zacatlán");
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable table = new PdfPTable(7);

            // Encabezados coloreados
            String[] encabezados = {"ID", "Fecha", "Vendedor", "Bloque", "Producto", "Cantidad", "Total"};
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado));
                celda.setBackgroundColor(new Color(167, 218, 255));
                celda.setPadding(8f);
                table.addCell(celda);
            }

            BigDecimal totalDia = BigDecimal.ZERO;
            for (VentaDTO venta : lista) {
                table.addCell(String.valueOf(venta.getId()));
                table.addCell(venta.getFecha() != null ? FORMATO_FECHA.format(venta.getFecha()) : "—");
                // Snapshot del vendedor: se imprime aunque ya esté borrado
                table.addCell(venta.getVendedorNombre() != null ? venta.getVendedorNombre() : "—");
                table.addCell(venta.getBloqueNombre() != null ? venta.getBloqueNombre() : "—");
                table.addCell(venta.getProductoNombre() != null ? venta.getProductoNombre() : "—");
                table.addCell(String.valueOf(venta.getCantidad()));
                table.addCell(venta.getTotal() != null ? venta.getTotal().toPlainString() : "0.00");
                if (venta.getTotal() != null) {
                    totalDia = totalDia.add(venta.getTotal());
                }
            }

            // Fila final: total del día
            PdfPCell celdaTotal = new PdfPCell(new Phrase("TOTAL DEL DÍA"));
            celdaTotal.setBackgroundColor(new Color(255, 228, 181));
            celdaTotal.setPadding(8f);
            celdaTotal.setColspan(6);
            table.addCell(celdaTotal);

            PdfPCell celdaTotalValor = new PdfPCell(new Phrase(totalDia.setScale(2).toPlainString()));
            celdaTotalValor.setBackgroundColor(new Color(255, 228, 181));
            celdaTotalValor.setPadding(8f);
            table.addCell(celdaTotalValor);

            document.add(table);
            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }
}
