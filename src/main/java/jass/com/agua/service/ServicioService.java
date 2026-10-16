package jass.com.agua.service;

import jass.com.agua.entity.Servicio;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ServicioService {
    List<Servicio> listarTodos();

    Page<Servicio> listarPaginado(
            String texto,
            int pagina,
            int tamanio
    );

    Servicio obtenerPorId(Long id);

    Servicio obtenerPorCodigo(String codigoServicio);

    List<Servicio> listarPorInmueble(Long idInmueble);

    List<Servicio> listarPorTipoServicio(Long idTipoServicio);

    List<Servicio> listarPorEstado(String estado);

    Servicio obtenerServicioActivoPorInmueble(Long idInmueble);

    Servicio registrar(Servicio servicio);

    Servicio actualizar(Long id, Servicio servicio);

    Servicio cambiarEstado(Long id, String estado);

    boolean existeCodigo(String codigoServicio);
}
