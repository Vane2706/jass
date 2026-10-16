package jass.com.agua.service;

import jass.com.agua.entity.UsuarioRol;

import java.util.List;
import java.util.Map;

public interface UsuarioRolService {
    List<UsuarioRol> listarTodos();

    List<UsuarioRol> listarPorUsuario(Long idUsuario);

    UsuarioRol asignar(Long idUsuario, Long idRol);

    void quitar(Long idUsuario, Long idRol);

    Map<Long, Long> obtenerRolesPorUsuario();
}
