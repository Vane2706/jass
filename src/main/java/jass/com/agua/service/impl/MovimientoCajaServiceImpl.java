package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.MovimientoCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.*;
@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoCajaServiceImpl implements MovimientoCajaService {
    private final MovimientoCajaRepository repo;
    private final CajaRepository cajaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PagoRepository pagoRepo;

    public List<MovimientoCaja> listarTodos() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoCaja> listarPorCajaPaginado(
            Long idCaja,
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                tamanio > 0
                        ? tamanio
                        : 5;


        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanioSeguro,

                        Sort.by(
                                Sort.Order.desc(
                                        "fechaMovimiento"
                                ),
                                Sort.Order.desc(
                                        "idMovimiento"
                                )
                        )
                );


        return repo
                .findByCaja_IdCaja(
                        idCaja,
                        pageable
                );
    }

    public MovimientoCaja obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: " + id));
    }

    public List<MovimientoCaja> listarPorCaja(Long id) {
        return repo.findByCaja_IdCajaOrderByFechaMovimientoDesc(id);
    }

    public MovimientoCaja registrar(Long idCaja, Long idUsuario, Long idPago, String tipo, String concepto, BigDecimal monto, String obs) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("El monto debe ser mayor que cero.");
        Caja c = cajaRepo.findById(idCaja).orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));
        if (!"ABIERTA".equalsIgnoreCase(c.getEstado())) throw new BusinessException("La caja está cerrada.");
        Usuario u = usuarioRepo.findById(idUsuario).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Pago p = null;

        if (idPago != null) {

            p = pagoRepo.findById(idPago)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Pago no encontrado: " + idPago
                            )
                    );

            if (repo.existsByPago_IdPago(idPago)) {

                throw new BusinessException(
                        "El pago ya posee un movimiento de caja registrado."
                );
            }
        }
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!List.of("INGRESO", "EGRESO").contains(t)) throw new BusinessException("Tipo de movimiento inválido.");
        return repo.save(MovimientoCaja.builder().caja(c).usuario(u).pago(p).tipoMovimiento(t).concepto(concepto).monto(monto).fechaMovimiento(LocalDateTime.now()).observaciones(obs).build());
    }
}