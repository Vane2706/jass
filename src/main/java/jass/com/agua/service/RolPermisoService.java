package jass.com.agua.service;

import jass.com.agua.entity.RolPermiso;

import java.util.List;

public interface RolPermisoService {
    List<RolPermiso> listarTodos();

    List<RolPermiso> listarPorRol(Long idRol);

    RolPermiso asignar(Long idRol, Long idPermiso);

    void quitar(Long idRol, Long idPermiso);
    boolean tienePermiso(Long idRol, Long idPermiso);
}
