package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.ReconexionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReconexionServiceImpl implements ReconexionService {
    private static final BigDecimal TARIFA_RECONEXION = new BigDecimal("20.00");
    private final ReconexionRepository repo;
    private final SuspensionRepository suspensionRepo;
    private final FacturaRepository facturaRepo;
    private final DetallePagoRepository detallePagoRepo;
    private final ServicioRepository servicioRepo;
    private final PagoRepository pagoRepo;
    private final UsuarioRepository usuarioRepo;
    private final CajaRepository cajaRepo;
    private final MovimientoCajaRepository movimientoCajaRepo;
    private final TitularidadRepository titularidadRepo;

    public List<Reconexion> listarTodos() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reconexion> buscarPorSuspension(
            Long idSuspension
    ) {

        return repo.findFirstBySuspension_IdSuspensionOrderByIdReconexionDesc(idSuspension);
    }

    @Override
    public Reconexion pagarDeudaYReconexion(
            Long idReconexion,
            Long idUsuario,
            String metodoPago,
            String numeroOperacion,
            String observaciones
    ) {

        Reconexion r =
                repo.findById(idReconexion)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reconexión no encontrada: "
                                                + idReconexion
                                )
                        );


        if ("EJECUTADA".equalsIgnoreCase(r.getEstado())) {
            throw new BusinessException(
                    "La reconexión ya fue ejecutada."
            );
        }


        if ("PAGADO".equalsIgnoreCase(r.getEstadoPago())) {
            throw new BusinessException(
                    "El pago de esta reconexión ya fue registrado."
            );
        }
        Servicio servicio =
                r.getSuspension()
                        .getOrdenCorte()
                        .getServicio();

        Long idServicio =
                servicio.getIdServicio();

        Usuario usuario =
                usuarioRepo.findById(idUsuario)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuario no encontrado: "
                                                + idUsuario
                                )
                        );

        Caja caja =
                cajaRepo
                        .findFirstByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAperturaDesc(
                                idUsuario,
                                "ABIERTA"
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "No existe una caja abierta para el usuario. "
                                                + "Primero debe abrir una caja."
                                )
                        );
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new BusinessException(
                    "Debe seleccionar un método de pago."
            );
        }

        String metodo =
                metodoPago.trim().toUpperCase();

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

        List<Factura> facturas =
                facturaRepo
                        .findByServicio_IdServicioAndEstadoInOrderByFechaVencimientoAsc(
                                idServicio,
                                List.of(
                                        "PENDIENTE",
                                        "PARCIAL"
                                )
                        );
        Map<Factura, BigDecimal> saldos =
                new LinkedHashMap<>();

        BigDecimal deudaActual =
                BigDecimal.ZERO;


        for (Factura factura : facturas) {

            BigDecimal pagado =
                    detallePagoRepo
                            .sumarMontoAplicadoPorFactura(
                                    factura.getIdFactura()
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
                            .max(BigDecimal.ZERO)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );


            if (saldo.compareTo(BigDecimal.ZERO) > 0) {

                saldos.put(
                        factura,
                        saldo
                );

                deudaActual =
                        deudaActual.add(saldo);
            }
        }


        deudaActual =
                deudaActual.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalCobrar =
                deudaActual
                        .add(TARIFA_RECONEXION)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (servicio.getInmueble() == null) {

            throw new BusinessException(
                    "El servicio no posee un inmueble asociado."
            );
        }


        Long idInmueble =
                servicio.getInmueble()
                        .getIdInmueble();


        Titularidad titularidad =
                titularidadRepo
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

        LocalDateTime ahora =
                LocalDateTime.now();


        Pago pago =
                Pago.builder()
                        .asociado(asociado)
                        .usuario(usuario)
                        .fechaPago(ahora)
                        .montoTotal(totalCobrar)
                        .metodoPago(metodo)
                        .numeroOperacion(numeroOperacion)
                        .observaciones(
                                observaciones == null
                                        || observaciones.isBlank()
                                        ? "Pago por regularización de deuda y reconexión."
                                        : observaciones.trim()
                        )
                        .estado("APLICADO")
                        .build();


        pago =
                pagoRepo.save(pago);
        for (Map.Entry<Factura, BigDecimal> entry
                : saldos.entrySet()) {

            Factura factura =
                    entry.getKey();

            BigDecimal saldo =
                    entry.getValue();


            DetallePago detalle =
                    DetallePago.builder()
                            .pago(pago)
                            .factura(factura)
                            .montoAplicado(saldo)
                            .build();


            detallePagoRepo.save(detalle);


            factura.setEstado("PAGADA");

            facturaRepo.save(factura);
        }
        String concepto =
                "Pago de deuda y reconexión - Servicio "
                        + servicio.getCodigoServicio();


        String observacionMovimiento;

        if ("EFECTIVO".equals(metodo)) {

            observacionMovimiento =
                    "Pago de deuda S/ "
                            + deudaActual
                            + " + reconexión S/ "
                            + TARIFA_RECONEXION
                            + ".";

        } else {

            observacionMovimiento =
                    "Pago de deuda S/ "
                            + deudaActual
                            + " + reconexión S/ "
                            + TARIFA_RECONEXION
                            + ". Método: "
                            + metodo
                            + ". Operación: "
                            + numeroOperacion
                            + ".";
        }


        MovimientoCaja movimiento =
                MovimientoCaja.builder()
                        .caja(caja)
                        .usuario(usuario)
                        .pago(pago)
                        .tipoMovimiento("INGRESO")
                        .concepto(concepto)
                        .monto(totalCobrar)
                        .fechaMovimiento(ahora)
                        .observaciones(observacionMovimiento)
                        .build();


        movimientoCajaRepo.save(movimiento);
        r.setDeudaPendiente(BigDecimal.ZERO);

        r.setMontoReconexion(
                TARIFA_RECONEXION
        );

        r.setMontoTotalRequerido(
                totalCobrar
        );

        r.setEstadoPago(
                "PAGADO"
        );


        return repo.save(r);
    }

    public Reconexion obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reconexión no encontrada: " + id));
    }

    public List<Reconexion> listarPorServicio(Long id) {
        return repo.findBySuspension_OrdenCorte_Servicio_IdServicioOrderByFechaSolicitudDesc(id);
    }

    private BigDecimal deuda(Long idServicio) {
        BigDecimal d = BigDecimal.ZERO;
        for (Factura f : facturaRepo.findByServicio_IdServicioAndEstadoInOrderByFechaVencimientoAsc(idServicio, List.of("PENDIENTE", "PARCIAL"))) {
            BigDecimal p = detallePagoRepo.sumarMontoAplicadoPorFactura(f.getIdFactura());
            if (p == null) p = BigDecimal.ZERO;
            d = d.add((f.getTotal() == null ? BigDecimal.ZERO : f.getTotal()).subtract(p).max(BigDecimal.ZERO));
        }
        return d;
    }

    @Override
    public Reconexion solicitar(
            Long idSusp,
            LocalDate fecha,
            String obs
    ) {

        // Buscar suspensión
        Suspension s =
                suspensionRepo
                        .findById(idSusp)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Suspensión no encontrada: " + idSusp
                                )
                        );


        // La suspensión debe seguir activa
        if (!"ACTIVA".equalsIgnoreCase(s.getEstado())) {

            throw new BusinessException(
                    "La suspensión no está activa."
            );
        }


        // Verificar que NO exista ya una reconexión
        Optional<Reconexion> existente =
                repo
                        .findFirstBySuspension_IdSuspensionOrderByIdReconexionDesc(
                                idSusp
                        );


        if (existente.isPresent()) {

            throw new BusinessException(
                    "Ya existe una solicitud de reconexión "
                            + "para esta suspensión."
            );
        }


        // Calcular deuda actual del servicio
        BigDecimal d =
                deuda(
                        s.getOrdenCorte()
                                .getServicio()
                                .getIdServicio()
                );


        // Crear solicitud de reconexión
        Reconexion reconexion =
                Reconexion.builder()
                        .suspension(s)

                        .fechaSolicitud(
                                fecha == null
                                        ? LocalDate.now()
                                        : fecha
                        )

                        .deudaPendiente(d)

                        .montoReconexion(
                                TARIFA_RECONEXION
                        )

                        .montoTotalRequerido(
                                d.add(
                                        TARIFA_RECONEXION
                                )
                        )

                        .estadoPago(
                                "PENDIENTE"
                        )

                        .estado(
                                "SOLICITADA"
                        )

                        .observaciones(obs)

                        .build();


        return repo.save(
                reconexion
        );
    }

    public Reconexion marcarPago(Long id, boolean pagado) {
        Reconexion r = obtenerPorId(id);
        r.setEstadoPago(pagado ? "PAGADO" : "PENDIENTE");
        return repo.save(r);
    }

    @Override
    public Reconexion ejecutar(
            Long id,
            LocalDate fecha
    ) {

        Reconexion r =
                obtenerPorId(id);


        if ("EJECUTADA".equalsIgnoreCase(r.getEstado())) {

            throw new BusinessException(
                    "La reconexión ya fue ejecutada."
            );
        }


        Long idServicio =
                r.getSuspension()
                        .getOrdenCorte()
                        .getServicio()
                        .getIdServicio();


        BigDecimal deudaActual =
                deuda(idServicio);


        if (deudaActual.compareTo(BigDecimal.ZERO) > 0) {

            throw new BusinessException(
                    "No se puede reconectar: aún existe deuda pendiente."
            );
        }


        if (!"PAGADO".equalsIgnoreCase(
                r.getEstadoPago()
        )) {

            throw new BusinessException(
                    "Debe registrarse el pago de S/ 20.00 por reconexión."
            );
        }


        r.setFechaReconexion(
                fecha == null
                        ? LocalDate.now()
                        : fecha
        );

        r.setEstado("EJECUTADA");

        r.setDeudaPendiente(
                BigDecimal.ZERO
        );


        Suspension s =
                r.getSuspension();

        s.setEstado("FINALIZADA");

        suspensionRepo.save(s);


        Servicio serv =
                s.getOrdenCorte()
                        .getServicio();

        serv.setEstado("ACTIVO");

        servicioRepo.save(serv);


        return repo.save(r);
    }
}