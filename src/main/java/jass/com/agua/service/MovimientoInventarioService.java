package jass.com.agua.service;

import jass.com.agua.entity.MovimientoInventario;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoInventarioService {
    List<MovimientoInventario> listarTodos();

    MovimientoInventario obtenerPorId(Long id);

    List<MovimientoInventario> listarPorMaterial(Long idMaterial);

    MovimientoInventario registrar(Long idMaterial, Long idUsuario, Long idRequerimiento, Long idComprobante, String tipo, BigDecimal cantidad, String motivo, String observaciones);
}