package jass.com.agua.service;

import jass.com.agua.entity.TipoServicio;

import java.util.List;

public interface TipoServicioService {
    List<TipoServicio> listarTodos();

    TipoServicio obtenerPorId(Long id);

    TipoServicio obtenerPorNombre(String nombre);

    List<TipoServicio> listarPorEstado(String estado);

    TipoServicio registrar(TipoServicio tipoServicio);

    TipoServicio actualizar(Long id, TipoServicio tipoServicio);

    TipoServicio cambiarEstado(Long id, String estado);

    boolean existeNombre(String nombre);
}
