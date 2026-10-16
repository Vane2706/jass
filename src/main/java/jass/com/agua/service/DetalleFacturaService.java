package jass.com.agua.service;

import jass.com.agua.entity.DetalleFactura;
import java.util.List;

public interface DetalleFacturaService {
    List<DetalleFactura> listarTodos();
    DetalleFactura obtenerPorId(Long id);
    List<DetalleFactura> listarPorFactura(Long idFactura);
    DetalleFactura obtenerPorLectura(Long idLectura);
    List<DetalleFactura> listarPorTarifa(Long idTarifa);
    DetalleFactura generarDetalle(Long idFactura, Long idLectura, Long idTarifa);
    boolean existeDetalleParaLectura(Long idLectura);
}
