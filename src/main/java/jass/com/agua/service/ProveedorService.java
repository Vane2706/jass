package jass.com.agua.service;

import jass.com.agua.entity.Proveedor;

import java.util.List;

public interface ProveedorService {
    List<Proveedor> listarTodos();

    Proveedor obtenerPorId(Long id);

    List<Proveedor> listarActivos();

    Proveedor registrar(Proveedor p);

    Proveedor actualizar(Long id, Proveedor p);

    Proveedor cambiarEstado(Long id, String estado);
}