package jass.com.agua.controller;

import jass.com.agua.entity.*;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/facturacion")
@RequiredArgsConstructor
public class FacturacionController {
    private final FacturaService factura;
    private final DetalleFacturaService detalle;
    private final PeriodoService periodo;
    private final TarifaService tarifa;
    private final InmuebleService inmueble;
    private final TipoServicioService tipo;
    private final ServicioService servicio;
    private final TitularidadService titularidad;
    private final AuditoriaService auditoria;
    private static final int TAMANIO_PAGINA = 5;
    private static final int MAX_NUMEROS_PAGINACION = 10;

    @GetMapping
    public String listar(

            @RequestParam(
                    required = false
            )
            Long idPeriodo,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String direccion,

            @RequestParam(
                    defaultValue = "0"
            )
            int pagina,

            Model m
    ) {

        // =====================================================
        // EVITAR PÁGINAS NEGATIVAS
        // =====================================================

        if (pagina < 0) {
            pagina = 0;
        }


        // =====================================================
        // FACTURAS PAGINADAS
        // =====================================================

        Page<Factura> resultado =
                factura.listarPaginado(
                        idPeriodo,
                        direccion,
                        pagina,
                        TAMANIO_PAGINA
                );


        // =====================================================
        // CORREGIR PÁGINA SI YA NO EXISTE
        // =====================================================

        if (resultado.getTotalPages() > 0
                && pagina >= resultado.getTotalPages()) {

            pagina =
                    resultado.getTotalPages() - 1;

            resultado =
                    factura.listarPaginado(
                            idPeriodo,
                            direccion,
                            pagina,
                            TAMANIO_PAGINA
                    );
        }

        List<Factura> facturas =
                resultado.getContent();

        List<Long> idsInmuebles =
                facturas
                        .stream()

                        .map(Factura::getServicio)
                        .filter(Objects::nonNull)

                        .map(Servicio::getInmueble)
                        .filter(Objects::nonNull)

                        .map(Inmueble::getIdInmueble)
                        .filter(Objects::nonNull)

                        .distinct()

                        .toList();

        Map<Long, Asociado> titularesPorInmueble =
                titularidad
                        .obtenerTitularesActivosPorInmuebles(
                                idsInmuebles
                        );

        int paginaActual =
                resultado.getNumber();

        int totalPaginas =
                resultado.getTotalPages();

        int inicioPagina = 0;
        int finPagina = -1;

        boolean mostrarPrimeraPagina = false;
        boolean mostrarUltimaPagina = false;

        boolean mostrarPuntosIzquierda = false;
        boolean mostrarPuntosDerecha = false;


        if (totalPaginas > 0) {

            if (totalPaginas <= MAX_NUMEROS_PAGINACION) {

                inicioPagina = 0;
                finPagina = totalPaginas - 1;

            } else {

                mostrarPrimeraPagina = true;
                mostrarUltimaPagina = true;

                inicioPagina =
                        Math.max(
                                1,
                                paginaActual - 3
                        );

                finPagina =
                        Math.min(
                                totalPaginas - 2,
                                inicioPagina + 7
                        );

                inicioPagina =
                        Math.max(
                                1,
                                finPagina - 7
                        );

                mostrarPuntosIzquierda =
                        inicioPagina > 1;

                mostrarPuntosDerecha =
                        finPagina < totalPaginas - 2;
            }
        }

        m.addAttribute(
                "facturas",
                facturas
        );

        m.addAttribute(
                "titularesPorInmueble",
                titularesPorInmueble
        );

        m.addAttribute(
                "periodos",
                periodo.listarTodos()
        );

        m.addAttribute(
                "idPeriodo",
                idPeriodo
        );

        m.addAttribute(
                "direccion",
                direccion
        );

        m.addAttribute(
                "paginaActual",
                paginaActual
        );

        m.addAttribute(
                "totalPaginas",
                totalPaginas
        );

        m.addAttribute(
                "totalRegistros",
                resultado.getTotalElements()
        );

        m.addAttribute(
                "esPrimera",
                resultado.isFirst()
        );

        m.addAttribute(
                "esUltima",
                resultado.isLast()
        );


        // =====================================================
        // DATOS DE PAGINACIÓN
        // =====================================================

        m.addAttribute(
                "inicioPagina",
                inicioPagina
        );

        m.addAttribute(
                "finPagina",
                finPagina
        );

        m.addAttribute(
                "mostrarPrimeraPagina",
                mostrarPrimeraPagina
        );

        m.addAttribute(
                "mostrarUltimaPagina",
                mostrarUltimaPagina
        );

        m.addAttribute(
                "mostrarPuntosIzquierda",
                mostrarPuntosIzquierda
        );

        m.addAttribute(
                "mostrarPuntosDerecha",
                mostrarPuntosDerecha
        );


        return "facturacion/lista";
    }

