package jass.com.agua.service.dashboard;

import jass.com.agua.dto.dashboard.*;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Inmueble;
import jass.com.agua.entity.Lectura;
import jass.com.agua.entity.Medidor;

import jass.com.agua.repository.TitularidadRepository;
import jass.com.agua.repository.OrdenCorteRepository;
import jass.com.agua.repository.SuspensionRepository;
import jass.com.agua.repository.ReconexionRepository;

import jass.com.agua.repository.dashboard.DashRepo;
import jass.com.agua.repository.dashboard.DashboardRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.Year;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl
        implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashRepo dashRepo;

    private final TitularidadRepository titularidadRepository;

    private final OrdenCorteRepository ordenCorteRepository;

    private final SuspensionRepository suspensionRepository;

    private final ReconexionRepository reconexionRepository;


    @Override
    public DashboardDTO obtenerDashboard(
            Integer anio,
            Integer mes
    ) {

        // =============================================
        // 1. VALORES POR DEFECTO
        // =============================================

        if (anio == null) {
            anio = Year.now().getValue();
        }

        if (mes != null
                && (mes < 1 || mes > 12)) {

            mes = null;
        }


        // =============================================
        // 2. KPI
        // =============================================

        BigDecimal totalFacturado =
                ceroSiNull(
                        dashboardRepository
                                .obtenerTotalFacturado(
                                        anio,
                                        mes
                                )
                );


        BigDecimal consumoTotal =
                ceroSiNull(
                        dashboardRepository
                                .obtenerConsumoTotal(
                                        anio,
                                        mes
                                )
                );


        BigDecimal totalRecaudado =
                ceroSiNull(
                        dashboardRepository
                                .obtenerTotalRecaudado(
                                        anio,
                                        mes
                                )
                );


        BigDecimal morosidad =
                ceroSiNull(
                        dashboardRepository
                                .obtenerMorosidad(
                                        anio,
                                        mes
                                )
                );


        Long aptosCorte =
                dashboardRepository
                        .contarServiciosAptosCorte(
                                anio,
                                mes
                        );

        if (aptosCorte == null) {
            aptosCorte = 0L;
        }


        // =============================================
        // 3. ÍNDICE DE RECAUDACIÓN
        // =============================================

        BigDecimal indice;

        if (totalFacturado.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            indice = BigDecimal.ZERO;

        } else {

            indice =
                    totalRecaudado
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    totalFacturado,
                                    1,
                                    RoundingMode.HALF_UP
                            );
        }


        // =============================================
        // 4. EVOLUCIÓN MENSUAL
        // =============================================

        List<EvolucionMensualDTO> evolucion =
                construirEvolucionMensual(
                        anio
                );


        // =============================================
        // 5. CONSUMO MENSUAL
        // =============================================

        List<ConsumoMensualDTO> consumoMensual =
                construirConsumoMensual(
                        anio
                );


        // =============================================
        // 6. CONSUMO POR TIPO
        // =============================================

        List<ConsumoTipoServicioDTO> consumoTipo =
                dashboardRepository
                        .obtenerConsumoPorTipo(
                                anio,
                                mes
                        )
                        .stream()
                        .map(r ->
                                new ConsumoTipoServicioDTO(
                                        String.valueOf(r[0]),
                                        convertirBigDecimal(r[1])
                                )
                        )
                        .toList();


        // =============================================
        // 7. MOROSIDAD POR SECTOR
        // =============================================

        List<MorosidadSectorDTO> morosidadSector =
                dashboardRepository
                        .obtenerMorosidadPorSector(
                                anio,
                                mes
                        )
                        .stream()
                        .map(r ->
                                new MorosidadSectorDTO(
                                        String.valueOf(r[0]),
                                        convertirBigDecimal(r[1])
                                )
                        )
                        .toList();


        // =============================================
        // 8. RECAUDACIÓN POR METODO
        // =============================================

        List<RecaudacionMetodoDTO> recaudacionMetodo =
                dashboardRepository
                        .obtenerRecaudacionPorMetodo(
                                anio,
                                mes
                        )
                        .stream()
                        .map(r ->
                                new RecaudacionMetodoDTO(
                                        String.valueOf(r[0]),
                                        convertirBigDecimal(r[1])
                                )
                        )
                        .toList();


        // =============================================
        // 9. SERVICIOS VENCIDOS
        // =============================================

        List<ServicioVencidoDTO> serviciosVencidos =
                construirServiciosVencidos(
                        anio,
                        mes
                );


        // =============================================
        // 10. LECTURAS DEL DASHBOARD
        // =============================================

        List<Lectura> lecturas =
                dashRepo
                        .obtenerLecturasDashboard(
                                anio,
                                mes
                        );


        // =============================================
        // 11. CONSUMOS ATÍPICOS
        // =============================================

        List<ConsumoAtipicoDTO> consumosAtipicos =
                construirConsumosAtipicos(
                        lecturas
                );


        // =============================================
        // 12. MEDIDORES CON OBSERVACIONES
        // =============================================

        List<MedidorObservacionDTO> medidoresObservacion =
                construirMedidoresObservacion(
                        lecturas
                );


        // =============================================
        // 13. EFECTIVIDAD DE CORTES
        // =============================================

        EfectividadCortesDTO efectividadCortes =
                construirEfectividadCortes(
                        anio,
                        mes
                );


        // =============================================
        // 14. RESPUESTA
        // =============================================

        return DashboardDTO.builder()

                .anio(anio)
                .mes(mes)

                .totalFacturado(
                        totalFacturado
                )

                .consumoTotal(
                        consumoTotal
                )

                .totalRecaudado(
                        totalRecaudado
                )

                .indiceRecaudacion(
                        indice
                )

                .totalMorosidad(
                        morosidad
                )

                .serviciosAptosCorte(
                        aptosCorte
                )

                .evolucionMensual(
                        evolucion
                )

                .consumoMensual(
                        consumoMensual
                )

                .consumoPorTipo(
                        consumoTipo
                )

                .morosidadPorSector(
                        morosidadSector
                )

                .recaudacionPorMetodo(
                        recaudacionMetodo
                )

                .serviciosVencidos(
                        serviciosVencidos
                )

                .consumosAtipicos(
                        consumosAtipicos
                )

                .medidoresObservacion(
                        medidoresObservacion
                )

                .efectividadCortes(
                        efectividadCortes
                )

                .build();
    }


    // =====================================================
    // EVOLUCIÓN MENSUAL
    // =====================================================

    private List<EvolucionMensualDTO>
    construirEvolucionMensual(
            Integer anio
    ) {

        Map<Integer, BigDecimal> facturacion =
                dashboardRepository
                        .obtenerFacturacionMensual(
                                anio
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        r ->
                                                ((Number) r[0])
                                                        .intValue(),

                                        r ->
                                                convertirBigDecimal(
                                                        r[1]
                                                )
                                )
                        );


        Map<Integer, BigDecimal> recaudacion =
                dashboardRepository
                        .obtenerRecaudacionMensual(
                                anio
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        r ->
                                                ((Number) r[0])
                                                        .intValue(),

                                        r ->
                                                convertirBigDecimal(
                                                        r[1]
                                                )
                                )
                        );


        List<EvolucionMensualDTO> resultado =
                new ArrayList<>();


        for (int mes = 1;
             mes <= 12;
             mes++) {

            resultado.add(
                    new EvolucionMensualDTO(
                            mes,
                            nombreMes(mes),

                            facturacion.getOrDefault(
                                    mes,
                                    BigDecimal.ZERO
                            ),

                            recaudacion.getOrDefault(
                                    mes,
                                    BigDecimal.ZERO
                            )
                    )
            );
        }


        return resultado;
    }


    // =====================================================
    // CONSUMO MENSUAL
    // =====================================================

    private List<ConsumoMensualDTO>
    construirConsumoMensual(
            Integer anio
    ) {

        Map<Integer, BigDecimal> consumos =
                dashboardRepository
                        .obtenerConsumoMensual(
                                anio
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        r ->
                                                ((Number) r[0])
                                                        .intValue(),

                                        r ->
                                                convertirBigDecimal(
                                                        r[1]
                                                )
                                )
                        );


        List<ConsumoMensualDTO> resultado =
                new ArrayList<>();


        for (int mes = 1;
             mes <= 12;
             mes++) {

            resultado.add(
                    new ConsumoMensualDTO(
                            mes,
                            nombreMes(mes),

                            consumos.getOrDefault(
                                    mes,
                                    BigDecimal.ZERO
                            )
                    )
            );
        }


        return resultado;
    }


    // =====================================================
    // SERVICIOS CON PERIODOS VENCIDOS
    // =====================================================

    private List<ServicioVencidoDTO>
    construirServiciosVencidos(
            Integer anio,
            Integer mes
    ) {

        return dashRepo
                .obtenerServiciosVencidos(
                        anio,
                        mes
                )
                .stream()

                .map(r -> {

                    Long periodos =
                            ((Number) r[4])
                                    .longValue();

                    String estado;

                    if (periodos >= 3) {

                        estado = "CRITICO";

                    } else if (periodos >= 2) {

                        estado = "ADVERTENCIA";

                    } else {

                        estado = "PENDIENTE";
                    }


                    return new ServicioVencidoDTO(

                            ((Number) r[0])
                                    .longValue(),

                            String.valueOf(
                                    r[1]
                            ),

                            limpiarTexto(
                                    r[2]
                            ),

                            limpiarTexto(
                                    r[3]
                            ),

                            periodos,

                            convertirBigDecimal(
                                    r[5]
                            ),

                            estado
                    );
                })

                .limit(5)

                .toList();
    }


    // =====================================================
    // CONSUMOS ATÍPICOS
    // =====================================================

    private List<ConsumoAtipicoDTO>
    construirConsumosAtipicos(
            List<Lectura> lecturas
    ) {

        List<ConsumoAtipicoDTO> resultado =
                new ArrayList<>();


        for (Lectura lectura : lecturas) {

            if (lectura.getConsumo() == null
                    || lectura.getMedidor() == null
                    || lectura.getPeriodo() == null) {

                continue;
            }


            Double promedioDouble =
                    dashRepo
                            .obtenerPromedioHistorico(

                                    lectura
                                            .getMedidor()
                                            .getIdMedidor(),

                                    lectura
                                            .getIdLectura(),

                                    lectura
                                            .getPeriodo()
                                            .getAnio(),

                                    lectura
                                            .getPeriodo()
                                            .getMes()
                            );


            if (promedioDouble == null
                    || promedioDouble <= 0) {

                continue;
            }


            BigDecimal promedio =
                    BigDecimal
                            .valueOf(
                                    promedioDouble
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );


            BigDecimal variacion =
                    lectura
                            .getConsumo()

                            .subtract(
                                    promedio
                            )

                            .divide(
                                    promedio,
                                    4,
                                    RoundingMode.HALF_UP
                            )

                            .multiply(
                                    BigDecimal.valueOf(
                                            100
                                    )
                            )

                            .setScale(
                                    1,
                                    RoundingMode.HALF_UP
                            );


            /*
             * Para este dashboard nos interesan
             * principalmente aumentos anormales.
             */
            if (variacion.compareTo(
                    BigDecimal.valueOf(25)
            ) < 0) {

                continue;
            }


            String estado =
                    variacion.compareTo(
                            BigDecimal.valueOf(50)
                    ) >= 0

                            ? "CRITICO"
                            : "ADVERTENCIA";


            Medidor medidor =
                    lectura.getMedidor();

            Inmueble inmueble =
                    medidor
                            .getServicio()
                            .getInmueble();


            resultado.add(
                    new ConsumoAtipicoDTO(

                            lectura.getIdLectura(),

                            medidor
                                    .getServicio()
                                    .getCodigoServicio(),

                            medidor
                                    .getNumeroMedidor(),

                            obtenerNombreAsociado(
                                    inmueble
                            ),

                            valorTexto(
                                    inmueble.getSector(),
                                    "Sin sector"
                            ),

                            lectura
                                    .getConsumo()
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    ),

                            promedio,

                            variacion,

                            estado
                    )
            );
        }


        return resultado
                .stream()

                .sorted(
                        Comparator
                                .comparing(
                                        ConsumoAtipicoDTO::getVariacion
                                )
                                .reversed()
                )

                .limit(5)

                .toList();
    }


    // =====================================================
    // MEDIDORES CON OBSERVACIONES
    // =====================================================

    private List<MedidorObservacionDTO>
    construirMedidoresObservacion(
            List<Lectura> lecturas
    ) {

        return lecturas
                .stream()

                .filter(l ->
                        l.getObservaciones() != null
                                && !l.getObservaciones()
                                .isBlank()
                )

                .map(l -> {

                    Medidor medidor =
                            l.getMedidor();

                    Inmueble inmueble =
                            medidor
                                    .getServicio()
                                    .getInmueble();


                    String observacion =
                            l.getObservaciones()
                                    .trim();


                    String estado =
                            determinarEstadoObservacion(
                                    observacion
                            );


                    return new MedidorObservacionDTO(

                            l.getIdLectura(),

                            medidor.getIdMedidor(),

                            medidor.getNumeroMedidor(),

                            obtenerNombreAsociado(
                                    inmueble
                            ),

                            valorTexto(
                                    inmueble.getSector(),
                                    "Sin sector"
                            ),

                            observacion,

                            estado
                    );
                })

                .limit(5)

                .toList();
    }


    // =====================================================
    // EFECTIVIDAD DE CORTES
    // =====================================================

    private EfectividadCortesDTO
    construirEfectividadCortes(
            Integer anio,
            Integer mes
    ) {

        Long ordenes =
                ordenCorteRepository
                        .contarOrdenesDashboard(
                                anio,
                                mes
                        );

        Long ejecutados =
                suspensionRepository
                        .contarCortesEjecutadosDashboard(
                                anio,
                                mes
                        );

        Long reconexiones =
                reconexionRepository
                        .contarReconexionesDashboard(
                                anio,
                                mes
                        );


        ordenes =
                ordenes == null
                        ? 0L
                        : ordenes;

        ejecutados =
                ejecutados == null
                        ? 0L
                        : ejecutados;

        reconexiones =
                reconexiones == null
                        ? 0L
                        : reconexiones;


        long pendientes =
                Math.max(
                        ordenes - ejecutados,
                        0L
                );


        BigDecimal porcentajeEjecutado;

        BigDecimal porcentajePendiente;


        if (ordenes == 0) {

            porcentajeEjecutado =
                    BigDecimal.ZERO;

            porcentajePendiente =
                    BigDecimal.ZERO;

        } else {

            porcentajeEjecutado =
                    BigDecimal.valueOf(
                                    ejecutados
                            )
                            .multiply(
                                    BigDecimal.valueOf(
                                            100
                                    )
                            )
                            .divide(
                                    BigDecimal.valueOf(
                                            ordenes
                                    ),
                                    1,
                                    RoundingMode.HALF_UP
                            );


            porcentajePendiente =
                    BigDecimal.valueOf(
                                    pendientes
                            )
                            .multiply(
                                    BigDecimal.valueOf(
                                            100
                                    )
                            )
                            .divide(
                                    BigDecimal.valueOf(
                                            ordenes
                                    ),
                                    1,
                                    RoundingMode.HALF_UP
                            );
        }


        return new EfectividadCortesDTO(

                ordenes,

                ejecutados,

                reconexiones,

                pendientes,

                porcentajeEjecutado,

                porcentajePendiente
        );
    }


    // =====================================================
    // ASOCIADO ACTUAL DEL INMUEBLE
    // =====================================================

    private String obtenerNombreAsociado(
            Inmueble inmueble
    ) {

        if (inmueble == null
                || inmueble.getIdInmueble() == null) {

            return "Sin titular";
        }


        return titularidadRepository

                .findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(
                        inmueble.getIdInmueble(),
                        "ACTIVO"
                )

                .map(t ->
                        t.getAsociado()
                )

                .map(this::nombreCompleto)

                .orElse(
                        "Sin titular"
                );
    }


    private String nombreCompleto(
            Asociado asociado
    ) {

        if (asociado == null) {
            return "Sin titular";
        }


        StringBuilder nombre =
                new StringBuilder();


        if (asociado.getNombres() != null) {

            nombre.append(
                    asociado.getNombres()
            );
        }


        if (asociado.getApellidoPaterno() != null) {

            nombre.append(" ")
                    .append(
                            asociado.getApellidoPaterno()
                    );
        }


        if (asociado.getApellidoMaterno() != null) {

            nombre.append(" ")
                    .append(
                            asociado.getApellidoMaterno()
                    );
        }


        String resultado =
                nombre
                        .toString()
                        .trim();


        return resultado.isBlank()
                ? "Sin titular"
                : resultado;
    }


    // =====================================================
    // CLASIFICACIÓN DE OBSERVACIONES
    // =====================================================

    private String determinarEstadoObservacion(
            String observacion
    ) {

        if (observacion == null
                || observacion.isBlank()) {

            return "NORMAL";
        }


        String texto =
                observacion
                        .toLowerCase();


        if (texto.contains("roto")
                || texto.contains("fuga")
                || texto.contains("dañado")
                || texto.contains("danado")
                || texto.contains("no funciona")
                || texto.contains("sin lectura")) {

            return "CRITICO";
        }


        return "ADVERTENCIA";
    }


    // =====================================================
    // UTILIDADES
    // =====================================================

    private BigDecimal ceroSiNull(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }


    private BigDecimal convertirBigDecimal(
            Object valor
    ) {

        if (valor == null) {
            return BigDecimal.ZERO;
        }


        if (valor instanceof BigDecimal bd) {
            return bd;
        }


        if (valor instanceof Number numero) {

            return BigDecimal.valueOf(
                    numero.doubleValue()
            );
        }


        return new BigDecimal(
                valor.toString()
        );
    }


    private String limpiarTexto(
            Object valor
    ) {

        if (valor == null) {
            return "—";
        }


        String texto =
                valor.toString()
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return texto.isBlank()
                ? "—"
                : texto;
    }


    private String valorTexto(
            String valor,
            String defecto
    ) {

        return valor == null
                || valor.isBlank()

                ? defecto
                : valor.trim();
    }


    private String nombreMes(
            int mes
    ) {

        return switch (mes) {

            case 1 -> "Ene";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dic";

            default -> "";
        };
    }
}