package jass.com.agua.service;

import jass.com.agua.entity.DetalleRequerimiento;

import java.math.BigDecimal;
import java.util.List;

public interface DetalleRequerimientoService {
    List<DetalleRequerimiento> listarTodos();

    DetalleRequerimiento obtenerPorId(Long id);

    List<DetalleRequerimiento> listarPorRequerimiento(Long idRequerimiento);

    DetalleRequerimiento agregar(Long idRequerimiento, Long idMaterial, BigDecimal cantidad, BigDecimal precioUnitario);

    void eliminar(Long id);
}