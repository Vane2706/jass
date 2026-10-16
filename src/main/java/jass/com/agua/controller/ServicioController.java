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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {
    private final ServicioService service;
    private final InmuebleService inmueble;
    private final TipoServicioService tipo;
    private final MedidorService medidor;
    private final FacturaService factura;
    private final TitularidadService titularidad;
    private static final int TAMANIO_PAGINA = 5;
    private static final int TAMANIO_PAGINA_FACTURAS = 5;
    private static final int MAX_NUMEROS_PAGINACION = 10;

    @GetMapping
    public String listar(

            @RequestParam(
                    required = false,
                    defaultValue = ""
            ) String q,

            @RequestParam(
                    defaultValue = "0"
            ) int pagina,

            Model m
    ) {

        if (pagina < 0) {
            pagina = 0;
        }

        Page<Servicio> resultado =
                service.listarPaginado(
                        q,
                        pagina,
                        TAMANIO_PAGINA
                );


        // Si se solicita una página que ya no existe
        if (resultado.getTotalPages() > 0
                && pagina >= resultado.getTotalPages()) {

            pagina =
                    resultado.getTotalPages() - 1;

            resultado =
                    service.listarPaginado(
                            q,
                            pagina,
                            TAMANIO_PAGINA
                    );
        }
        List<Servicio> servicios =
                resultado.getContent();

        List<Long> idsInmuebles =
                servicios
                        .stream()

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
        m.addAttribute(
                "servicios",
                servicios
        );

        m.addAttribute(
                "titularesPorInmueble",
                titularesPorInmueble
        );

        m.addAttribute(
                "paginaActual",
                resultado.getNumber()
        );

        m.addAttribute(
                "totalPaginas",
                resultado.getTotalPages()
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

        m.addAttribute(
                "q",
                q
        );


        return "servicios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {

        Servicio servicio = new Servicio();

        servicio.setInmueble(new Inmueble());
        servicio.setTipoServicio(new TipoServicio());
        servicio.setEstado("ACTIVO");

        m.addAttribute(
                "servicio",
                servicio
        );

        m.addAttribute(
                "tipos",
                tipo.listarPorEstado("ACTIVO")
        );

        return "servicios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Servicio s) {
        return "redirect:/servicios/" + service.registrar(s).getIdServicio();
    }

    @GetMapping("/{id:\\d+}")
    public String detalle(

            @PathVariable Long id,

            @RequestParam(
                    defaultValue = "0"
            )
            int paginaFactura,

            @RequestParam(
                    required = false
            )
            String tab,

            Model m
    ) {

        // Evitar páginas negativas
        if (paginaFactura < 0) {
            paginaFactura = 0;
        }

        Page<Factura> resultadoFacturas =
                factura.listarPorServicioPaginado(
                        id,
                        paginaFactura,
                        TAMANIO_PAGINA_FACTURAS
                );

        if (resultadoFacturas.getTotalPages() > 0
                && paginaFactura
                >= resultadoFacturas.getTotalPages()) {

            paginaFactura =
                    resultadoFacturas
                            .getTotalPages() - 1;


            resultadoFacturas =
                    factura.listarPorServicioPaginado(
                            id,
                            paginaFactura,
                            TAMANIO_PAGINA_FACTURAS
                    );
        }


        int paginaFacturaActual =
                resultadoFacturas.getNumber();

        int totalPaginasFacturas =
                resultadoFacturas.getTotalPages();

        PaginacionVista paginacion =
                calcularPaginacion(
                        paginaFacturaActual,
                        totalPaginasFacturas
                );

        Servicio servicioActual =
                service.obtenerPorId(id);

        m.addAttribute(
                "servicio",
                servicioActual
        );

        m.addAttribute(
                "titularidades",
                titularidad.listarPorInmueble(
                        servicioActual
                                .getInmueble()
                                .getIdInmueble()
                )
        );

        m.addAttribute(
                "medidores",
                medidor.listarPorServicio(id)
        );

        m.addAttribute(
                "facturas",
                resultadoFacturas.getContent()
        );

        m.addAttribute(
                "paginaFacturaActual",
                paginaFacturaActual
        );

        m.addAttribute(
                "totalPaginasFacturas",
                totalPaginasFacturas
        );

        m.addAttribute(
                "totalFacturas",
                resultadoFacturas.getTotalElements()
        );

        m.addAttribute(
                "facturasPrimeraPagina",
                resultadoFacturas.isFirst()
        );

        m.addAttribute(
                "facturasUltimaPagina",
                resultadoFacturas.isLast()
        );

        m.addAttribute(
                "inicioPaginaFactura",
                paginacion.inicio()
        );

        m.addAttribute(
                "finPaginaFactura",
                paginacion.fin()
        );

        m.addAttribute(
                "puntosIzquierdaFactura",
                paginacion.puntosIzquierda()
        );

        m.addAttribute(
                "puntosDerechaFactura",
                paginacion.puntosDerecha()
        );


        // Mantener la pestaña seleccionada
        m.addAttribute(
                "tabActivo",
                tab
        );


        return "servicios/detalle";
    }

    private PaginacionVista calcularPaginacion(
            int paginaActual,
            int totalPaginas
    ) {

        if (totalPaginas <= 0) {

            return new PaginacionVista(
                    0,
                    -1,
                    false,
                    false
            );
        }

        if (totalPaginas
                <= MAX_NUMEROS_PAGINACION) {

            return new PaginacionVista(
                    0,
                    totalPaginas - 1,
                    false,
                    false
            );
        }

        int inicio =
                Math.max(
                        1,
                        paginaActual - 3
                );


        int fin =
                Math.min(
                        totalPaginas - 2,
                        inicio + 7
                );

        inicio =
                Math.max(
                        1,
                        fin - 7
                );

        boolean puntosIzquierda =
                inicio > 1;


        boolean puntosDerecha =
                fin < totalPaginas - 2;


        return new PaginacionVista(
                inicio,
                fin,
                puntosIzquierda,
                puntosDerecha
        );
    }

    private record PaginacionVista(
            int inicio,
            int fin,
            boolean puntosIzquierda,
            boolean puntosDerecha
    ) {
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "servicio",
                service.obtenerPorId(id)
        );

        m.addAttribute(
                "tipos",
                tipo.listarTodos()
        );

        return "servicios/formulario";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Long id, @ModelAttribute Servicio s) {
        service.actualizar(id, s);
        return "redirect:/servicios/" + id;
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam String estado) {
        service.cambiarEstado(id, estado);
        return "redirect:/servicios/" + id;
    }

    @GetMapping("/{id}/medidor/nuevo")
    public String nuevoMedidor(
            @PathVariable Long id,
            Model m
    ) {

        Servicio servicio = service.obtenerPorId(id);

        Medidor medidorNuevo = new Medidor();
        medidorNuevo.setServicio(servicio);
        medidorNuevo.setEstado("ACTIVO");

        m.addAttribute("medidor", medidorNuevo);
        m.addAttribute("servicio", servicio);

        return "servicios/medidor-formulario";
    }

    @PostMapping("/{id}/medidor")
    public String medidor(@PathVariable Long id, @ModelAttribute Medidor x) {
        x.setServicio(service.obtenerPorId(id));
        medidor.registrar(x);
        return "redirect:/servicios/" + id;
    }

    @GetMapping("/buscar-inmuebles")
    @ResponseBody
    public List<Map<String, Object>> buscarInmuebles(
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
                .buscarActivosParaServicio(
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
                            "codigo",
                            i.getCodigoInmueble()
                    );

                    resultado.put(
                            "direccion",
                            i.getDireccion()
                    );

                    resultado.put(
                            "sector",
                            i.getSector()
                    );

                    resultado.put(
                            "distrito",
                            i.getDistrito()
                    );

                    resultado.put(
                            "manzana",
                            i.getManzana()
                    );

                    resultado.put(
                            "lote",
                            i.getNumeroLote()
                    );

                    return resultado;
                })
                .toList();
    }
}
