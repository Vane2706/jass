package jass.com.agua.service.impl;

import jass.com.agua.exception.BusinessException;
import jass.com.agua.repository.*;
import jass.com.agua.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private final AsociadoRepository asociado;
    private final LecturaRepository lectura;
    private final FacturaRepository factura;
    private final PagoRepository pago;
    private final OrdenCorteRepository orden;
    private final MovimientoCajaRepository movCaja;
    private final CierreCajaRepository cierre;

    private List<String[]> datos(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "asociados" ->
                    asociado.findAll().stream().map(x -> new String[]{v(x.getCodigoAsociado()), v(x.getNombres()), v(x.getApellidoPaterno()), v(x.getDocumento()), v(x.getEstado())}).toList();
            case "lecturas", "consumo" ->
                    lectura.findAll().stream().map(x -> new String[]{v(x.getIdLectura()), v(x.getMedidor().getNumeroMedidor()), v(x.getPeriodo().getAnio()) + "-" + v(x.getPeriodo().getMes()), v(x.getLecturaAnterior()), v(x.getLecturaActual()), v(x.getConsumo())}).toList();
            case "facturacion" ->
                    factura.findAll().stream().map(x -> new String[]{v(x.getNumeroFactura()), v(x.getServicio().getCodigoServicio()), v(x.getTotal()), v(x.getEstado()), v(x.getFechaVencimiento())}).toList();
            case "pagos" ->
                    pago.findAll().stream().map(x -> new String[]{v(x.getIdPago()), v(x.getAsociado().getCodigoAsociado()), v(x.getFechaPago()), v(x.getMontoTotal()), v(x.getMetodoPago()), v(x.getEstado())}).toList();
            case "morosidad", "ordenes-corte" ->
                    orden.findAll().stream().map(x -> new String[]{v(x.getIdOrdenCorte()), v(x.getServicio().getCodigoServicio()), v(x.getPeriodosPendientes()), v(x.getDeudaAcumulada()), v(x.getEstado())}).toList();
            case "caja" ->
                    movCaja.findAll().stream().map(x -> new String[]{v(x.getIdMovimiento()), v(x.getCaja().getIdCaja()), v(x.getTipoMovimiento()), v(x.getConcepto()), v(x.getMonto()), v(x.getFechaMovimiento())}).toList();
            case "cierres" ->
                    cierre.findAll().stream().map(x -> new String[]{v(x.getIdCierre()), v(x.getCaja().getIdCaja()), v(x.getTotalIngresos()), v(x.getTotalEgresos()), v(x.getSaldoCalculado()), v(x.getSaldoReal()), v(x.getDiferencia())}).toList();
            default -> throw new BusinessException("Tipo de reporte no válido: " + tipo);
        };
    }

    private String[] headers(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "asociados" -> new String[]{"Código", "Nombres", "Apellido", "Documento", "Estado"};
            case "lecturas", "consumo" -> new String[]{"ID", "Medidor", "Periodo", "Anterior", "Actual", "Consumo"};
            case "facturacion" -> new String[]{"Factura", "Servicio", "Total", "Estado", "Vencimiento"};
            case "pagos" -> new String[]{"ID", "Asociado", "Fecha", "Monto", "Método", "Estado"};
            case "morosidad", "ordenes-corte" -> new String[]{"Orden", "Servicio", "Periodos", "Deuda", "Estado"};
            case "caja" -> new String[]{"ID", "Caja", "Tipo", "Concepto", "Monto", "Fecha"};
            case "cierres" -> new String[]{"ID", "Caja", "Ingresos", "Egresos", "Calculado", "Real", "Diferencia"};
            default -> throw new BusinessException("Tipo de reporte no válido.");
        };
    }

    private String v(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    public byte[] generarExcel(String tipo) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Reporte");
            String[] h = headers(tipo);
            Row r = sh.createRow(0);
            for (int i = 0; i < h.length; i++) r.createCell(i).setCellValue(h[i]);
            int n = 1;
            for (String[] d : datos(tipo)) {
                r = sh.createRow(n++);
                for (int i = 0; i < d.length; i++) r.createCell(i).setCellValue(d[i]);
            }
            for (int i = 0; i < h.length; i++) sh.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("No se pudo generar Excel.");
        }
    }

    public byte[] generarPdf(String tipo) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("JASS Mishquiyacu - Reporte de " + tipo));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(String.join(" | ", headers(tipo))));
            for (String[] d : datos(tipo)) doc.add(new Paragraph(String.join(" | ", d)));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar PDF.");
        }
    }
}