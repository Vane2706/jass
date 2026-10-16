package jass.com.agua.service;

import jass.com.agua.entity.DetallePago;
import java.math.BigDecimal;
import java.util.List;

public interface DetallePagoService {
    List<DetallePago> listarTodos();
    DetallePago obtenerPorId(Long id);
    List<DetallePago> listarPorPago(Long idPago);
    List<DetallePago> listarPorFactura(Long idFactura);
    DetallePago aplicarPagoAFactura(Long idPago, Long idFactura, BigDecimal montoAplicado);
    BigDecimal obtenerTotalPagadoFactura(Long idFactura);
    BigDecimal obtenerSaldoPendienteFactura(Long idFactura);
    boolean existeRelacionPagoFactura(Long idPago, Long idFactura);
}
