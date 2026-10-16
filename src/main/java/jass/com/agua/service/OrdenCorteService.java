package jass.com.agua.service;

import jass.com.agua.entity.OrdenCorte;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrdenCorteService {
    List<OrdenCorte> listarTodos();

    OrdenCorte obtenerPorId(Long id);

    List<OrdenCorte> listarPorServicio(Long idServicio);

    List<OrdenCorte> listarPorEstado(String estado);

    OrdenCorte evaluarYGenerar(Long idServicio, String observaciones);

    OrdenCorte actualizarEstado(Long id, String estado, String fechaCorte);

    Page<OrdenCorte> listarPaginado(
            int pagina,
            int tamanio
    );
}