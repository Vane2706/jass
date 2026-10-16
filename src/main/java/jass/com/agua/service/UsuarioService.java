package jass.com.agua.service;

import jass.com.agua.entity.Usuario;

import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();

    Usuario obtenerPorId(Long id);

    Usuario obtenerPorUsername(String username);

    List<Usuario> listarActivos();

    Usuario registrar(Usuario usuario);

    Usuario actualizar(Long id, Usuario datos);

    Usuario cambiarEstado(Long id, String estado);

    void cambiarPassword(Long id, String nuevaPassword);

    List<Usuario> listarOperadoresActivos();
}