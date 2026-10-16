package jass.com.agua.service;

public interface ReporteService {
    byte[] generarExcel(String tipo);

    byte[] generarPdf(String tipo);
}
