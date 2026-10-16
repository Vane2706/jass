package jass.com.agua.service;

import jass.com.agua.dto.MedidorLecturaDTO;
import jass.com.agua.entity.Medidor;
import java.util.List;

public interface MedidorService {
    List<Medidor> listarTodos();
    Medidor obtenerPorId(Long id);
    Medidor obtenerPorNumero(String numeroMedidor);
    List<Medidor> listarPorServicio(Long idServicio);
    List<Medidor> listarPorEstado(String estado);
    Medidor obtenerMedidorActivoPorServicio(Long idServicio);
    Medidor registrar(Medidor medidor);
    Medidor actualizar(Long id, Medidor medidor);
    Medidor cambiarEstado(Long id, String estado);
    boolean existeNumero(String numeroMedidor);
    List<MedidorLecturaDTO> buscarParaLectura(String q);
}
