package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.*;
import jass.com.agua.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final ServicioRepository servicioRepository;
    private final PeriodoRepository periodoRepository;
    private final LecturaRepository lecturaRepository;
    private final TarifaRepository tarifaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final TitularidadRepository titularidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarTodas() { return facturaRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorNumero(String numeroFactura) {
        return facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + numeroFactura));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarPorServicio(Long idServicio) {
        return facturaRepository.findByServicio_IdServicioOrderByFechaEmisionDesc(idServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarPorPeriodo(Long idPeriodo) {
        return facturaRepository.findByPeriodo_IdPeriodoOrderByNumeroFacturaAsc(idPeriodo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarPorEstado(String estado) {
        return facturaRepository.findByEstadoIgnoreCaseOrderByFechaVencimientoAsc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorServicioYPeriodo(Long idServicio, Long idPeriodo) {
        return facturaRepository.findFirstByServicio_IdServicioAndPeriodo_IdPeriodoOrderByIdFacturaDesc(idServicio, idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe factura para el servicio y periodo indicados."));
    }

    @Override
    public Factura generarFactura(Long idServicio, Long idPeriodo) {

        if (facturaRepository
                .existsByServicio_IdServicioAndPeriodo_IdPeriodo(
                        idServicio,
                        idPeriodo
                )) {

            throw new BusinessException(
                    "Ya existe una factura para este servicio y periodo."
            );
        }

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado: " + idServicio
                        )
                );

        Periodo periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Periodo no encontrado: " + idPeriodo
                        )
                );

        Lectura lectura = lecturaRepository
                .findFirstByMedidor_Servicio_IdServicioAndPeriodo_IdPeriodoOrderByIdLecturaDesc(
                        idServicio,
                        idPeriodo
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "No existe una lectura para el servicio y periodo indicados."
                        )
                );

        if (lectura.getConsumo() == null) {
            throw new BusinessException(
                    "La lectura no tiene un consumo calculado."
            );
        }

        BigDecimal consumo = lectura.getConsumo();

        if (consumo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "El consumo registrado no puede ser negativo."
            );
        }

        LocalDate fechaTarifa = periodo.getFechaFin() != null
                ? periodo.getFechaFin()
                : LocalDate.now();

        Tarifa tarifa = tarifaRepository
                .buscarTarifaAplicable(
                        servicio.getTipoServicio()
                                .getIdTipoServicio(),
                        consumo,
                        fechaTarifa
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "No existe tarifa activa aplicable al consumo registrado."
                        )
                );

        BigDecimal importeFijo =
                tarifa.getImporteFijo() != null
                        ? tarifa.getImporteFijo()
                        : BigDecimal.ZERO;

        BigDecimal importeConsumo =
                tarifa.getPrecioM3() != null
                        ? consumo
                        .multiply(tarifa.getPrecioM3())
                        .setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        BigDecimal total = importeFijo
                .add(importeConsumo)
                .setScale(2, RoundingMode.HALF_UP);

        String numero =
                generarNumeroFactura(servicio, periodo);

        LocalDate emision = LocalDate.now();

        LocalDate vencimiento =
                emision.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

        Factura factura = Factura.builder()
                .servicio(servicio)
                .periodo(periodo)
                .numeroFactura(numero)
                .fechaEmision(emision)
                .fechaVencimiento(vencimiento)
                .subtotal(total)
                .total(total)
                .estado("PENDIENTE")
                .build();

        factura = facturaRepository.save(factura);

        DetalleFactura detalle = DetalleFactura.builder()
                .factura(factura)
                .lectura(lectura)
                .tarifa(tarifa)
                .consumo(consumo)
                .importeFijo(importeFijo)
                .importeConsumo(importeConsumo)
                .importeTotal(total)
                .build();

        detalleFacturaRepository.save(detalle);

        return factura;
    }

    @Override
    public Factura actualizarEstado(Long idFactura, String estado) {
        Factura factura = obtenerPorId(idFactura);
        if (estado == null || estado.isBlank()) throw new BusinessException("El estado es obligatorio.");
        factura.setEstado(estado.trim().toUpperCase());
        return facturaRepository.save(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeFacturaParaPeriodo(Long idServicio, Long idPeriodo) {
        return facturaRepository.existsByServicio_IdServicioAndPeriodo_IdPeriodo(idServicio, idPeriodo);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarFacturasPorServicioYEstado(Long idServicio, String estado) {
        return facturaRepository.contarPorServicioYEstado(idServicio, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarFacturasPorCobrar() {

        return facturaRepository
                .findByEstadoInOrderByPeriodo_AnioDescPeriodo_MesDesc(
                        List.of(
                                "PENDIENTE",
                                "PARCIAL"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Asociado obtenerAsociadoFactura(Long idFactura) {

        Factura factura = obtenerPorId(idFactura);

        if (factura.getServicio() == null ||
                factura.getServicio().getInmueble() == null) {

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
                                        "El inmueble de esta factura no posee un titular activo."
                                )
                        );

        return titularidad.getAsociado();
    }

    private String generarNumeroFactura(Servicio servicio, Periodo periodo) {
        String base = String.format("FAC-%04d%02d-%s",
                periodo.getAnio(),
                periodo.getMes(),
                servicio.getCodigoServicio());

        String numero = base;
        int correlativo = 1;
        while (facturaRepository.existsByNumeroFactura(numero)) {
            numero = base + "-" + correlativo++;
        }
        return numero;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Factura> listarPaginado(
            Long idPeriodo,
            String direccion,
            int pagina,
            int tamanio
    ) {

        int paginaSegura = Math.max(pagina, 0);

        String direccionBusqueda =
                direccion == null
                        ? ""
                        : direccion.trim();

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,
                        Sort.by(
                                Sort.Order.desc("fechaEmision"),
                                Sort.Order.desc("idFactura")
                        )
                );

        return facturaRepository.buscarPaginado(
                idPeriodo,
                direccionBusqueda,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Factura> listarFacturasPorCobrarPaginado(
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
                                Sort.Order.desc("periodo.anio"),
                                Sort.Order.desc("periodo.mes"),
                                Sort.Order.desc("fechaEmision"),
                                Sort.Order.desc("idFactura")
                        )
                );

        return facturaRepository.findByEstadoIn(
                List.of(
                        "PENDIENTE",
                        "PARCIAL"
                ),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Factura> listarPorServicioPaginado(
            Long idServicio,
            int pagina,
            int tamanio
    ) {

        if (idServicio == null) {
            throw new BusinessException(
                    "El servicio es obligatorio."
            );
        }

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,

                        // Las facturas más nuevas aparecen arriba
                        Sort.by(
                                Sort.Order.desc("fechaEmision"),
                                Sort.Order.desc("idFactura")
                        )
                );

        return facturaRepository
                .findByServicio_IdServicio(
                        idServicio,
                        pageable
                );
    }
}
