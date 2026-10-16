package jass.com.agua.service;

import jass.com.agua.entity.Material;

import java.util.List;

public interface MaterialService {
    List<Material> listarTodos();

    Material obtenerPorId(Long id);

    List<Material> listarActivos();

    Material registrar(Material material);

    Material actualizar(Long id, Material datos);

    Material cambiarEstado(Long id, String estado);
}