    @GetMapping("/generar")
    public String generarForm(Model m) {
        m.addAttribute("servicios", servicio.listarPorEstado("ACTIVO"));
        m.addAttribute("periodos", periodo.listarPorEstado("ABIERTO"));
        return "facturacion/generar";
    }

    @PostMapping("/generar")
    public String generar(@RequestParam Long idServicio, @RequestParam Long idPeriodo) {
        var f = factura.generarFactura(idServicio, idPeriodo);
        auditoria.registrarEvento(
                "CREAR",
                "FACTURA",
                f.getIdFactura(),
                "Factura generada para el servicio ID: "
                        + idServicio
                        + ", período ID: "
                        + idPeriodo
        );
        return "redirect:/facturacion/" + f.getIdFactura();
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model m
    ) {

        Factura facturaActual =
                factura.obtenerPorId(id);

        Asociado asociado =
                factura.obtenerAsociadoFactura(id);

        m.addAttribute(
                "factura",
                facturaActual
        );

        m.addAttribute(
                "detalles",
                detalle.listarPorFactura(id)
        );

        m.addAttribute(
                "asociado",
                asociado
        );

        return "facturacion/detalle";
    }

    @GetMapping("/periodos")
    public String periodos(Model m) {
        m.addAttribute("periodos", periodo.listarTodos());
        m.addAttribute("periodo", new Periodo());
        return "facturacion/periodos";
    }

    @PostMapping("/periodos")
    public String periodo(@ModelAttribute Periodo p) {
        periodo.registrar(p);
        return "redirect:/facturacion/periodos";
    }

    @PostMapping("/periodos/{id}/cerrar")
    public String cerrar(@PathVariable Long id) {
        periodo.cerrarPeriodo(id);
        return "redirect:/facturacion/periodos";
    }

    @PostMapping("/periodos/{id}/abrir")
    public String abrir(@PathVariable Long id) {
        periodo.abrirPeriodo(id);
        return "redirect:/facturacion/periodos";
    }

    @GetMapping("/tarifas")
    public String tarifas(Model m) {
        m.addAttribute("tarifas", tarifa.listarTodas());
        m.addAttribute("tipos", tipo.listarTodos());
        m.addAttribute("tarifa", new Tarifa());
        return "facturacion/tarifas";
    }

    @PostMapping("/tarifas")
    public String tarifa(@ModelAttribute Tarifa t) {
        tarifa.registrar(t);
        return "redirect:/facturacion/tarifas";
    }

    @GetMapping("/buscar-direcciones")
    @ResponseBody
    public List<Map<String, Object>> buscarDirecciones(
            @RequestParam(defaultValue = "") String q
    ) {

        String texto = q.trim();

        if (texto.length() < 2) {
            return List.of();
        }

        Pageable limite =
                PageRequest.of(
                        0,
                        8
                );

        return inmueble
                .buscarDirecciones(
                        texto,
                        limite
                )
                .stream()
                .map(i -> {

                    Map<String, Object> resultado =
                            new LinkedHashMap<>();

                    resultado.put(
                            "idInmueble",
                            i.getIdInmueble()
                    );

                    resultado.put(
                            "direccion",
                            i.getDireccion()
                    );

                    return resultado;
                })
                .toList();
    }
}
