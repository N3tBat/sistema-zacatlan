package mx.fes.unam.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import mx.fes.unam.model.entities.venta.VentaDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Genera el reporte de ventas en PDF con presentación institucional:
 * encabezado con el municipio y el periodo, tabla con encabezado repetible,
 * filas alternas, total destacado y pie de página con número de página.
 */
@Service
public class PdfVentaService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_EMISION = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color AZUL = new Color(31, 78, 121);
    private static final Color AZUL_CLARO = new Color(222, 235, 247);
    private static final Color GRIS = new Color(90, 100, 110);
    private static final Color TEXTO = new Color(45, 55, 65);
    private static final Color FILA_ALTERNA = new Color(245, 248, 252);

    public byte[] generatePdf(List<VentaDTO> lista) {
        return generatePdf(lista, "Reporte Diario de Ventas — Municipio de Zacatlán", "TOTAL DEL DÍA", null);
    }

    public byte[] generatePdf(List<VentaDTO> lista, String titulo, String etiquetaTotal) {
        return generatePdf(lista, titulo, etiquetaTotal, null);
    }

    public byte[] generatePdf(List<VentaDTO> lista, String titulo, String etiquetaTotal, String subtitulo) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 58, 45);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new PieDePagina());
            document.addTitle(titulo);
            document.addAuthor("Municipio de Zacatlán");
            document.addSubject("Reporte de ventas del Sistema Zacatlán");
            document.open();

            agregarEncabezado(document, titulo, subtitulo);

            PdfPTable table = new PdfPTable(new float[]{5, 14, 19, 12, 19, 7, 14});
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(8f);

            String[] encabezados = {"ID", "Fecha", "Vendedor", "Bloque", "Producto", "Cant.", "Total"};
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuente(true, 8.5f, Color.WHITE)));
                celda.setBackgroundColor(AZUL);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
                celda.setPadding(6f);
                celda.setBorderColor(Color.WHITE);
                celda.setBorderWidth(0.5f);
                table.addCell(celda);
            }

            BigDecimal totalPeriodo = BigDecimal.ZERO;
            if (lista == null || lista.isEmpty()) {
                PdfPCell celda = new PdfPCell(new Phrase(
                        "No hay ventas registradas para el periodo seleccionado.", fuente(false, 9f, GRIS)));
                celda.setColspan(7);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
                celda.setPadding(14f);
                celda.setBackgroundColor(FILA_ALTERNA);
                table.addCell(celda);
            } else {
                int fila = 0;
                for (VentaDTO venta : lista) {
                    Color fondo = fila++ % 2 == 0 ? Color.WHITE : FILA_ALTERNA;
                    agregarCelda(table, String.valueOf(venta.getId()), fondo, Element.ALIGN_CENTER);
                    agregarCelda(table, venta.getFecha() != null ? FORMATO_FECHA.format(venta.getFecha()) : "—",
                            fondo, Element.ALIGN_CENTER);
                    // Snapshot del vendedor: se imprime aunque ya esté borrado
                    agregarCelda(table, venta.getVendedorNombre(), fondo, Element.ALIGN_LEFT);
                    agregarCelda(table, venta.getBloqueNombre(), fondo, Element.ALIGN_LEFT);
                    agregarCelda(table, venta.getProductoNombre(), fondo, Element.ALIGN_LEFT);
                    agregarCelda(table, String.valueOf(venta.getCantidad()), fondo, Element.ALIGN_CENTER);
                    agregarCelda(table, formatearMoneda(venta.getTotal()), fondo, Element.ALIGN_RIGHT);
                    if (venta.getTotal() != null) {
                        totalPeriodo = totalPeriodo.add(venta.getTotal());
                    }
                }
            }

            PdfPCell celdaEtiquetaTotal = new PdfPCell(new Phrase(etiquetaTotal, fuente(true, 10f, AZUL)));
            celdaEtiquetaTotal.setColspan(6);
            celdaEtiquetaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaEtiquetaTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaEtiquetaTotal.setBackgroundColor(AZUL_CLARO);
            celdaEtiquetaTotal.setPadding(7f);
            table.addCell(celdaEtiquetaTotal);

            PdfPCell valorTotal = new PdfPCell(new Phrase(formatearMoneda(totalPeriodo), fuente(true, 10f, AZUL)));
            valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valorTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
            valorTotal.setBackgroundColor(AZUL_CLARO);
            valorTotal.setPadding(7f);
            table.addCell(valorTotal);

            document.add(table);

            Paragraph resumen = new Paragraph("Movimientos: " + (lista == null ? 0 : lista.size()),
                    fuente(false, 9f, GRIS));
            resumen.setAlignment(Element.ALIGN_RIGHT);
            resumen.setSpacingBefore(2f);
            document.add(resumen);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private void agregarEncabezado(Document document, String titulo, String subtitulo) throws DocumentException {
        Paragraph institucion = new Paragraph("MUNICIPIO DE ZACATLÁN", fuente(true, 14f, AZUL));
        institucion.setAlignment(Element.ALIGN_CENTER);
        institucion.setSpacingAfter(2f);
        document.add(institucion);

        Paragraph sistema = new Paragraph("Sistema de Comercio Municipal", fuente(false, 9f, GRIS));
        sistema.setAlignment(Element.ALIGN_CENTER);
        sistema.setSpacingAfter(12f);
        document.add(sistema);

        Paragraph tituloReporte = new Paragraph(titulo, fuente(true, 15f, TEXTO));
        tituloReporte.setAlignment(Element.ALIGN_CENTER);
        tituloReporte.setSpacingAfter(4f);
        document.add(tituloReporte);

        if (subtitulo != null && !subtitulo.isBlank()) {
            Paragraph periodo = new Paragraph("Periodo: " + subtitulo, fuente(false, 10f, GRIS));
            periodo.setAlignment(Element.ALIGN_CENTER);
            periodo.setSpacingAfter(2f);
            document.add(periodo);
        }

        Paragraph emision = new Paragraph("Generado: " + FORMATO_EMISION.format(LocalDateTime.now()),
                fuente(false, 8f, GRIS));
        emision.setAlignment(Element.ALIGN_CENTER);
        emision.setSpacingAfter(10f);
        document.add(emision);

        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorder(PdfPCell.BOTTOM);
        celdaLinea.setBorderColor(AZUL);
        celdaLinea.setBorderWidthBottom(1.2f);
        celdaLinea.setFixedHeight(1f);
        linea.addCell(celdaLinea);
        document.add(linea);
    }

    private void agregarCelda(PdfPTable table, String texto, Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null || texto.isBlank() ? "—" : texto,
                fuente(false, 8.5f, TEXTO)));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(5f);
        celda.setBorderColor(new Color(220, 225, 230));
        celda.setBorderWidth(0.4f);
        table.addCell(celda);
    }

    private String formatearMoneda(BigDecimal valor) {
        DecimalFormat formato = new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static Font fuente(boolean negrita, float tamano, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, tamano, negrita ? Font.BOLD : Font.NORMAL, color);
    }

    /** Pie de página con la fecha de emisión y "Página X de Y". */
    private static class PieDePagina extends PdfPageEventHelper {

        private final Font fuentePie = fuente(false, 8f, GRIS);
        private PdfTemplate totalPaginas;

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte contenido = writer.getDirectContent();
            contenido.saveState();

            String izquierda = "Sistema Zacatlán · Emitido " + FORMATO_EMISION.format(LocalDateTime.now());
            ColumnText.showTextAligned(contenido, Element.ALIGN_LEFT, new Phrase(izquierda, fuentePie),
                    document.left(), document.bottom() - 14f, 0);

            String pagina = "Página " + writer.getPageNumber();
            ColumnText.showTextAligned(contenido, Element.ALIGN_RIGHT, new Phrase(pagina, fuentePie),
                    document.right() - 58f, document.bottom() - 14f, 0);

            if (totalPaginas == null) {
                totalPaginas = PdfTemplate.createTemplate(writer, 58f, 12f);
            }
            contenido.addTemplate(totalPaginas, document.right() - 58f, document.bottom() - 16f);
            contenido.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            if (totalPaginas != null) {
                totalPaginas.beginText();
                totalPaginas.setFontAndSize(fuentePie.getBaseFont(), fuentePie.getSize());
                totalPaginas.setColorFill(GRIS);
                // Al cerrar el documento el contador ya avanzó a la página siguiente.
                totalPaginas.showTextAligned(Element.ALIGN_LEFT, " de " + (writer.getPageNumber() - 1), 0, 2f, 0);
                totalPaginas.endText();
            }
        }
    }
}
