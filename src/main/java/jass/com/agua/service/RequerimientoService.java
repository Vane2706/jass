package jass.com.agua.service;

import jass.com.agua.entity.Requerimiento;

import java.util.List;

public interface RequerimientoService {
    List<Requerimiento> listarTodos();

    Requerimiento obtenerPorId(Long id);

    List<Requerimiento> listarPorEstado(String estado);

    Requerimiento registrar(Long idProveedor, Long idUsuario, String descripcion);

    Requerimiento actualizarEstado(Long id, String estado);
}