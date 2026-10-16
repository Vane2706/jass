package jass.com.agua.service.impl;

import jass.com.agua.entity.DetallePago;
import jass.com.agua.entity.Factura;
import jass.com.agua.entity.Pago;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.DetallePagoRepository;
import jass.com.agua.repository.FacturaRepository;
import jass.com.agua.repository.PagoRepository;
import jass.com.agua.service.DetallePagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DetallePagoServiceImpl implements DetallePagoService {

    private final DetallePagoRepository detallePagoRepository;
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DetallePago> listarTodos() { return detallePagoRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public DetallePago obtenerPorId(Long id) {
        return detallePagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de pago no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetallePago> listarPorPago(Long idPago) {
        return detallePagoRepository.findByPago_IdPagoOrderByIdDetallePagoAsc(idPago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetallePago> listarPorFactura(Long idFactura) {
        return detallePagoRepository.findByFactura_IdFacturaOrderByIdDetallePagoAsc(idFactura);
    }

    @Override
    public DetallePago aplicarPagoAFactura(Long idPago, Long idFactura, BigDecimal montoAplicado) {
        if (montoAplicado == null || montoAplicado.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("El monto aplicado debe ser mayor que cero.");

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + idPago));
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));

        if (detallePagoRepository.existsByPago_IdPagoAndFactura_IdFactura(idPago, idFactura))
            throw new BusinessException("Este pago ya fue aplicado a la factura.");

        BigDecimal saldoFactura = obtenerSaldoPendienteFactura(idFactura);
        if (montoAplicado.compareTo(saldoFactura) > 0)
            throw new BusinessException("El monto aplicado supera el saldo pendiente de la factura.");

        BigDecimal totalAplicadoAlPago = detallePagoRepository.findByPago_IdPagoOrderByIdDetallePagoAsc(idPago)
                .stream()
                .map(DetallePago::getMontoAplicado)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal disponiblePago = pago.getMontoTotal().subtract(totalAplicadoAlPago);
        if (montoAplicado.compareTo(disponiblePago) > 0)
            throw new BusinessException("El monto aplicado supera el saldo disponible del pago.");

        DetallePago detalle = DetallePago.builder()
                .pago(pago)
                .factura(factura)
                .montoAplicado(montoAplicado)
                .build();

        detalle = detallePagoRepository.save(detalle);

        BigDecimal nuevoSaldo = obtenerSaldoPendienteFactura(idFactura);
        factura.setEstado(nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? "PAGADA" : "PARCIAL");
        facturaRepository.save(factura);

        BigDecimal nuevoTotalAplicadoPago = totalAplicadoAlPago.add(montoAplicado);
        pago.setEstado(nuevoTotalAplicadoPago.compareTo(pago.getMontoTotal()) == 0 ? "APLICADO" : "PARCIAL");
        pagoRepository.save(pago);

        return detalle;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPagadoFactura(Long idFactura) {
        facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        BigDecimal total = detallePagoRepository.sumarMontoAplicadoPorFactura(idFactura);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoPendienteFactura(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        BigDecimal total = factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO;
        BigDecimal pagado = detallePagoRepository.sumarMontoAplicadoPorFactura(idFactura);
        if (pagado == null) pagado = BigDecimal.ZERO;

        BigDecimal saldo = total.subtract(pagado);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRelacionPagoFactura(Long idPago, Long idFactura) {
        return detallePagoRepository.existsByPago_IdPagoAndFactura_IdFactura(idPago, idFactura);
    }
}
