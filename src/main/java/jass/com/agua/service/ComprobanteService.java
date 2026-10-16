package jass.com.agua.service;

import jass.com.agua.entity.Comprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ComprobanteService {
    List<Comprobante> listarTodos();

    Comprobante obtenerPorId(Long id);

    List<Comprobante> listarPorRequerimiento(Long id);

    Comprobante registrar(Long idRequerimiento, String tipo, String serie, String numero, LocalDate fecha, BigDecimal importe, String estadoPago, String archivoUrl);

    Comprobante actualizarEstadoPago(Long id, String estado);
}