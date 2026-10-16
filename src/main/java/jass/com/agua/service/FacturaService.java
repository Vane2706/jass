package jass.com.agua.service;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Factura;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FacturaService {
    List<Factura> listarTodas();
    Factura obtenerPorId(Long id);
    Factura obtenerPorNumero(String numeroFactura);
    List<Factura> listarPorServicio(Long idServicio);
    List<Factura> listarPorPeriodo(Long idPeriodo);
    List<Factura> listarPorEstado(String estado);
    Factura obtenerPorServicioYPeriodo(Long idServicio, Long idPeriodo);
    Factura generarFactura(Long idServicio, Long idPeriodo);
    Factura actualizarEstado(Long idFactura, String estado);
    boolean existeFacturaParaPeriodo(Long idServicio, Long idPeriodo);
    long contarFacturasPorServicioYEstado(Long idServicio, String estado);
    List<Factura> listarFacturasPorCobrar();
    Asociado obtenerAsociadoFactura(Long idFactura);
    Page<Factura> listarPaginado(
            Long idPeriodo,
            String direccion,
            int pagina,
            int tamanio
    );
    Page<Factura> listarFacturasPorCobrarPaginado(
            int pagina,
            int tamanio
    );

    Page<Factura> listarPorServicioPaginado(
            Long idServicio,
            int pagina,
            int tamanio
    );
}
