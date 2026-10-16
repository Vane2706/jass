package jass.com.agua.service;

import jass.com.agua.entity.Permiso;

import java.util.List;

public interface PermisoService {
    List<Permiso> listarTodos();

    Permiso obtenerPorId(Long id);

    Permiso guardar(Permiso p);

    void eliminar(Long id);
    boolean existeNombre(String nombre);

    Permiso obtenerPorNombre(String nombre);
}
