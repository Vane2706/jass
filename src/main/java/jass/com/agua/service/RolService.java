package jass.com.agua.service;

import jass.com.agua.entity.Rol;

import java.util.List;

public interface RolService {
    List<Rol> listarTodos();

    Rol obtenerPorId(Long id);
    Rol obtenerPorNombre(String nombre);

    Rol guardar(Rol rol);

    void eliminar(Long id);
    boolean existeNombre(String nombre);
}
