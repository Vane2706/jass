package jass.com.agua.service;

import jass.com.agua.entity.Asociado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AsociadoService {
    List<Asociado> listarTodos();
    Asociado obtenerPorId(Long id);
    Asociado obtenerPorCodigo(String codigoAsociado);
    Asociado obtenerPorDocumento(String documento);
    List<Asociado> buscar(String texto);
    List<Asociado> listarPorEstado(String estado);
    Asociado registrar(Asociado asociado);
    Asociado actualizar(Long id, Asociado asociado);
    Asociado cambiarEstado(Long id, String estado);
    boolean existeCodigo(String codigoAsociado);
    boolean existeDocumento(String documento);
    Page<Asociado> listarPaginado(
            String texto,
            int pagina,
            int tamanio
    );
    List<Asociado> buscarActivosParaTitular(
            String texto,
            Pageable pageable
    );
}
