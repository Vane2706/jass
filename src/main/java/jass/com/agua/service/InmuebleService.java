package jass.com.agua.service;

import jass.com.agua.entity.Inmueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InmuebleService {
    List<Inmueble> listarTodos();
    Inmueble obtenerPorId(Long id);
    Inmueble obtenerPorCodigo(String codigoInmueble);
    List<Inmueble> buscar(String texto);
    List<Inmueble> listarPorEstado(String estado);
    List<Inmueble> listarPorSector(String sector);
    Inmueble registrar(Inmueble inmueble);
    Inmueble actualizar(Long id, Inmueble inmueble);
    Inmueble cambiarEstado(Long id, String estado);
    boolean existeCodigo(String codigoInmueble);
    Page<Inmueble> listarPaginado(
            String texto,
            int pagina,
            int tamanio
    );
    List<Inmueble> buscarActivosParaServicio(
            String texto,
            Pageable pageable
    );

    List<Inmueble> buscarDirecciones(
            String q,
            Pageable pageable
    );
}
