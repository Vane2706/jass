package jass.com.agua.service;

import jass.com.agua.entity.Suspension;

import java.util.List;
import java.util.Optional;

public interface SuspensionService {
    List<Suspension> listarTodos();

    Suspension obtenerPorId(Long id);

    List<Suspension> listarPorServicio(Long idServicio);

    Suspension ejecutar(Long idOrdenCorte, String motivo, String observaciones);

    Suspension actualizarEstado(Long id, String estado);

    Optional<Suspension> buscarPorOrdenCorte(
            Long idOrdenCorte
    );
}