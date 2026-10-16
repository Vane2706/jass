package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.*;
import jass.com.agua.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FacturaRepository facturaRepository;
    private final TitularidadRepository titularidadRepository;
    private final DetallePagoRepository detallePagoRepository;
    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;


    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarTodos() {

        return pagoRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Pago obtenerPorId(Long id) {

        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pago no encontrado: " + id
                        )
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarPorAsociado(
            Long idAsociado
    ) {

        return pagoRepository
                .findByAsociado_IdAsociadoOrderByFechaPagoDesc(
                        idAsociado
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarPorUsuario(
            Long idUsuario
    ) {

        return pagoRepository
                .findByUsuario_IdUsuarioOrderByFechaPagoDesc(
                        idUsuario
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarPorEstado(
            String estado
    ) {

        return pagoRepository
                .findByEstadoIgnoreCaseOrderByFechaPagoDesc(
                        estado
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarPorRangoFechas(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {

        if (desde == null
                || hasta == null
                || desde.isAfter(hasta)) {

            throw new BusinessException(
                    "El rango de fechas no es válido."
            );
        }

        return pagoRepository
                .findByFechaPagoBetweenOrderByFechaPagoDesc(
                        desde,
                        hasta
                );
    }


    @Override
    public Pago actualizarEstado(
            Long idPago,
            String estado
    ) {

        Pago pago = obtenerPorId(idPago);

        if (estado == null || estado.isBlank()) {

            throw new BusinessException(
                    "El estado es obligatorio."
            );
        }

        pago.setEstado(
                estado.trim().toUpperCase()
        );

        return pagoRepository.save(pago);
    }
    @Override
    public Pago registrarPagoFactura(
            Long idFactura,
            Long idUsuario,
            BigDecimal monto,
            String metodoPago,
            String numeroOperacion,
            String observaciones
    ) {
        Factura factura =
                facturaRepository
                        .findById(idFactura)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Factura no encontrada: "
                                                + idFactura
                                )
                        );


        String estadoFactura =
                factura.getEstado() == null
                        ? ""
                        : factura.getEstado()
                        .trim()
                        .toUpperCase();


        if (!List.of(
                "PENDIENTE",
                "PARCIAL"
        ).contains(estadoFactura)) {

            throw new BusinessException(
                    "La factura no se encuentra disponible para pago."
            );
        }
        Usuario usuario =
                usuarioRepository
                        .findById(idUsuario)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuario no encontrado: "
                                                + idUsuario
                                )
                        );
        Caja caja =
                cajaRepository
                        .findFirstByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAperturaDesc(
                                idUsuario,
                                "ABIERTA"
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "No existe una caja abierta para el usuario seleccionado. "
                                                + "Primero debe abrir una caja antes de registrar el pago."
                                )
                        );
        if (factura.getServicio() == null
                || factura.getServicio().getInmueble() == null) {

            throw new BusinessException(
                    "La factura no posee un inmueble asociado."
            );
        }


        Long idInmueble =
                factura.getServicio()
                        .getInmueble()
                        .getIdInmueble();


        Titularidad titularidad =
                titularidadRepository
                        .findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(
                                idInmueble,
                                "ACTIVO"
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "El inmueble no posee un titular activo."
                                )
                        );


        Asociado asociado =
                titularidad.getAsociado();
        if (metodoPago == null
                || metodoPago.isBlank()) {

            throw new BusinessException(
                    "Debe seleccionar un método de pago."
            );
        }


        String metodo =
                metodoPago
                        .trim()
                        .toUpperCase();


        List<String> metodosPermitidos =
                List.of(
                        "EFECTIVO",
                        "YAPE",
                        "PLIN",
                        "TRANSFERENCIA"
                );


        if (!metodosPermitidos.contains(metodo)) {

            throw new BusinessException(
                    "Método de pago no válido."
            );
        }


        if ("EFECTIVO".equals(metodo)) {

            numeroOperacion = null;

        } else {

            if (numeroOperacion == null
                    || numeroOperacion.isBlank()) {

                throw new BusinessException(
                        "Debe ingresar el número de operación."
                );
            }

            numeroOperacion =
                    numeroOperacion.trim();
        }
        BigDecimal pagado =
                detallePagoRepository
                        .sumarMontoAplicadoPorFactura(
                                idFactura
                        );


        if (pagado == null) {
            pagado = BigDecimal.ZERO;
        }


        BigDecimal totalFactura =
                factura.getTotal() != null
                        ? factura.getTotal()
                        : BigDecimal.ZERO;


        BigDecimal saldo =
                totalFactura
                        .subtract(pagado)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        if (saldo.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    "La factura no posee saldo pendiente."
            );
        }
        if (monto == null
                || monto.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    "El monto debe ser mayor que cero."
            );
        }


        monto = monto.setScale(
                2,
                RoundingMode.HALF_UP
        );


        if (monto.compareTo(saldo) > 0) {

            throw new BusinessException(
                    "El monto supera el saldo pendiente de la factura."
            );
        }

        LocalDateTime fechaOperacion =
                LocalDateTime.now();
        Pago pago =
                Pago.builder()
                        .asociado(asociado)
                        .usuario(usuario)
                        .fechaPago(fechaOperacion)
                        .montoTotal(monto)
                        .metodoPago(metodo)
                        .numeroOperacion(numeroOperacion)
                        .observaciones(
                                normalizarTexto(
                                        observaciones
                                )
                        )
                        .estado("APLICADO")
                        .build();


        pago = pagoRepository.save(pago);
        DetallePago detalle =
                DetallePago.builder()
                        .pago(pago)
                        .factura(factura)
                        .montoAplicado(monto)
                        .build();


        detallePagoRepository.save(detalle);
        if (movimientoCajaRepository
                .existsByPago_IdPago(
                        pago.getIdPago()
                )) {

            throw new BusinessException(
                    "El pago ya posee un movimiento de caja."
            );
        }


        String concepto =
                "Pago de factura "
                        + factura.getNumeroFactura();


        String observacionMovimiento =
                construirObservacionMovimiento(
                        metodo,
                        numeroOperacion
                );


        MovimientoCaja movimiento =
                MovimientoCaja.builder()
                        .caja(caja)
                        .usuario(usuario)
                        .pago(pago)
                        .tipoMovimiento("INGRESO")
                        .concepto(concepto)
                        .monto(monto)
                        .fechaMovimiento(fechaOperacion)
                        .observaciones(
                                observacionMovimiento
                        )

                        .build();


        movimientoCajaRepository.save(
                movimiento
        );

        BigDecimal nuevoSaldo =
                saldo
                        .subtract(monto)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        if (nuevoSaldo.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            factura.setEstado(
                    "PAGADA"
            );

        } else {

            factura.setEstado(
                    "PARCIAL"
            );
        }


        facturaRepository.save(
                factura
        );


        return pago;
    }

    private String normalizarTexto(
            String texto
    ) {

        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        return texto.trim();
    }


    private String construirObservacionMovimiento(
            String metodo,
            String numeroOperacion
    ) {

        if ("EFECTIVO".equals(metodo)) {

            return "Pago recibido en efectivo.";
        }


        return "Pago recibido mediante "
                + metodo
                + ". Operación: "
                + numeroOperacion
                + ".";
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pago> listarPorAsociadoPaginado(
            Long idAsociado,
            int pagina,
            int tamanio
    ) {

        int paginaSegura = Math.max(
                pagina,
                0
        );

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,
                        Sort.by(
                                Sort.Order.desc("fechaPago"),
                                Sort.Order.desc("idPago")
                        )
                );

        return pagoRepository
                .findByAsociado_IdAsociado(
                        idAsociado,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pago> listarPaginado(
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(pagina, 0);

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,
                        Sort.by(
                                Sort.Order.desc("fechaPago"),
                                Sort.Order.desc("idPago")
                        )
                );

        return pagoRepository.findAll(
                pageable
        );
    }
}