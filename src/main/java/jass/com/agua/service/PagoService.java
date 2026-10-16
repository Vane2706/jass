package jass.com.agua.service;

import jass.com.agua.entity.Pago;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {
    List<Pago> listarTodos();
    Pago obtenerPorId(Long id);
    List<Pago> listarPorAsociado(Long idAsociado);
    List<Pago> listarPorUsuario(Long idUsuario);
    List<Pago> listarPorEstado(String estado);
    List<Pago> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
    Pago actualizarEstado(Long idPago, String estado);
    Pago registrarPagoFactura(
            Long idFactura,
            Long idUsuario,
            BigDecimal monto,
            String metodoPago,
            String numeroOperacion,
            String observaciones
    );

    Page<Pago> listarPorAsociadoPaginado(
            Long idAsociado,
            int pagina,
            int tamanio
    );

    Page<Pago> listarPaginado(
            int pagina,
            int tamanio
    );
}
